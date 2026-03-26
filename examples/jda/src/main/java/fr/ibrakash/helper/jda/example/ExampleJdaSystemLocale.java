package fr.ibrakash.helper.jda.example;

import fr.ibrakash.helper.jda.configuration.readers.JdaSystemLocale;
import fr.ibrakash.helper.platform.KashAddon;

/**
 * Example plain-text system locale backed by {@code system-locale.yml}.
 *
 * <p>The file is auto-created from the bundled default
 * ({@code jda-system-locale.yml}) on first start if absent.
 *
 * <p>Usage:
 * <pre>{@code
 * JdaExample.getInstance().systemLocale().reply("no-permission", event);
 * JdaExample.getInstance().systemLocale().send("error-occurred", channel);
 * }</pre>
 */
public class ExampleJdaSystemLocale extends JdaSystemLocale {

    public ExampleJdaSystemLocale(KashAddon<?> addon) {
        super(addon);
    }

    @Override
    public String key() {
        return "system-locale";
    }
}

