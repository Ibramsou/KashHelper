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

public final class HomeRepository {

    private final PersistenceSession session;
    private final EntityStore<HomeRecord, String> store;

    private final Map<String, HomeRecord> cache = new ConcurrentHashMap<>();

    private HomeRepository(PersistenceSession session) {
        this.session = session;
        this.store = session.entity(HomeRecord.class, String.class);
    }

    public static HomeRepository create(KashAddon<JavaPlugin> addon, ConfigPersistence config) {
        return new HomeRepository(PersistenceSession.create(addon, config));
    }


    public void reload() {
        this.cache.clear();
        this.store.findAll().forEach(h -> this.cache.put(h.key(), h));
    }


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
