package fr.ibrakash.helper.example.sql;

import fr.ibrakash.helper.example.ExampleConfig;
import fr.ibrakash.helper.persistence.adapter.DatabaseRepository;
import fr.ibrakash.helper.platform.KashAddon;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ExampleRepository extends DatabaseRepository {

    private final Map<String, ExampleData> cache = new ConcurrentHashMap<>();

    public ExampleRepository(KashAddon<JavaPlugin> addon) {
        super(addon, ExampleConfig.get().getDatabase());
    }

    public List<ExampleData> getTopProfiles() {
        return this.sortBuilder(ExampleData.class)
                .descClause("score")
                .build();
    }

    public List<ExampleData> getTop10Profiles() {
        return this.sortBuilder(ExampleData.class)
                .descClause("score")
                .loadColumns("score", "display_name")
                .limit(10)
                .build();
    }

    public List<ExampleTopEntry> getLeaderboardPage(int limit) {
        return this.sortBuilder(ExampleData.class)
                .memoryCache(this.cache.values())
                .descClause("score")
                .descClause("points")
                .limit(limit)
                .build(data -> new ExampleTopEntry(data.getId(), data.getScore(), data.getDisplayName()));
    }

    public List<ExampleData> refreshLeaderboard(int limit) {
        this.saveAll();
        return this.sortBuilder(ExampleData.class)
                .descClause("score")
                .loadColumns("score", "display_name")
                .limit(limit)
                .build();
    }

    public record ExampleTopEntry(String id, long score, String displayName) {}

    public Map<String, ExampleData> getCache() {
        return cache;
    }

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

    public Optional<ExampleData> getIfPresent(String id) {
        return Optional.ofNullable(cache.get(id));
    }

    public List<ExampleData> getTopByScore(int max) {
        return cache.values().stream()
                .sorted(Comparator.comparingLong(ExampleData::getScore).reversed())
                .limit(max)
                .toList();
    }

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

    public List<Integer> bulkSave(List<ExampleData> dataList) {
        dataList.forEach(d -> cache.put(d.getId(), d));
        return this.serializer(ExampleData.class)
                .datas(dataList)
                .columnFilters("points", "score")
                .flush();
    }

    public void bulkLoad(List<String> ids) {
        List<String> missing = ids.stream().filter(id -> !cache.containsKey(id)).toList();
        if (missing.isEmpty()) return;
        List<ExampleData> loaded = this.deserializer(String.class, ExampleData.class)
                .ids(missing)
                .columnFilters("points", "score")
                .build();
        loaded.forEach(d -> cache.put(d.getId(), d));
    }

    public List<Integer> bulkLoadPoints(List<String> ids) {
        return this.deserializer(String.class, ExampleData.class)
                .ids(ids)
                .build(ExampleData::getPoints);
    }

    public void evict(String id) {
        cache.remove(id);
    }

    public void delete(String id) {
        cache.remove(id);
        this.getStore(ExampleData.class).delete(id);
    }

    @Override
    public void saveAll() {
        this.flushEntireData(cache, ExampleData.class);
    }

    public List<ExampleTopEntry> getTop10To20() {
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
