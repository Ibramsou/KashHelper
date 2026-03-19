package fr.ibrakash.helper.example;

import fr.ibrakash.helper.paper.configuration.readers.PaperConfigurationLocale;
import fr.ibrakash.helper.platform.KashAddon;
import org.bukkit.plugin.java.JavaPlugin;

public class ExampleLocale extends PaperConfigurationLocale {

    public static ExampleLocale get() {
        return ExamplePlugin.getInstance().getExampleLocale();
    }

    protected ExampleLocale(KashAddon<JavaPlugin> addon) {
        super(addon);
        this.reload();
    }

    @Override
    public String key() {
        return "locale";
    }
}
