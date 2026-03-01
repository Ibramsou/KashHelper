package fr.ibrakash.helper.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class BukkitExecutors {

    public static Executor mainThread(Plugin plugin) {
        return task -> {
            if (Bukkit.isPrimaryThread()) task.run();
            else Bukkit.getScheduler().runTask(plugin, task);
        };
    }
    
    public static <T> CompletableFuture<T> hopToMain(CompletableFuture<T> future, Plugin plugin, boolean keepAsync) {
        if (keepAsync) return future;
        return hopToMain(future, plugin);
    }

    public static <T> CompletableFuture<T> hopToMain(CompletableFuture<T> future, Plugin plugin) {
        Executor main = mainThread(plugin);
        return future.thenComposeAsync(CompletableFuture::completedFuture, main);
    }
}