package fr.ibrakash.helper.jda.example;

import fr.ibrakash.helper.jda.command.JdaSlashCommand;
import fr.ibrakash.helper.jda.configuration.readers.JdaConfigurationLocale;
import fr.ibrakash.helper.jda.configuration.readers.JdaSystemLocale;
import fr.ibrakash.helper.jda.platform.KashJdaAddon;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.List;

/**
 * Example JDA bot using the KashHelper JDA platform.
 */
public class JdaExample extends KashJdaAddon<ExampleJdaConfig, ExampleJdaEmbedLocale, ExampleJdaSystemLocale> {

    private JdaConfigurationLocale localeConfig;
    private JdaSystemLocale systemLocaleConfig;

    // -------------------------------------------------------------------------
    // Entry point
    // -------------------------------------------------------------------------

    public static void main(String[] args) {
        // Instancing JdaExample will automatically:
        //  - load / create configuration (config.yml next to the working directory)
        //  - archive old log files
        //  - build the JDA instance with the configured token
        //  - register startup listeners and slash commands
        //  - start the console reader thread
        //  - install a JVM shutdown hook
        new JdaExample();
    }

    // -------------------------------------------------------------------------
    // Static accessor (optional – useful to reach the instance from anywhere)
    // -------------------------------------------------------------------------

    public static JdaExample getInstance() {
        return KashJdaAddon.getInstance();
    }

    // -------------------------------------------------------------------------
    // Required overrides
    // -------------------------------------------------------------------------

    /**
     * Create and return the configuration object for this bot.
     * KashHelper will (de)serialize it automatically to/from {@code config.yml}.
     */
    @Override
    public ExampleJdaConfig createConfiguration() {
        return new ExampleJdaConfig();
    }

    // -------------------------------------------------------------------------
    // Optional overrides
    // -------------------------------------------------------------------------

    @Override
    public ExampleJdaEmbedLocale createEmbedLocale() {
        return new ExampleJdaEmbedLocale(this);
    }

    @Override
    public ExampleJdaSystemLocale createSystemLocale() {
        return new ExampleJdaSystemLocale(this);
    }

    @Override
    public List<JdaSlashCommand> slashCommands() {
        return List.of(
                new PingSlashCommand(this),
                new GuildInfoSlashCommand(this),
                new GuildInfoV2SlashCommand(this),
                new GuildInfoDmSlashCommand(this)
        );
    }

    /**
     * Listeners attached to JDA before the connection is established.
     * The JDA instance is accessible via {@link #getJda()} / {@link #getRaw()} after boot.
     */
    @Override
    public List<ListenerAdapter> startupListeners() {
        return List.of();
    }

    /**
     * Extra Gateway intents beyond JDA defaults.
     * If you need GUILD_MEMBERS, GUILD_MESSAGES, etc., add them here.
     */
    @Override
    public List<GatewayIntent> intents() {
        return List.of(
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MESSAGES
        );
    }

    /**
     * Called once JDA is ready and all infrastructure is set up.
     */
    @Override
    public void onReady(JDA jda) {
        // Slash command manager is already wired by KashJdaAddon.
    }

    /**
     * Called by the JVM shutdown hook before JDA is shut down.
     * Save data, close connections, etc.
     */
    @Override
    public void onShutdown() {
        // Persist any data before the JVM exits.
    }
}
