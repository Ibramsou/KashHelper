package fr.ibrakash.helper.jda.example;

import fr.ibrakash.helper.jda.configuration.readers.JdaEmbedConfigurationLocale;
import fr.ibrakash.helper.platform.KashAddon;

public class ExampleJdaEmbedLocale extends JdaEmbedConfigurationLocale {

    public ExampleJdaEmbedLocale(KashAddon<?> addon) {
        super(addon);
    }

    @Override
    public String key() {
        return "example-jda-locale";
    }
}

