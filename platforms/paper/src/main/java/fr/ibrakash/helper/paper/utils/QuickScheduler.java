package fr.ibrakash.helper.paper.utils;

import fr.ibrakash.helper.paper.KashPaperAddon;
import fr.ibrakash.helper.platform.KashAddon;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class QuickScheduler {

    public static QuickScheduler of(KashAddon<JavaPlugin> addon) {
        if (addon instanceof KashPaperAddon kashPaperAddon) {
            return kashPaperAddon.scheduler();
        }
        return new QuickScheduler(addon.getRaw());
    }

    private final Plugin plugin;
    private final boolean folia;

    public QuickScheduler(Plugin plugin) {
        this.plugin = plugin;
        this.folia = isFolia();
    }

    private boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public void runSync(Runnable task) {
        if (folia) {
            Bukkit.getGlobalRegionScheduler().execute(plugin, task);
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    public void runLater(Runnable task, long delayTicks, boolean adjustFoliaMillis) {
        if (folia) {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> task.run(), adjust(delayTicks, adjustFoliaMillis));
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    public void runTimer(Runnable task, long delayTicks, long periodTicks, boolean adjustFoliaMillis) {
        if (folia) {
            Bukkit.getGlobalRegionScheduler().runAtFixedRate(
                    plugin,
                    scheduledTask -> task.run(),
                    adjust(delayTicks, adjustFoliaMillis),
                    adjust(periodTicks, adjustFoliaMillis)
            );
        } else {
            Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
        }
    }

    public void runAsync(Runnable task) {
        if (folia) {
            Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    public void runAsyncLater(Runnable task, long delayTicks, boolean adjustFoliaMillis) {
        if (folia) {
            Bukkit.getAsyncScheduler().runDelayed(plugin, scheduledTask -> task.run(), adjust(delayTicks, adjustFoliaMillis), TimeUnit.MILLISECONDS);
        } else {
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks);
        }
    }

    public void runAsyncTimer(Runnable task, long delayTicks, long periodTicks, boolean adjustFoliaMillis) {
        if (folia) {
            Bukkit.getAsyncScheduler().runAtFixedRate(
                    plugin,
                    scheduledTask -> task.run(),
                    adjust(delayTicks, adjustFoliaMillis),
                    adjust(periodTicks, adjustFoliaMillis),
                    TimeUnit.MILLISECONDS
            );
        } else {
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
        }
    }

    public void runPlayerPendingTasks(Player player, Queue<Runnable> syncTasks) {
        this.runPlayerTask(player, () -> {
            Runnable runnable;
            while ((runnable = syncTasks.poll()) != null) runnable.run();
        });
    }

    public void runPlayerTask(Player player, Runnable task) {
        if (folia) {
            player.getScheduler().execute(plugin, task, null, 1L);
        } else {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, task);
        }
    }

    private long adjust(long ticks, boolean adjust) {
        return adjust ? ticks * 50L : ticks;
    }
}
