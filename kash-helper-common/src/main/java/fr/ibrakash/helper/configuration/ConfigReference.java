package fr.ibrakash.helper.configuration;

import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Small stateful wrapper over configuration mapping that keeps a cached object
 * and refreshes it automatically when mapper reload marks it outdated.
 */
public final class ConfigReference<V extends ConfigurationObject> {

    private final Configurations configurations;
    private final ConfigurationLoaderType type;
    private final Class<V> clazz;
    private final Supplier<Path> pathSupplier;
    private final Consumer<TypeSerializerCollection.Builder> serializers;

    private V cached;

    ConfigReference(
            Configurations configurations,
            ConfigurationLoaderType type,
            Class<V> clazz,
            Supplier<Path> pathSupplier,
            Consumer<TypeSerializerCollection.Builder> serializers
    ) {
        this.configurations = configurations;
        this.type = type;
        this.clazz = clazz;
        this.pathSupplier = pathSupplier;
        this.serializers = serializers;
    }

    public V get() {
        this.cached = this.configurations.reloadIfOutdated(
                this.type,
                this.cached,
                this.clazz,
                this.pathSupplier,
                value -> this.cached = value,
                this.serializers
        );
        return this.cached;
    }

    public V reload() {
        ConfigurationMapper<V> mapper = this.configurations.getUniqueMapper(
                this.type,
                this.clazz,
                this.pathSupplier,
                this.serializers
        );
        mapper.reload();
        this.cached = mapper.getObject();
        return this.cached;
    }

    public V peek() {
        return this.cached;
    }
}

