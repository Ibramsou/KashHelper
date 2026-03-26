package fr.ibrakash.helper.configuration;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.configurate.xml.XmlConfigurationLoader;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public enum ConfigurationLoaderType {
    YAML((path, serializers) -> YamlConfigurationLoader
            .builder()
            .path(path)
            .nodeStyle(NodeStyle.BLOCK)
            .indent(2)
            .defaultOptions(options -> options.shouldCopyDefaults(true).serializers(serializers))
            .build()),
    HOCON((path, serializers) -> HoconConfigurationLoader.builder()
            .path(path)
            .indent(2)
            .defaultOptions(options -> options.shouldCopyDefaults(true).serializers(serializers))
            .build()),
    XML(ConfigurationLoaderType::buildXmlLoader);

    private final BiFunction<Path, Consumer<TypeSerializerCollection.Builder>, AbstractConfigurationLoader<CommentedConfigurationNode>> loader;

    ConfigurationLoaderType(BiFunction<Path, Consumer<TypeSerializerCollection.Builder>, AbstractConfigurationLoader<CommentedConfigurationNode>> loader) {
        this.loader = loader;
    }

    @SuppressWarnings("unchecked")
    private static AbstractConfigurationLoader<CommentedConfigurationNode> buildXmlLoader(
            Path path,
            Consumer<TypeSerializerCollection.Builder> serializers
    ) {
        return (AbstractConfigurationLoader<CommentedConfigurationNode>) (AbstractConfigurationLoader<?>) XmlConfigurationLoader.builder()
                .path(path)
                .indent(2)
                .defaultOptions(options -> options.shouldCopyDefaults(true).serializers(serializers))
                .build();
    }

    public AbstractConfigurationLoader<CommentedConfigurationNode> get(Path path, @NotNull Consumer<TypeSerializerCollection.Builder> serializers) {
        return this.loader.apply(path, serializers);
    }

    /**
     * Returns the default file extension for this configuration format (e.g. "yml", "conf", "xml").
     */
    public String defaultExtension() {
        return switch (this) {
            case YAML -> "yml";
            case HOCON -> "conf";
            case XML -> "xml";
        };
    }
}
