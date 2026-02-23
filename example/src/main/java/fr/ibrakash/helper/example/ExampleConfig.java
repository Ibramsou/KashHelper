package fr.ibrakash.helper.example;

import fr.ibrakash.helper.configuration.ConfigurationLoaderType;
import fr.ibrakash.helper.configuration.ConfigurationObject;
import fr.ibrakash.helper.configuration.ConfigurationUtils;
import fr.ibrakash.helper.configuration.objects.item.impl.ConfigItem;
import fr.ibrakash.helper.utils.FileUtil;
import org.bukkit.Material;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;

@ConfigSerializable
public class ExampleConfig extends ConfigurationObject {

    private static ExampleConfig instance;

    public static ExampleConfig get() {
        return ConfigurationUtils.getUniqueInstance(
                ConfigurationLoaderType.YAML,
                instance,
                ExampleConfig.class, () -> FileUtil.getPath(ExamplePlugin.getInstance(), "config", "yml"),
                config -> instance = config);
    }

    private List<ExampleObject> intervals = List.of(
            new ExampleObject(20 * 5, "example-1"),
            new ExampleObject(20 * 7, "example-2"),
            new ExampleObject(20 * 2, "example-3")
    );
    private ConfigItem configItem = new ConfigItem()
            .material(Material.DIAMOND)
            .displayName("<gold>Unique item.")
            .lore(List.of("<gray>This is %player%'s item."));

    public List<ExampleObject> getIntervals() {
        return intervals;
    }

    public ConfigItem getReplaceableItem() {
        return configItem;
    }
}


