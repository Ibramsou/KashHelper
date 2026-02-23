package fr.ibrakash.helper.chunk.entity;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class EntityChunkTrackingCache {

    private static final Table<JavaPlugin, UUID, LivingEntity> TRACKING_ENTITIES = HashBasedTable.create();
    private static final Table<JavaPlugin, Chunk, Integer> TRACKING_CHUNKS = HashBasedTable.create();

    public static void unload(JavaPlugin plugin) {
        TRACKING_ENTITIES.row(plugin).clear();
        TRACKING_CHUNKS.row(plugin).keySet().forEach(chunk -> chunk.removePluginChunkTicket(plugin));
    }

    public static void load(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(new EntityChunkListener(plugin), plugin);
    }

    public static void trackEntity(JavaPlugin plugin, final LivingEntity entity) {
        TRACKING_ENTITIES.put(plugin, entity.getUniqueId(), entity);
        trackChunk(plugin, entity.getChunk());
    }

    public static void unTrackEntity(JavaPlugin plugin, final LivingEntity entity) {
        if (entity == null) return;
        LivingEntity livingEntity = TRACKING_ENTITIES.remove(plugin, entity.getUniqueId());
        if (livingEntity == null) return;
        unTrackChunk(plugin, entity.getChunk());
    }

    protected static void handleEntityMovement(JavaPlugin plugin, Entity entity, Location from, Location to) {
        if (from.getChunk().equals(to.getChunk())) return;
        final LivingEntity livingEntity = TRACKING_ENTITIES.get(plugin, entity.getUniqueId());
        if (livingEntity == null) return;
        unTrackChunk(plugin, from.getChunk());
        trackChunk(plugin, to.getChunk());
    }

    protected static void removeEntityIfTracked(JavaPlugin plugin, Entity entity) {
        LivingEntity livingEntity = TRACKING_ENTITIES.remove(plugin, entity.getUniqueId());
        if (livingEntity == null) return;
        unTrackChunk(plugin, livingEntity.getChunk());
    }

    private static void trackChunk(JavaPlugin plugin, Chunk chunk) {
        int result = TRACKING_CHUNKS.row(plugin).merge(chunk, 1, Integer::sum);
        if (result != 1) return; // If a result equals 1, it means that chunk need to be tracked
        chunk.addPluginChunkTicket(plugin);
    }

    private static void unTrackChunk(JavaPlugin plugin, Chunk chunk) {
        int result = TRACKING_CHUNKS.row(plugin).merge(chunk, -1, Integer::sum);
        if (result > 0) return;
        TRACKING_CHUNKS.remove(plugin, chunk);
        chunk.removePluginChunkTicket(plugin);
    }
}
