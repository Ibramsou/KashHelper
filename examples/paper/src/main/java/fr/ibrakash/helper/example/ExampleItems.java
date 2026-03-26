package fr.ibrakash.helper.example;

import fr.ibrakash.helper.paper.configuration.readers.ConfigurationItems;
import fr.ibrakash.helper.platform.KashAddon;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class ExampleItems extends ConfigurationItems {

    public static ExampleItems get() {
        return ExamplePlugin.getInstance().getExampleItems();
    }

    protected ExampleItems(KashAddon<JavaPlugin> addon) {
        super(addon);
    }

    @Override
    public String key() {
        return "items";
    }
}
