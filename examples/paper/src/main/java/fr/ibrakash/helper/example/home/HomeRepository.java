package fr.ibrakash.helper.example.home;

import fr.ibrakash.helper.configuration.objects.database.ConfigPersistence;
import fr.ibrakash.helper.persistence.EntityStore;
import fr.ibrakash.helper.persistence.PersistenceSession;
import fr.ibrakash.helper.persistence.PersistenceType;
import fr.ibrakash.helper.platform.KashAddon;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Homes repository.
 *
 * <p>Owns the in-memory cache. All backend I/O goes through the annotation-driven
 * {@link EntityStore} — no adapters, no manual SQL statements needed.
 *
 * <pre>{@code
 * HomeRepository homes = HomeRepository.create(addon, config);
 * homes.save(HomeRecord.of(player.getUniqueId(), "base", player.getLocation()));
 * homes.list(player.getUniqueId()).forEach(h -> player.sendMessage(h.getName()));
 * homes.delete(player.getUniqueId(), "base");
 * homes.saveAll();
 * homes.close();
 * }</pre>
 */
public final class HomeRepository {

    private final PersistenceSession session;
    private final EntityStore<HomeRecord, String> store;

    private final Map<String, HomeRecord> cache = new ConcurrentHashMap<>();

    private HomeRepository(PersistenceSession session) {
        this.session = session;
        this.store = session.entity(HomeRecord.class, String.class);
        // reload() is now automatically called by the base ConfigurationReader constructor.
    }

    public static HomeRepository create(KashAddon<JavaPlugin> addon, ConfigPersistence config) {
        return new HomeRepository(PersistenceSession.create(addon, config));
    }

    // -------------------------------------------------------------------------
    // Cache management
    // -------------------------------------------------------------------------

    public void reload() {
        this.cache.clear();
        this.store.findAll().forEach(h -> this.cache.put(h.key(), h));
    }

    // -------------------------------------------------------------------------
    // Read operations (served from cache)
    // -------------------------------------------------------------------------

    public Optional<HomeRecord> find(UUID owner, String name) {
        return Optional.ofNullable(this.cache.get(owner + ":" + name.toLowerCase()));
    }

    public List<HomeRecord> list(UUID owner) {
        String prefix = owner.toString();
        return this.cache.values().stream()
                .filter(h -> h.key().startsWith(prefix + ":"))
                .sorted(Comparator.comparing(HomeRecord::isFavorite).reversed()
                        .thenComparing(HomeRecord::getName))
                .toList();
    }

    public Map<UUID, Integer> topOwners(int limit) {
        Map<UUID, Integer> counts = new ConcurrentHashMap<>();
        this.cache.values().forEach(h -> counts.merge(h.getOwnerUuid(), 1, Integer::sum));
        return counts.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    // -------------------------------------------------------------------------
    // Write operations (write-through: cache + backend)
    // -------------------------------------------------------------------------

    public void save(HomeRecord home) {
        this.cache.put(home.key(), home);
        this.store.save(home);
    }

    public void delete(UUID owner, String name) {
        String key = owner + ":" + name.toLowerCase();
        this.cache.remove(key);
        this.store.delete(key);
    }

    public void saveAll() {
        this.store.saveAll(this.cache.values());
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    public void close() {
        this.session.close();
    }

    public PersistenceType backendType() {
        return this.session.backendType();
    }

    public int cacheSize() {
        return this.cache.size();
    }
}
