package fr.ibrakash.helper.chain;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

public class DamageChainManager {

    private static final Map<UUID, DamageChainData> damageChainmap = new HashMap<>();

    public static void chainDamageEntities(Player damager, Collection<LivingEntity> victims, double damage, BiConsumer<DamageChainData, EntityDamageByEntityEvent> entityDamaged) {
        if (damageChainmap.containsKey(damager.getUniqueId())) return; // Prevent doubloons
        final DamageChainData data = new DamageChainData(damager, victims.iterator(), entityDamaged, damage);
        damageChainmap.put(damager.getUniqueId(), data);
        data.sendNextDamage();
    }

    public static boolean handleCurrentChainDamage(EntityDamageByEntityEvent event) {
        DamageChainData data = damageChainmap.get(event.getDamager().getUniqueId());
        if (data == null) return false;
        if (Bukkit.getCurrentTick() - data.getStartTick() >= 20L) { // Just a security
            damageChainmap.remove(event.getDamager().getUniqueId());
            return false;
        }
        if (!event.getEntity().equals(data.getCurrentDamagingEntity())) return false; // Another security
        data.handleDamageEvent(event);
        if (data.isStopped()) {
            damageChainmap.remove(event.getDamager().getUniqueId());
        }
        return true;
    }
}
