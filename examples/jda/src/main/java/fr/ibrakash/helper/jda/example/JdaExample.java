package fr.ibrakash.helper.jda.example;

import fr.ibrakash.helper.jda.example.embed.v2.PersistenceExampleRepository;
import fr.ibrakash.helper.jda.example.embed.v2.PersistenceExampleSlashCommand;
import fr.ibrakash.helper.jda.logging.JdaBotLogger;
import fr.ibrakash.helper.jda.command.JdaSlashCommand;
import fr.ibrakash.helper.jda.platform.KashJdaAddon;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.List;

public class JdaExample extends KashJdaAddon<ExampleJdaConfig, ExampleJdaEmbedLocale, ExampleJdaSystemLocale> {

    private PersistenceExampleRepository persistenceExampleRepository;

    public static void main(String[] args) {
        new JdaExample();
    }

    public static JdaExample getInstance() {
        return KashJdaAddon.getInstance();
    }

    @Override
    public ExampleJdaConfig createConfiguration() {
        return new ExampleJdaConfig();
    }

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
                new GuildInfoDmSlashCommand(this),
                new PersistenceExampleSlashCommand(this)
        );
    }

    @Override
    public List<ListenerAdapter> startupListeners() {
        return List.of();
    }

    @Override
    public List<GatewayIntent> intents() {
        return List.of(
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MESSAGES
        );
    }

    @Override
    public boolean redirectSystemStreams() {
        return false;
    }

    @Override
    public void onReady(JDA jda) {
        if (this.getConfig() == null || this.getConfig().getDatabase() == null) {
            throw new IllegalStateException("Configuration was not initialized before onReady().");
        }
        JdaBotLogger.info("onReady called - persistence config initialized: %s", this.getConfig().getDatabase() != null);
        this.persistenceExampleRepository = new PersistenceExampleRepository(this);
        this.persistenceExampleRepository.reload();
        JdaBotLogger.info("PersistenceExampleRepository initialized");
    }

    @Override
    public void onShutdown() {
        if (this.persistenceExampleRepository != null) {
            this.persistenceExampleRepository.close();
        }
    }

    public PersistenceExampleRepository getPersistenceExampleRepository() {
        return this.persistenceExampleRepository;
    }

    public PersistenceExampleRepository requirePersistenceExampleRepository() {
        if (this.persistenceExampleRepository == null) {
            throw new IllegalStateException("PersistenceExampleRepository is not initialized yet.");
        }
        return this.persistenceExampleRepository;
    }
}
