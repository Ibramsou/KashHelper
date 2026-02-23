package fr.ibrakash.helper.example;

import fr.ibrakash.helper.configuration.ConfigurationLocale;

public class ExampleLocale extends ConfigurationLocale {

    private static ExampleLocale instance;

    public static ExampleLocale get() {
        if (instance == null) instance = new ExampleLocale();
        return instance;
    }

    @Override
    public String key() {
        return "locale";
    }
}
