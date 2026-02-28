package fr.ibrakash.helper.example;

import fr.ibrakash.helper.configuration.ConfigurationItems;

public class ExampleItems extends ConfigurationItems {

    private static ExampleItems instance;

    public static ExampleItems get() {
        if (instance == null) instance = new ExampleItems();
        return instance;
    }

    @Override
    public String key() {
        return "items";
    }
}
