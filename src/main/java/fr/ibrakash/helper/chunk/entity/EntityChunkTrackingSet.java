package fr.ibrakash.helper.chunk.entity;

import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;

public class EntityChunkTrackingSet<V extends LivingEntity> extends HashSet<V> {

    protected final JavaPlugin plugin;

    public EntityChunkTrackingSet(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean add(V v) {
        boolean add = super.add(v);
        if (add) {
            EntityChunkTrackingCache.trackEntity(this.plugin, v);
        }
        return add;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        boolean removed = super.remove(o);
        if (removed) {
            EntityChunkTrackingCache.unTrackEntity(this.plugin, (V) o);
        }

        return removed;
    }

    @Override
    public void clear() {
        this.forEach(this::remove);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends V> c) {
        c.forEach(this::add);
        return true;
    }
}
