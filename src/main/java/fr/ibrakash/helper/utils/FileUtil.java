package fr.ibrakash.helper.utils;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Path;
import java.util.function.BiConsumer;

public class FileUtil {

    public static void deleteDataFile(JavaPlugin plugin, String folder, String fileName, String extension) {
        final File file = new File(plugin.getDataFolder(), folder + "/" + fileName.toLowerCase() + "." + extension);
        if (!file.exists()) return;
        file.delete();
    }

    public static void listDataFiles(JavaPlugin plugin, String folder, String extension, BiConsumer<String, File> consumer) {
        File lootboxFolder = new File(plugin.getDataFolder(), folder);
        if (!lootboxFolder.exists()) {
            lootboxFolder.mkdirs();
            return;
        }

        final String endFile = "." + extension;
        File[] files = lootboxFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(endFile));
        if (files == null) return;

        for (File file : files) {
            consumer.accept(file.getName().replace(endFile, ""), file);
        }
    }

    public static Path getPath(JavaPlugin plugin, String name, String extension, String... parents) {
        Path path = plugin.getDataFolder().toPath();
        for (String parent : parents) {
            path = path.resolve(parent);
        }

        return path.resolve(name + "." + extension);
    }

    public static Path getPath(JavaPlugin plugin, String name, String extension) {
        return plugin.getDataFolder().toPath().resolve(name + "." + extension);
    }
}
