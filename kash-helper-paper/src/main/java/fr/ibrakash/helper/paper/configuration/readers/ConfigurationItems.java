package fr.ibrakash.helper.paper.configuration.readers;

import fr.ibrakash.helper.paper.configuration.objects.item.ConfigGuiItem;
import fr.ibrakash.helper.paper.configuration.objects.item.ConfigItem;
import fr.ibrakash.helper.configuration.readers.DualConfigurationReader;
import fr.ibrakash.helper.platform.KashAddon;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ConfigurationItems extends DualConfigurationReader<ConfigItem, ConfigGuiItem> {

    private final Map<Character, ConfigGuiItem> shapeItems = new HashMap<>();

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
        return this.shapeItems.get(character);
    }

    @Override
    public void reload() {
        this.shapeItems.clear();
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
    public boolean preLoad() {
        return true;
    }
}
