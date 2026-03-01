package fr.ibrakash.helper.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class BukkitExecutors {
    
    public static <T> CompletableFuture<T> hopToMain(CompletableFuture<T> future, Plugin plugin, boolean keepAsync) {
        return keepAsync ? future :
                future.thenApplyAsync(v -> v, r -> Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> r.run()));
    }

    public static <T> CompletableFuture<T> hopToMain(CompletableFuture<T> future, Plugin plugin) {
        return hopToMain(future, plugin, false);
    }

    public static void ensureAsync(Executor executor, Runnable runnable) {
        if (Bukkit.isPrimaryThread()) {
            executor.execute(runnable);
            return;
        }

        runnable.run();
    }

    public static void ensureAsync(Runnable runnable, Plugin plugin) {
        if (Bukkit.isPrimaryThread()) {
            Bukkit.getServer().getAsyncScheduler().runNow(plugin, scheduledTask -> runnable.run());
            return;
        }

        runnable.run();
    }
}