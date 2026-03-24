package fr.ibrakash.helper.example;


import fr.ibrakash.helper.paper.configuration.readers.ConfigurationItems;
import fr.ibrakash.helper.paper.configuration.readers.ConfigurationMenus;
import fr.ibrakash.helper.platform.KashAddon;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class ExampleMenus extends ConfigurationMenus {

    public static ExampleMenus get() {
        return ExamplePlugin.getInstance().getExampleMenus();
    }

    protected ExampleMenus(KashAddon<JavaPlugin> addon) {
        super(addon);

        this.reload();
    }

    @Override
    public String key() {
        return "menus";
    }

    @Override
    public ConfigurationItems defaultItems() {
        return ExampleItems.get();
    }
}
