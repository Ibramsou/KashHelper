package fr.ibrakash.helper.configuration;

import fr.ibrakash.helper.configuration.objects.action.ConfigGroupAction;
import fr.ibrakash.helper.configuration.serialization.ActionSerializer;
import fr.ibrakash.helper.configuration.serialization.SoundSerializer;
import fr.ibrakash.helper.configuration.serialization.WorldSerializer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.nio.file.Files.writeString;

public class ConfigurationUtils {

    private static final Map<Class<?>, ConfigurationMapper<?>> UNIQUE_MAPPERS = new HashMap<>();
    public static final Consumer<TypeSerializerCollection.Builder> BUKKIT_SERIALIZERS = builder -> {
        builder.register(World.class, WorldSerializer.get());
        builder.register(Sound.class, SoundSerializer.get());
        builder.register(ConfigGroupAction.class, ActionSerializer.get());
        builder.registerAnnotatedObjects(ObjectMapper.factory());
    };

    public static CommentedConfigurationNode loadReadOnlyNode(
            JavaPlugin plugin,
            ConfigurationLoaderType type,
            Path userPath) {
        final AbstractConfigurationLoader<CommentedConfigurationNode> loader = type.get(userPath, BUKKIT_SERIALIZERS);

        try {
            Files.createDirectories(userPath.getParent());

            String resourceFileName =  userPath.getFileName().toString();

            // 1) Ensure file exists (premier run)
            if (Files.notExists(userPath)) {
                ConfigurationResourceUtils.copyResourceTo(plugin, resourceFileName, userPath);
            }

            // 2) Load user node
            CommentedConfigurationNode userNode = loader.load();

            // 3) Load defaults node from resource (temp copy)
            ConfigurationResourceUtils.ResourceSnapshot snapshot = ConfigurationResourceUtils.loadResourceSnapshot(plugin, type, resourceFileName);

            // 4) Check hash change to avoid doing merge every reload
            Path hashFile = userPath.getParent().resolve(".defaults").resolve(resourceFileName + ".sha256");
            String previousHash = ConfigurationResourceUtils.readIfExists(hashFile);
            boolean resourceChanged = previousHash == null || !previousHash.equals(snapshot.sha256());

            boolean changed = false;
            if (resourceChanged) {
                changed = ConfigurationResourceUtils.mergeMissingOnly(userNode, snapshot.node());
                if (!hashFile.toFile().exists()) {
                    hashFile.toFile().getParentFile().mkdirs();
                    hashFile.toFile().createNewFile();
                }
                // Save hash for next time
                writeString(hashFile, snapshot.sha256());
            }

            if (changed) {
                loader.save(userNode);
            }

            return userNode;
        } catch (IOException e) {
            throw new IllegalArgumentException("An error occurred while loading/updating configuration: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <V extends ConfigurationObject> ConfigurationMapper<V> getUniqueMapper(ConfigurationLoaderType type, Class<V> clazz, Supplier<Path> supplier, Consumer<TypeSerializerCollection.Builder> serializers) {
        return (ConfigurationMapper<V>) UNIQUE_MAPPERS.computeIfAbsent(clazz, aClass -> new ConfigurationMapper<>(type, clazz, supplier.get(), serializers));
    }

    public static <V extends ConfigurationObject> V getUniqueInstance(ConfigurationLoaderType type, V currentInstance, Class<V> clazz, Supplier<Path> supplier, Consumer<V> consumeNewInstance) {
        return getUniqueInstance(type, currentInstance, clazz, supplier, consumeNewInstance, null);
    }

    public static <V extends ConfigurationObject> V getUniqueInstance(ConfigurationLoaderType type, V currentInstance, Class<V> clazz, Supplier<Path> supplier, Consumer<V> consumeNewInstance, Consumer<TypeSerializerCollection.Builder> serializers) {
        if (currentInstance == null || currentInstance.isSerializableObjectOutdated()) {
            V result = getUnique(type, clazz, supplier, serializers);
            consumeNewInstance.accept(result);
            return result;
        }

        return currentInstance;
    }

    public static <V extends ConfigurationObject> V getUnique(ConfigurationLoaderType type, Class<V> clazz, Supplier<Path> supplier) {
        return getUnique(type, clazz, supplier, null);
    }

    public static <V extends ConfigurationObject> V getUnique(ConfigurationLoaderType type, Class<V> clazz, Supplier<Path> supplier, Consumer<TypeSerializerCollection.Builder> serializers) {
        return getUniqueMapper(type, clazz, supplier, serializers).getObject();
    }

    public static void reloadUniqueMappers() {
        UNIQUE_MAPPERS.values().forEach(ConfigurationMapper::reload);
    }
}
