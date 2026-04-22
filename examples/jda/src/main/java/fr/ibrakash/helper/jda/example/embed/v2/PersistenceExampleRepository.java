package fr.ibrakash.helper.jda.example.embed.v2;

import fr.ibrakash.helper.jda.example.JdaExample;
import fr.ibrakash.helper.persistence.adapter.DatabaseRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class PersistenceExampleRepository extends DatabaseRepository {

    private final Map<Long, PersistenceExample> cache = new ConcurrentHashMap<>();

    public PersistenceExampleRepository(JdaExample addon) {
        super(addon, addon.getConfig().getDatabase());
    }

    public void reload() {
        this.cache.clear();
        this.loadEntireData(this.cache, PersistenceExample.class);
    }

    public Optional<PersistenceExample> find(long id) {
        return Optional.ofNullable(this.cache.get(id));
    }

    public PersistenceExample getOrCreate(long channelId, long ownerId) {
        return this.findByChannelId(channelId)
                .orElseGet(() -> this.create(channelId, ownerId));
    }

    public PersistenceExample create(long channelId, long ownerId) {
        return new PersistenceExample(channelId, ownerId);
    }

    public void save(PersistenceExample embed) {
        this.save(embed, embed.id());
    }

    public void save(PersistenceExample embed, long previousMessageId) {
        long messageId = embed.id();
        if (messageId <= 0L) {
            throw new IllegalStateException("PersistenceExample requires a message id before save.");
        }

        if (previousMessageId > 0L && previousMessageId != messageId) {
            this.cache.remove(previousMessageId);
            this.getStore(PersistenceExample.class, Long.class).delete(previousMessageId);
        }

        this.removeStaleEntry(embed, messageId);
        this.cache.put(messageId, embed);
        this.getStore(PersistenceExample.class, Long.class).save(embed);
    }

    public void delete(PersistenceExample embed) {
        long id = embed.id();
        this.removeStaleEntry(embed, id);
        this.cache.remove(id);
        if (id > 0L) {
            this.getStore(PersistenceExample.class, Long.class).delete(id);
        }
    }

    @Override
    public void saveAll() {
        this.flushEntireData(this.cache, PersistenceExample.class);
    }

    private Optional<PersistenceExample> findByChannelId(long channelId) {
        return this.cache.values().stream()
                .filter(embed -> embed.getChannelId() == channelId)
                .findFirst();
    }

    private void removeStaleEntry(PersistenceExample embed, long messageId) {
        this.cache.entrySet().removeIf(entry -> entry.getValue() == embed && entry.getKey() != messageId);
    }
}




