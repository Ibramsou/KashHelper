package fr.ibrakash.helper.configuration;

import fr.ibrakash.helper.configuration.objects.AbstractGuiConfig;
import fr.ibrakash.helper.configuration.objects.gui.GuiConfig;
import fr.ibrakash.helper.configuration.objects.gui.PagedGuiConfig;
import fr.ibrakash.helper.utils.FileUtil;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.HashMap;
import java.util.Map;

public abstract class ConfigurationMenus {

    private final Map<String, PagedGuiConfig> pagedPathMap = new HashMap<>();
    private final Map<String, GuiConfig> normalPathMap = new HashMap<>();
    private final String key;
    private ConfigurationNode node;

    protected ConfigurationMenus() {
        this.key = this.key();
    }

    public void reload(JavaPlugin plugin) {
        this.pagedPathMap.clear();
        this.normalPathMap.clear();
        this.node = ConfigurationUtils.loadReadOnlyNode(plugin, ConfigurationLoaderType.YAML, FileUtil.getPath(plugin, this.key, "yml"));
    }

    public PagedGuiConfig getPaged(String path) {
        return get(this.pagedPathMap, path, PagedGuiConfig.class);
    }

    public GuiConfig getNormal(String path) {
        return this.get(this.normalPathMap, path, GuiConfig.class);
    }

    public <V extends AbstractGuiConfig> V get(Map<String, V> map, String path, Class<V> clazz) {
        return map.computeIfAbsent(path, s -> {
            ConfigurationNode node = resolvePath(path);
            try {
                if (node.isMap()) {
                    V entry = node.get(clazz);
                    if (entry == null) {
                        throw new IllegalArgumentException("Invalid GUI configuration: " + path);
                    }
                    return entry;
                } else {
                    throw new  IllegalArgumentException("Invalid GUI configuration: " + path);
                }
            } catch (SerializationException e) {
                throw new IllegalArgumentException("Invalid GUI configuration: " + path);
            }
        });
    }

    private ConfigurationNode resolvePath(String path) {
        String[] split = path.split("\\.");
        return this.node.node((Object[]) split);
    }

    public abstract String key();
}
