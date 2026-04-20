package fr.ibrakash.helper.jda.example.embed.v2;

import fr.ibrakash.helper.jda.example.JdaExample;
import fr.ibrakash.helper.persistence.adapter.DatabaseRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class PersistenceExampleRepository extends DatabaseRepository {

    private final Map<Long, PersistenceExample> cache = new ConcurrentHashMap<>();
    private final Map<Long, PersistenceExample> runtimeByChannel = new ConcurrentHashMap<>();

    public PersistenceExampleRepository(JdaExample addon) {
        super(addon, addon.getConfig().getDatabase());
    }

    public void reload() {
        this.cache.clear();
        this.runtimeByChannel.clear();
        this.loadEntireData(this.cache, PersistenceExample.class);
    }

    public Optional<PersistenceExample> find(long id) {
        return Optional.ofNullable(this.cache.get(id));
    }

    public PersistenceExample getOrCreate(long channelId, long ownerId) {
        return this.runtimeByChannel.computeIfAbsent(channelId, ignored -> this.create(channelId, ownerId));
    }

    public PersistenceExample create(long channelId, long ownerId) {
        return new PersistenceExample(channelId, ownerId);
    }

    public void save(PersistenceExample embed) {
        long id = embed.id();
        if (embed.initialChannelId() > 0L) {
            this.runtimeByChannel.putIfAbsent(embed.initialChannelId(), embed);
        }
        if (id > 0L) {
            this.cache.put(id, embed);
        }
        this.getStore(PersistenceExample.class, Long.class).save(embed);
    }

    public void delete(PersistenceExample embed) {
        long id = embed.id();
        this.cache.remove(id);
        if (id > 0L) {
            this.getStore(PersistenceExample.class, Long.class).delete(id);
        }
    }

    @Override
    public void saveAll() {
        this.flushEntireData(this.cache, PersistenceExample.class);
    }
}




