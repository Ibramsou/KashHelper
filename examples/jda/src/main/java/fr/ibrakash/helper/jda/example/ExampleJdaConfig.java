package fr.ibrakash.helper.jda.example;

import fr.ibrakash.helper.jda.platform.KashJdaConfig;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

/**
 * Configuration for the JDA example bot.
 *
 * <p>The bot token is inherited from {@link KashJdaConfig} and serialized
 * as {@code token} in {@code config.yml}.  Add your own fields below.
 */
@ConfigSerializable
public class ExampleJdaConfig extends KashJdaConfig {

    /**
     * Convenience accessor – returns the config loaded by {@link JdaExample}.
     */
    public static ExampleJdaConfig get() {
        return JdaExample.getInstance().getConfig();
    }

    @Override
    public String defaultToken() {
        return "YOUR_DISCORD_BOT_TOKEN_HERE";
    }

    @Override
    public String defaultDevGuildId() {
        return "1479442680470835222";
    }
}
