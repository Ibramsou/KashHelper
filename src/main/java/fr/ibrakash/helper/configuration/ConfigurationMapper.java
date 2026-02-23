package fr.ibrakash.helper.configuration;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;

public class ConfigurationMapper<V extends ConfigurationObject> {

    private final Class<V> clazz;
    private AbstractConfigurationLoader<CommentedConfigurationNode> loader;
    private final Path path;
    private final ConfigurationLoaderType type;
    private CommentedConfigurationNode config;
    private final Consumer<TypeSerializerCollection.Builder> serializers;
    private V object;

    public ConfigurationMapper(ConfigurationLoaderType type, Class<V> clazz, Path path, Consumer<TypeSerializerCollection.Builder> serializers) {
        this.type = type;
        this.clazz = clazz;
        this.serializers = serializers == null ? ConfigurationUtils.BUKKIT_SERIALIZERS : serializers;
        this.path = path;
        this.updateLoader();
    }

    private void updateLoader() {
        this.loader = this.type.get(path, serializers);

        try {
            if (Files.notExists(path)) {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            }
            this.config = Objects.requireNonNull(this.loader).load();
        } catch (IOException exception) {
            throw new IllegalArgumentException("An error occurred while loading configuration: " + exception.getMessage(), exception);
        }
    }

    public void reload() {
        boolean shouldSave = false;
        try {
            if (this.object != null) {
                this.object.setSerializableObjectOutdated(true);
            } else {
                shouldSave = true;
            }

            this.updateLoader();
            this.object = this.config.get(this.clazz);
        } catch (SerializationException e) {
            throw new RuntimeException("An error occurred while reloading configuration: " + e.getMessage(), e);
        } finally {
            if (shouldSave) {
                this.save();
            }
        }
    }

    public void save() {
        try {
            this.loader.save(this.config);
        } catch (ConfigurateException exception) {
            throw new RuntimeException("An error occurred while saving configuration: " + exception.getMessage(), exception);
        }
    }

    public V getObject() {
        if (this.object == null) this.reload();
        return this.object;
    }
}
