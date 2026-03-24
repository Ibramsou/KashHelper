package fr.ibrakash.helper.paper.configuration.readers;

import fr.ibrakash.helper.configuration.Configurations;
import fr.ibrakash.helper.paper.configuration.objects.AbstractGuiConfig;
import fr.ibrakash.helper.paper.configuration.objects.action.ConfigGroupAction;
import fr.ibrakash.helper.paper.configuration.objects.gui.ConfigGui;
import fr.ibrakash.helper.paper.configuration.objects.gui.ConfigPagedGui;
import fr.ibrakash.helper.configuration.readers.DualConfigurationReader;
import fr.ibrakash.helper.paper.configuration.serializers.ActionSerializer;
import fr.ibrakash.helper.platform.KashAddon;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.util.function.Consumer;

public abstract class ConfigurationMenus extends DualConfigurationReader<ConfigPagedGui, ConfigGui> {

    private static final Consumer<TypeSerializerCollection.Builder> GUI_SERIALIZERS =
            Configurations.createSerializer(builder -> builder.register(ConfigGroupAction.class, ActionSerializer.get()));

    protected ConfigurationMenus(KashAddon<JavaPlugin> addon) {
        super(addon);
    }

    public ConfigPagedGui getPaged(String path) {
        return this.resolveKey(path);
    }

    public ConfigGui getNormal(String path) {
        return this.resolveValue(path);
    }

    public <V extends AbstractGuiConfig> V get(ConfigurationNode node, Class<V> clazz) throws SerializationException {
        V entry = node.get(clazz);
        if (entry == null) {
            throw new IllegalArgumentException("Invalid GUI configuration: " + node.getString());
        }

        entry.addDefaultItems(this.defaultItems());
        return entry;
    }

    @Override
    public ConfigPagedGui buildKey(ConfigurationNode node) throws SerializationException {
        return get(node, ConfigPagedGui.class);
    }

    @Override
    public ConfigGui buildValue(ConfigurationNode node) throws SerializationException {
        return get(node, ConfigGui.class);
    }

    @Override
    public ConfigPagedGui fallbackKey() {
        return new ConfigPagedGui();
    }

    @Override
    public ConfigGui fallbackValue() {
        return new ConfigGui();
    }

    @Override
    protected Consumer<TypeSerializerCollection.Builder> serializers() {
        return GUI_SERIALIZERS;
    }

    public abstract String key();

    public abstract ConfigurationItems defaultItems();
}
