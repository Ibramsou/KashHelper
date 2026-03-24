package fr.ibrakash.helper.configuration;

import fr.ibrakash.helper.platform.KashAddon;
import fr.ibrakash.helper.platform.KashPlatform;
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

public class Configurations {

    @SafeVarargs
    public static Consumer<TypeSerializerCollection.Builder> composeSerializers(Consumer<TypeSerializerCollection.Builder>... serializers) {
        return builder -> {
            for (Consumer<TypeSerializerCollection.Builder> serializer : serializers) {
                if (serializer != null) {
                    serializer.accept(builder);
                }
            }
            builder.registerAnnotatedObjects(ObjectMapper.factory());
        };
    }

    public static Consumer<TypeSerializerCollection.Builder> createSerializer(Consumer<TypeSerializerCollection.Builder> config) {
        return composeSerializers(config);
    }

    public static final Consumer<TypeSerializerCollection.Builder> DEFAULT_SERIALIZERS = composeSerializers();

    // Backward compatible alias.
    public static final Consumer<TypeSerializerCollection.Builder> BUKKIT_SERIALIZERS = DEFAULT_SERIALIZERS;

    private static final Map<Class<?>, ConfigurationMapper<?>> UNIQUE_MAPPERS = new HashMap<>();

    private final ConfigurationResources resources;

    public Configurations(KashAddon<?> addon) {
        this.resources = new ConfigurationResources(addon);
    }

    public CommentedConfigurationNode loadReadOnlyNode(ConfigurationLoaderType type, Path userPath) {
        return this.loadReadOnlyNode(type, userPath, null);
    }

    public CommentedConfigurationNode loadReadOnlyNode(
            ConfigurationLoaderType type,
            Path userPath,
            Consumer<TypeSerializerCollection.Builder> extraSerializers
    ) {
        final AbstractConfigurationLoader<CommentedConfigurationNode> loader = type.get(
                userPath,
                composeSerializers(KashPlatform.get().getConfigSerializer(), extraSerializers)
        );

        try {
            Files.createDirectories(userPath.getParent());

            String resourceFileName = userPath.getFileName().toString();

            if (Files.notExists(userPath)) {
                this.resources.copyResourceTo(resourceFileName, userPath);
            }

            CommentedConfigurationNode userNode = loader.load();
            ConfigurationResources.ResourceSnapshot snapshot = this.resources.loadResourceSnapshot(type, resourceFileName);

            Path hashFile = userPath.getParent().resolve(".defaults").resolve(resourceFileName + ".sha256");
            String previousHash = ConfigurationResources.readIfExists(hashFile);
            boolean resourceChanged = previousHash == null || !previousHash.equals(snapshot.sha256());

            boolean changed = false;
            if (resourceChanged) {
                changed = ConfigurationResources.mergeMissingOnly(userNode, snapshot.node());
                if (!hashFile.toFile().exists()) {
                    hashFile.toFile().getParentFile().mkdirs();
                    hashFile.toFile().createNewFile();
                }
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
    public <V extends ConfigurationObject> ConfigurationMapper<V> getUniqueMapper(
            ConfigurationLoaderType type,
            Class<V> clazz,
            Supplier<Path> supplier,
            Consumer<TypeSerializerCollection.Builder> serializers
    ) {
        return (ConfigurationMapper<V>) UNIQUE_MAPPERS.computeIfAbsent(
                clazz,
                ignored -> new ConfigurationMapper<>(type, clazz, supplier.get(), serializers)
        );
    }

    public <V extends ConfigurationObject> V getUnique(
            ConfigurationLoaderType type,
            Class<V> clazz,
            Supplier<Path> supplier
    ) {
        return getUnique(type, clazz, supplier, null);
    }

    public <V extends ConfigurationObject> V getUnique(
            ConfigurationLoaderType type,
            Class<V> clazz,
            Supplier<Path> supplier,
            Consumer<TypeSerializerCollection.Builder> serializers
    ) {
        return getUniqueMapper(type, clazz, supplier, serializers).getObject();
    }

    public <V extends ConfigurationObject> V loadObject(
            ConfigurationLoaderType type,
            Class<V> clazz,
            Supplier<Path> supplier
    ) {
        return getUnique(type, clazz, supplier);
    }

    public <V extends ConfigurationObject> V loadObject(
            ConfigurationLoaderType type,
            Class<V> clazz,
            Supplier<Path> supplier,
            Consumer<TypeSerializerCollection.Builder> serializers
    ) {
        return getUnique(type, clazz, supplier, serializers);
    }

    public <V extends ConfigurationObject> V getUniqueInstance(
            ConfigurationLoaderType type,
            V currentInstance,
            Class<V> clazz,
            Supplier<Path> supplier,
            Consumer<V> consumeNewInstance
    ) {
        return getUniqueInstance(type, currentInstance, clazz, supplier, consumeNewInstance, null);
    }

    public <V extends ConfigurationObject> V getUniqueInstance(
            ConfigurationLoaderType type,
            V currentInstance,
            Class<V> clazz,
            Supplier<Path> supplier,
            Consumer<V> consumeNewInstance,
            Consumer<TypeSerializerCollection.Builder> serializers
    ) {
        if (currentInstance == null || currentInstance.isSerializableObjectOutdated()) {
            V result = getUnique(type, clazz, supplier, serializers);
            consumeNewInstance.accept(result);
            return result;
        }
        return currentInstance;
    }

    public <V extends ConfigurationObject> V reloadIfOutdated(
            ConfigurationLoaderType type,
            V currentInstance,
            Class<V> clazz,
            Supplier<Path> supplier,
            Consumer<V> consumeNewInstance
    ) {
        return reloadIfOutdated(type, currentInstance, clazz, supplier, consumeNewInstance, null);
    }

    public <V extends ConfigurationObject> V reloadIfOutdated(
            ConfigurationLoaderType type,
            V currentInstance,
            Class<V> clazz,
            Supplier<Path> supplier,
            Consumer<V> consumeNewInstance,
            Consumer<TypeSerializerCollection.Builder> serializers
    ) {
        return getUniqueInstance(type, currentInstance, clazz, supplier, consumeNewInstance, serializers);
    }

    public void reloadUniqueMappers() {
        UNIQUE_MAPPERS.values().forEach(ConfigurationMapper::reload);
    }

    public void reloadCachedMappers() {
        reloadUniqueMappers();
    }

    public <V extends ConfigurationObject> ConfigReference<V> serializedConfig(
            ConfigurationLoaderType type,
            Class<V> clazz,
            Supplier<Path> supplier
    ) {
        return serializedConfig(type, clazz, supplier, null);
    }

    public <V extends ConfigurationObject> ConfigReference<V> serializedConfig(
            ConfigurationLoaderType type,
            Class<V> clazz,
            Supplier<Path> supplier,
            Consumer<TypeSerializerCollection.Builder> serializers
    ) {
        return new ConfigReference<>(this, type, clazz, supplier, serializers);
    }
}
