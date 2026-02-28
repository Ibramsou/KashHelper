package fr.ibrakash.helper.configuration;

import fr.ibrakash.helper.configuration.objects.AbstractConfigItem;
import fr.ibrakash.helper.configuration.objects.item.ConfigGuiItem;
import fr.ibrakash.helper.configuration.objects.item.ConfigItem;
import fr.ibrakash.helper.utils.FileUtil;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.HashMap;
import java.util.Map;

public abstract class ConfigurationItems {

    private final Map<String, ConfigItem> itemMap = new HashMap<>();
    private final Map<String, ConfigGuiItem> guiItemMap = new HashMap<>();
    private final Map<Character, ConfigGuiItem> shapeItems = new HashMap<>();

    private final String key;
    private ConfigurationNode node;

    protected ConfigurationItems() {
        this.key = this.key();
    }

    public void reload(JavaPlugin plugin) {
        this.itemMap.clear();
        this.guiItemMap.clear();
        this.shapeItems.clear();
        this.node = ConfigurationUtils.loadReadOnlyNode(plugin, ConfigurationLoaderType.YAML, FileUtil.getPath(plugin, this.key, "yml"));
        if (this.node.isMap()) {
            this.node.childrenMap().forEach((key, node) -> {
                boolean gui = !node.node("shape-character").virtual();
                String nodeName = String.valueOf(key);
                try {
                    if (gui) {
                        ConfigGuiItem configGuiItem = node.get(ConfigGuiItem.class);
                        if (configGuiItem != null) {
                            configGuiItem.setId(nodeName);
                            this.guiItemMap.put(nodeName, configGuiItem);
                            this.shapeItems.put(configGuiItem.getShapeCharacter(),  configGuiItem);
                        } else
                            throw new RuntimeException("Invalid item '" + nodeName);
                    } else {
                        ConfigItem configGuiItem = node.get(ConfigItem.class);
                        if (configGuiItem != null) {
                            this.itemMap.put(nodeName, configGuiItem);
                        } else
                            throw new RuntimeException("Invalid item '" + nodeName);
                    }
                } catch (SerializationException e) {
                    throw new RuntimeException("Invalid item '" + nodeName + "': " + e.getMessage(), e);
                }
            });
        }
    }

    public ConfigItem getItem(String path) {
        return this.itemMap.get(path);
    }

    public ConfigGuiItem getGuiItem(String path) {
        return this.guiItemMap.get(path);
    }

    public ConfigGuiItem getShapeItem(Character character) {
        return this.shapeItems.get(character);
    }

    public abstract String key();
}
