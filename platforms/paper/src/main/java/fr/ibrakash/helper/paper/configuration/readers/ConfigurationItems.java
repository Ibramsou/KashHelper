package fr.ibrakash.helper.paper.configuration.readers;

import fr.ibrakash.helper.configuration.Configurations;
import fr.ibrakash.helper.paper.configuration.objects.item.ConfigGuiItem;
import fr.ibrakash.helper.paper.configuration.objects.item.ConfigItem;
import fr.ibrakash.helper.configuration.readers.DualConfigurationReader;
import fr.ibrakash.helper.paper.configuration.objects.action.ConfigGroupAction;
import fr.ibrakash.helper.paper.configuration.serializers.ActionSerializer;
import fr.ibrakash.helper.platform.KashAddon;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class ConfigurationItems extends DualConfigurationReader<ConfigItem, ConfigGuiItem> {

    private static final Consumer<TypeSerializerCollection.Builder> GUI_SERIALIZERS =
            Configurations.createSerializer(builder -> builder.register(ConfigGroupAction.class, ActionSerializer.get()));

    private Map<Character, ConfigGuiItem> shapeItems;

    protected ConfigurationItems(KashAddon<JavaPlugin> addon) {
        super(addon);
    }

    public ConfigItem getItem(String path) {
        return this.firstPathMap.get(path);
    }

    public ConfigGuiItem getGuiItem(String path) {
        return this.secondPathMap.get(path);
    }

    public ConfigGuiItem getShapeItem(Character character) {
        return this.shapeItems == null ? null : this.shapeItems.get(character);
    }

    @Override
    public void reload() {
        if (this.shapeItems == null) {
            this.shapeItems = new HashMap<>();
        } else {
            this.shapeItems.clear();
        }
        super.reload();
    }

    @Override
    public void readNode(String nodeName, ConfigurationNode node) throws SerializationException {
        // Guard: preload is recursive, but only root item nodes should be deserialized as items.
        if (!node.isMap() || node.node("item").virtual()) return;

        boolean gui = !node.node("shape-character").virtual();
        if (gui) {
            ConfigGuiItem configGuiItem = node.get(ConfigGuiItem.class);
            if (configGuiItem != null) {
                configGuiItem.setId(nodeName);
                this.secondPathMap.put(nodeName, configGuiItem);
                this.shapeItems.put(configGuiItem.getShapeCharacter(),  configGuiItem);
            } else throw new RuntimeException("Invalid item '" + nodeName);
        } else {
            ConfigItem configGuiItem = node.get(ConfigItem.class);
            if (configGuiItem != null) {
                this.firstPathMap.put(nodeName, configGuiItem);
            } else throw new RuntimeException("Invalid item '" + nodeName);
        }
    }

    @Override
    protected List<NodeFilter> nodeFilters() {
        return List.of((path, nodeName, node) -> !path.contains(".") && node.isMap() && !node.node("item").virtual());
    }

    @Override
    public ConfigItem fallbackKey() {
        return new ConfigItem();
    }

    @Override
    public ConfigGuiItem fallbackValue() {
        return new ConfigGuiItem();
    }

    @Override
    protected Consumer<TypeSerializerCollection.Builder> serializers() {
        return GUI_SERIALIZERS;
    }

    @Override
    public boolean preLoad() {
        return true;
    }
}
