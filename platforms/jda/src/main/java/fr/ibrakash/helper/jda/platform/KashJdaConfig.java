package fr.ibrakash.helper.jda.platform;

import fr.ibrakash.helper.configuration.ConfigurationObject;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

/**
 * Base configuration object for all JDA bots.
 *
 * <p>Subclass this, annotate with {@code @ConfigSerializable} and add your own fields.
 * The {@link #token} field is serialized as {@code token} in the config file.
 *
 * <pre>
 * &#64;ConfigSerializable
 * public class MyConfig extends KashJdaConfig {
 *     private int maxRetries = 3;
 * }
 * </pre>
 */
@ConfigSerializable
public abstract class KashJdaConfig extends ConfigurationObject {

    /** The bot token read from the config file. */
    private String token = defaultToken();

    /** The dev guild ID used to register slash commands on a specific guild for instant availability. */
    private String devGuildId = defaultDevGuildId();

    /**
     * Returns the bot token.  This is the value serialized under the {@code token} key
     * in the configuration file.  It is also used by {@link KashJdaAddon} at startup.
     */
    public String defaultToken() {
        return "";
    }

    /**
     * Returns the default dev guild ID.
     * Override this in your config subclass to provide a default value.
     * When set, slash commands are registered on this guild only (instant update)
     * instead of globally (which can take up to an hour).
     *
     * @return the dev guild ID, or empty string for global registration
     */
    public String defaultDevGuildId() {
        return "";
    }

    public String getToken() {
        return token;
    }

    /**
     * Returns the dev guild ID from the configuration.
     */
    public String getDevGuildId() {
        return devGuildId;
    }
}
