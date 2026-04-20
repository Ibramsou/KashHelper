package fr.ibrakash.helper.jda.example;

import fr.ibrakash.helper.jda.configuration.readers.JdaSystemLocale;
import fr.ibrakash.helper.platform.KashAddon;

public class ExampleJdaSystemLocale extends JdaSystemLocale {

    public ExampleJdaSystemLocale(KashAddon<?> addon) {
        super(addon);
    }

    @Override
    public String key() {
        return "system-locale";
    }
}

