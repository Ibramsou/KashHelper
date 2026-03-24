package fr.ibrakash.helper.example.sql;

import fr.ibrakash.helper.example.ExampleConfig;
import fr.ibrakash.helper.persistence.adapter.DatabaseRepository;
import fr.ibrakash.helper.platform.KashAddon;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Example repository that manages {@link ExampleData} for players.
 *
 * <p>Owns a single in-memory cache and works transparently with both JSON and SQL
 * backends through the default adapter system.
 *
 * <pre>{@code
 * // Create
 * ExampleRepository repo = new ExampleRepository(addon);
 *
 * // Async load
 * repo.getCached(player.getUniqueId()).thenAccept(data -> {
 *     player.sendMessage("Points: " + data.getPoints());
 * });
 *
 * // Write-through save
 * data.setPoints(data.getPoints() + 10);
 * repo.save(data);
 *
 * // On disable
 * repo.saveAll();
 * repo.close();
 * }</pre>
 */
public class ExampleRepository extends DatabaseRepository {

    /** Single source-of-truth in-memory cache: String id → data. */
    private final Map<String, ExampleData> cache = new ConcurrentHashMap<>();

    public ExampleRepository(KashAddon<JavaPlugin> addon) {
        super(addon, ExampleConfig.get().getDatabase());
    }

    public List<ExampleData> getTopProfiles() {
        // Full fetch from DB, all columns, ORDER BY score DESC
        return this.sortBuilder(ExampleData.class)
                .descClause("score")
                .build();
    }

    public List<ExampleData> getTop10Profiles() {
        // Partial projection: only id + score + display_name fetched from DB
        return this.sortBuilder(ExampleData.class)
                .descClause("score")
                .loadColumns("score", "display_name")
                .limit(10)
                .build();
    }

    public List<ExampleTopEntry> getLeaderboardPage(int limit) {
        // Sort in-memory cache (no DB call), map to a lightweight TopEntry
        return this.sortBuilder(ExampleData.class)
                .memoryCache(this.cache.values())
                .descClause("score")
                .descClause("points")
                .limit(limit)
                .build(data -> new ExampleTopEntry(data.getId(), data.getScore(), data.getDisplayName()));
    }

    public List<ExampleData> refreshLeaderboard(int limit) {
        // 1. Flush cache → DB
        this.saveAll();
        // 2. Reload top N fresh from DB (partial projection), ORDER BY score DESC
        return this.sortBuilder(ExampleData.class)
                .descClause("score")
                .loadColumns("score", "display_name")
                .limit(limit)
                .build();
    }

    /** Lightweight leaderboard entry — avoids exposing full ExampleData. */
    public record ExampleTopEntry(String id, long score, String displayName) {}

    // -------------------------------------------------------------------------
    // Cache accessor (used by adapters to pre-load)
    // -------------------------------------------------------------------------

    public Map<String, ExampleData> getCache() {
        return cache;
    }

    // -------------------------------------------------------------------------
    // Read operations (served from cache with lazy backend fallback)
    // -------------------------------------------------------------------------

    /**
     * Returns a {@link CompletableFuture} that resolves to the cached entry for
     * {@code id}, loading from the backend if absent.
     */
    public CompletableFuture<ExampleData> getCached(String id) {
        ExampleData cached = cache.get(id);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        return async(() -> {
            ExampleData loaded = this.deserializer(String.class, ExampleData.class)
                    .id(id)
                    .build()
                    .stream()
                    .findFirst()
                    .orElse(null);
            if (loaded != null) {
                cache.put(id, loaded);
            }
            return loaded;
        });
    }

    /** Returns the cached data or {@link Optional#empty()} if not loaded. */
    public Optional<ExampleData> getIfPresent(String id) {
        return Optional.ofNullable(cache.get(id));
    }

    /**
     * Returns the top {@code max} players sorted by score descending.
     * Derived from the in-memory cache — works for every backend.
     */
    public List<ExampleData> getTopByScore(int max) {
        return cache.values().stream()
                .sorted(Comparator.comparingLong(ExampleData::getScore).reversed())
                .limit(max)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Write operations (write-through: cache + backend)
    // -------------------------------------------------------------------------

    /** Write-through save of a single record (builder API). */
    public void save(ExampleData data) {
        cache.put(data.getId(), data);
        this.serializer(ExampleData.class)
                .data(data)
                .flush();
    }

    public void loadAndHandle(List<String> ids, Consumer<ExampleData> consumer) {
        this.deserializer(String.class, ExampleData.class)
                .ids(ids)
                .load(data -> {
                    cache.put(data.getId(), data);
                    consumer.accept(data);
                });
    }

    /**
     * Batch write-through save (builder API).
     */
    public List<Integer> bulkSave(List<ExampleData> dataList) {
        dataList.forEach(d -> cache.put(d.getId(), d));
        return this.serializer(ExampleData.class)
                .datas(dataList)
                .columnFilters("points", "score")
                .flush();
    }

    /**
     * Batch load from backend into cache (builder API).
     */
    public void bulkLoad(List<String> ids) {
        List<String> missing = ids.stream().filter(id -> !cache.containsKey(id)).toList();
        if (missing.isEmpty()) return;
        List<ExampleData> loaded = this.deserializer(String.class, ExampleData.class)
                .ids(missing)
                .columnFilters("points", "score")
                .build();
        loaded.forEach(d -> cache.put(d.getId(), d));
    }

    /**
     * Batch load with mapper function — extracts specific fields without loading full entities.
     */
    public List<Integer> bulkLoadPoints(List<String> ids) {
        return this.deserializer(String.class, ExampleData.class)
                .ids(ids)
                .build(ExampleData::getPoints);
    }

    /** Removes a player's data from cache (does NOT delete from backend). */
    public void evict(String id) {
        cache.remove(id);
    }

    /** Removes a player's data from cache AND deletes it from the backend. */
    public void delete(String id) {
        cache.remove(id);
        this.getStore(ExampleData.class).delete(id);
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override
    public void saveAll() {
        this.flushEntireData(cache, ExampleData.class);
    }

    public List<ExampleTopEntry> getTop10To20() {
        // 0-based offset: 9 => starts at rank 10, then 10 rows => rank 10..19
        return this.sortBuilder(ExampleData.class)
                .descClause("score")
                .window(9, 10)
                .loadColumns("score", "display_name")
                .build(data -> new ExampleTopEntry(data.getId(), data.getScore(), data.getDisplayName()));
    }

    public int getLeaderboardPosition(ExampleData data) {
        return this.rankBuilder(ExampleData.class)
                .data(data)
                .rankField("leaderboardRank")
                .build();
    }

    public void updateLeaderboardPosition(ExampleData data) {
        this.rankUpdater(ExampleData.class)
                .data(data)
                .filtering_ranks("leaderboardRank")
                .update();
    }

    public void updateLeaderboardPositions(List<ExampleData> dataList) {
        this.rankUpdater(ExampleData.class)
                .dataCollection(dataList)
                .filtering_ranks("leaderboardRank")
                .update();
    }
}
