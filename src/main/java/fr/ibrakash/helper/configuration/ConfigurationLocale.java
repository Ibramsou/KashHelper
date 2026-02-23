package fr.ibrakash.helper.configuration;

import fr.ibrakash.helper.configuration.objects.ConfigMessage;
import fr.ibrakash.helper.utils.FileUtil;
import fr.ibrakash.helper.utils.TextUtil;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ConfigurationLocale {

    private final Map<String, ConfigMessage> pathMap = new HashMap<>();
    private final String key;
    private ConfigurationNode node;

    protected ConfigurationLocale() {
        this.key = this.key();
    }

    public void reload(JavaPlugin plugin) {
        this.pathMap.clear();
        this.node = ConfigurationUtils.loadReadOnlyNode(plugin, ConfigurationLoaderType.YAML, FileUtil.getPath(plugin, this.key, "yml"));
    }

    public void broadcast(String path, Object... replacers) {
        this.get(path).broadcast(replacers);
    }

    public void send(CommandSender sender, String path, Object... replacers) {
        this.get(path).send(sender, replacers);
    }

    public Component serialized(String path, Object... replacers) {
        return TextUtil.replacedComponent(this.get(path).getMessage(), replacers);
    }

    public ConfigMessage get(String path) {
        return this.pathMap.computeIfAbsent(path, s -> {
            ConfigurationNode node = resolvePath(path);

            try {
                if (node.isMap()) {
                    ConfigMessage entry = node.get(ConfigMessage.class);
                    if (entry != null) {
                        return entry;
                    }

                    return new ConfigMessage("Not configured");
                } else if (node.isList()) {
                    List<String> lines = node.getList(String.class);
                    return new ConfigMessage((lines == null || lines.isEmpty()) ? "Not configured" : StringUtils.join(lines, "\n"));
                } else {
                    String value = node.getString();
                    return new ConfigMessage((value == null || value.isEmpty()) ? "Not configured" : value);
                }
            } catch (SerializationException e) {
                return new ConfigMessage("Not configured");
            }
        });
    }

    private ConfigurationNode resolvePath(String path) {
        String[] split = path.split("\\.");
        return this.node.node((Object[]) split);
    }

    public abstract String key();
}