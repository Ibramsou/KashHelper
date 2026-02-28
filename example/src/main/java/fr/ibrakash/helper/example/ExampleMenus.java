package fr.ibrakash.helper.example;

import fr.ibrakash.helper.configuration.ConfigurationItems;
import fr.ibrakash.helper.configuration.ConfigurationMenus;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class ExampleMenus extends ConfigurationMenus {

    private static ExampleMenus instance;

    public static ExampleMenus get() {
        if (instance == null) instance = new ExampleMenus();
        return instance;
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
