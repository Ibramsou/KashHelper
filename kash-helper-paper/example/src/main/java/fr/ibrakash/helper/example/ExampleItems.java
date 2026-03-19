package fr.ibrakash.helper.example;

import fr.ibrakash.helper.paper.configuration.readers.ConfigurationItems;
import fr.ibrakash.helper.platform.KashAddon;
import org.bukkit.plugin.java.JavaPlugin;

public class ExampleItems extends ConfigurationItems {

    public static ExampleItems get() {
        return ExamplePlugin.getInstance().getExampleItems();
    }

    protected ExampleItems(KashAddon<JavaPlugin> addon) {
        super(addon);

        this.reload();
    }

    @Override
    public String key() {
        return "items";
    }
}
