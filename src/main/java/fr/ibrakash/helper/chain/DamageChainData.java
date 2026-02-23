package fr.ibrakash.helper.chain;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Iterator;
import java.util.function.BiConsumer;

public final class DamageChainData {

    private final long startTick = Bukkit.getCurrentTick();

    private final Player damager;
    private final Iterator<LivingEntity> victims;
    private final BiConsumer<DamageChainData, EntityDamageByEntityEvent> entityDamaged;
    private final double damage;
    private int count = 0;
    private boolean stopped;
    private LivingEntity currentDamagingEntity;

    public DamageChainData(Player damager, Iterator<LivingEntity> victims, BiConsumer<DamageChainData, EntityDamageByEntityEvent> entityDamaged, double damage) {
        this.damager = damager;
        this.victims = victims;
        this.entityDamaged = entityDamaged;
        this.damage = damage;
    }

    void handleDamageEvent(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) { // Ignore canceled events and send next damage
            this.sendNextDamage();
            return;
        }

        this.count++;
        this.entityDamaged.accept(this, event);
        if (this.stopped) return;
        this.sendNextDamage();
    }

    void sendNextDamage() {
        if (!this.victims.hasNext()) {
            this.stopped = true;
            return;
        }
        this.currentDamagingEntity = this.victims.next();
        this.currentDamagingEntity.damage(this.damage, this.damager);
    }

    public int getCount() {
        return count;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    public long getStartTick() {
        return startTick;
    }

    public LivingEntity getCurrentDamagingEntity() {
        return currentDamagingEntity;
    }
}
