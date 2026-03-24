package fr.ibrakash.helper.platform;

import java.io.File;
import java.nio.file.Path;
import java.util.function.BiConsumer;

public class KashPaths {

    private final KashAddon<?> addon;

    public KashPaths(KashAddon<?> addon) {
        this.addon = addon;
    }

    public void deleteFile(String folder, String fileName, String extension) {
        final File file = new File(this.addon.getAddonFolder(), folder + "/" + fileName.toLowerCase() + "." + extension);
        if (!file.exists()) return;
        file.delete();
    }

    public void listFiles(String folder, String extension, BiConsumer<String, File> consumer) {
        File lootboxFolder = new File(this.addon.getAddonFolder(), folder);
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

    public Path get(String name, String extension, String... parents) {
        Path path = this.addon.getAddonFolder().toPath();
        for (String parent : parents) {
            path = path.resolve(parent);
        }

        return path.resolve(name + "." + extension);
    }

    public Path get(String name, String extension) {
        return this.addon.getAddonFolder().toPath().resolve(name + "." + extension);
    }
}
