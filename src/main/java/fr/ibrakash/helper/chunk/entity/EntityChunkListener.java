package fr.ibrakash.helper.chunk.entity;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class EntityChunkListener implements Listener {

    private final JavaPlugin plugin;

    public EntityChunkListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMove(EntityMoveEvent event) {
        EntityChunkTrackingCache.handleEntityMovement(this.plugin, event.getEntity(), event.getFrom(), event.getTo());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDeath(EntityDeathEvent event) {
        EntityChunkTrackingCache.removeEntityIfTracked(this.plugin, event.getEntity());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onRemoveFromWorld(EntityRemoveFromWorldEvent event) {
        EntityChunkTrackingCache.removeEntityIfTracked(this.plugin, event.getEntity());
    }
}
