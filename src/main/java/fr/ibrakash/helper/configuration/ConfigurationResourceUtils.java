package fr.ibrakash.helper.configuration;

import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class ConfigurationResourceUtils {

    static void copyResourceTo(JavaPlugin plugin, String resourceFileName, Path dest) throws IOException {
        String resourcePath = "/" + resourceFileName;
        try (InputStream in = plugin.getClass().getResourceAsStream(resourcePath)) {
            if (in == null) {
                Files.createFile(dest);
                return;
            }
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    static ResourceSnapshot loadResourceSnapshot(
            JavaPlugin plugin,
            ConfigurationLoaderType type,
            String resourceFileName
    ) throws IOException {
        String resourcePath = "/" + resourceFileName;

        byte[] bytes;
        try (InputStream in = plugin.getClass().getResourceAsStream(resourcePath)) {
            if (in == null) {
                CommentedConfigurationNode empty = CommentedConfigurationNode.root();
                return new ResourceSnapshot(empty, "missing-resource");
            }
            bytes = in.readAllBytes();
        }

        String sha256 = sha256Hex(bytes);

        Path temp = Files.createTempFile("cfg-defaults-", "-" + resourceFileName);
        Files.write(temp, bytes, StandardOpenOption.TRUNCATE_EXISTING);

        try {
            AbstractConfigurationLoader<CommentedConfigurationNode> tempLoader = type.get(temp, ConfigurationUtils.BUKKIT_SERIALIZERS);
            CommentedConfigurationNode defaultsNode = tempLoader.load();
            return new ResourceSnapshot(defaultsNode, sha256);
        } finally {
            try { Files.deleteIfExists(temp); } catch (IOException ignored) {}
        }
    }

    static boolean mergeMissingOnly(CommentedConfigurationNode target, CommentedConfigurationNode defaults) {
        boolean[] changed = new boolean[]{false};
        mergeMissingOnly0(target, defaults, changed);
        return changed[0];
    }

    static void mergeMissingOnly0(ConfigurationNode target, ConfigurationNode defaults, boolean[] changed) {
        if (defaults.isMap()) {

            for (Object key : defaults.childrenMap().keySet()) {
                ConfigurationNode defChild = defaults.node(key);
                ConfigurationNode tgtChild = target.node(key);

                if (tgtChild.virtual()) {
                    try {
                        tgtChild.set(defChild);
                    } catch (SerializationException e) {
                        throw new RuntimeException(e);
                    }
                    changed[0] = true;
                } else {
                    mergeMissingOnly0(tgtChild, defChild, changed);
                }
            }
            return;
        }

        if (defaults.isList()) {
            if (target.virtual()) {
                try {
                    target.set(defaults);
                } catch (SerializationException e) {
                    throw new RuntimeException(e);
                }
                changed[0] = true;
            }
            return;
        }

        if (target.virtual()) {
            try {
                target.set(defaults.raw());
            } catch (SerializationException e) {
                throw new RuntimeException(e);
            }
            changed[0] = true;
        }
    }

    static String sha256Hex(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(bytes);
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    static String readIfExists(Path path) throws IOException {
        if (Files.notExists(path)) return null;
        return Files.readString(path, StandardCharsets.UTF_8).trim();
    }

    static void writeString(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public record ResourceSnapshot(CommentedConfigurationNode node, String sha256) {}

}
