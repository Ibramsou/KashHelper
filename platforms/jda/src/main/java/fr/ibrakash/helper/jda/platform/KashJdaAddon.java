package fr.ibrakash.helper.jda.platform;

import fr.ibrakash.helper.configuration.ConfigReference;
import fr.ibrakash.helper.configuration.ConfigurationLoaderType;
import fr.ibrakash.helper.jda.command.JdaSlashCommand;
import fr.ibrakash.helper.jda.command.JdaSlashCommandManager;
import fr.ibrakash.helper.jda.command.ReloadSlashCommand;
import fr.ibrakash.helper.jda.configuration.readers.JdaEmbedConfigurationLocale;
import fr.ibrakash.helper.jda.console.JdaConsoleManager;
import fr.ibrakash.helper.jda.configuration.readers.JdaSystemLocale;
import fr.ibrakash.helper.jda.embed.PersistentEmbedManager;
import fr.ibrakash.helper.jda.logging.JdaBotLogger;
import fr.ibrakash.helper.jda.logging.JdaLogArchiver;
import fr.ibrakash.helper.jda.logging.JdaLoggingOutputStream;
import fr.ibrakash.helper.platform.KashAddon;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

/**
 * Self-bootstrapping base class for JDA bots.
 *
 * <p>Subclass this, implement at minimum {@link #createConfiguration()} and
 * {@link #getEmbedLocale()}, then call {@code new MyBot()} from
 * {@code main()} – everything else is wired up automatically.
 *
 * <pre>{@code
 * public class MyBot extends KashJdaAddon<MyConfig> {
 *
 *     public static void main(String[] args) { new MyBot(); }
 *
 *     public MyConfig createConfiguration() { return new MyConfig(); }
 *
 *     // optional overrides:
 *     public List<ListenerAdapter> startupListeners() { return List.of(new MyListener()); }
 *     public List<JdaSlashCommand>  slashCommands()   { return List.of(new PingCommand()); }
 *     public File file()                              { return new File("my-bot"); }
 * }
 * }</pre>
 *
 * @param <C> the configuration type (must extend {@link KashJdaConfig})
 */
public abstract class KashJdaAddon<C extends KashJdaConfig, EMB extends JdaEmbedConfigurationLocale, MSG extends JdaSystemLocale> extends KashAddon<JDA> {

    @SuppressWarnings("rawtypes")
    private static KashJdaAddon instance;

    @SuppressWarnings("unchecked")
    public static <A extends KashJdaAddon<?, ?, ?>> A getInstance() {
        return (A) instance;
    }

    private final File addonFolder;
    private ConfigReference<C> configReference;
    private EMB embedLocale;
    private MSG systemLocale;
    private JdaSlashCommandManager slashCommandManager;
    private JdaConsoleManager consoleManager;
    private final PersistentEmbedManager persistentEmbedManager;

    protected KashJdaAddon() {
        super(null); // JDA is set after build
        instance = this;
        this.addonFolder = resolveAddonFolder();

        // 1. Archive old logs
        if (archiveLogs()) {
            try { JdaLogArchiver.archiveLatestLogs(this.addonFolder.toPath().resolve("logs")); }
            catch (Exception e) { JdaBotLogger.warn("Log archiving failed: %s", e.getMessage()); }
        }

        // 2. Load configuration
        this.configReference = this.configurations().serializedConfig(
                configLoaderType(),
                this.createConfigClass(),
                () -> this.paths().get("config", configLoaderType().defaultExtension())
        );
        this.embedLocale = this.createEmbedLocale();
        this.systemLocale = this.createSystemLocale();
        this.configurations().reloadCachedMappers();

        // 3. Build JDA
        JDA jda = this.buildJda();
        this.raw = jda;
        this.persistentEmbedManager = new PersistentEmbedManager(jda);

        // Ensure framework locales exist on disk at startup.
        this.getSystemLocale().reload();

        // 4. Redirect System.out / System.err to logger
        if (redirectSystemStreams()) {
            System.setOut(new PrintStream(new JdaLoggingOutputStream(
                    LoggerFactory.getLogger("SystemOut"), JdaLoggingOutputStream.LogLevel.INFO), true));
            System.setErr(new PrintStream(new JdaLoggingOutputStream(
                    LoggerFactory.getLogger("SystemErr"), JdaLoggingOutputStream.LogLevel.ERROR), true));
            Thread.setDefaultUncaughtExceptionHandler((t, e) ->
                    LoggerFactory.getLogger("UncaughtException").error("Exception in thread {}", t.getName(), e));
        }

        // 5. Register slash commands — reload command is always present
        this.slashCommandManager = new JdaSlashCommandManager();
        this.slashCommandManager.register(new ReloadSlashCommand(this));
        this.slashCommands().forEach(this.slashCommandManager::register);
        String devGuildId = this.configReference.get().getDevGuildId();
        this.slashCommandManager.registerAll(jda, devGuildId);

        // 6. Console
        if (enableConsole()) {
            this.consoleManager = new JdaConsoleManager();
            this.setupConsole(this.consoleManager);
        }

        // 7. Shutdown hook
        if (installShutdownHook()) {
            Runtime.getRuntime().addShutdownHook(new JdaShutdownHook(this));
        }

        // 8. Done
        this.onReady(jda);
        JdaBotLogger.info("Bot is ready: %s", jda.getSelfUser().getAsTag());
    }

    /**
     * Constructor used when a JDA instance and addon folder are already available.
     */
    public KashJdaAddon(JDA jda, File addonFolder) {
        super(jda);
        this.addonFolder = addonFolder;
        this.persistentEmbedManager = new PersistentEmbedManager(jda);
    }

    /**
     * Creates the configuration object for this bot.
     * <p>Called once at startup before JDA is built.
     */
    public abstract C createConfiguration();

    public abstract EMB createEmbedLocale();

    public abstract MSG createSystemLocale();

    /**
     * Called after all configurations and locales have been reloaded.
     * Implement this to react to a reload: re-initialize managers, clear caches, etc.
     */
    public abstract void onReload();

    /**
     * Additional {@link ConfigReference} instances to reload alongside the primary config.
     *
     * <p>Override this to include any extra config files managed by this addon.
     * Create the references as fields (e.g. in {@link #onReady(JDA)}) and return them here.
     *
     * <pre>{@code
     * private ConfigReference<MyExtraConfig> extraConfig;
     *
     * public List<ConfigReference<?>> additionalConfigurations() {
     *     return List.of(extraConfig);
     * }
     * }</pre>
     */
    public List<ConfigReference<?>> additionalConfigurations() {
        return Collections.emptyList();
    }

    public EMB getEmbedLocale() {
        return this.embedLocale;
    }

    public MSG getSystemLocale() {
        return this.systemLocale;
    }

    /**
     * Returns the working directory for this bot.
     * Defaults to {@code new File(".")}.
     */
    public File file() {
        return new File(".");
    }

    /**
     * Listeners to register <em>before</em> JDA has connected.
     * Use these for presence/status listeners, etc.
     */
    public List<ListenerAdapter> startupListeners() {
        return Collections.emptyList();
    }

    /**
     * Slash commands to upload and handle.
     */
    public List<JdaSlashCommand> slashCommands() {
        return Collections.emptyList();
    }

    /**
     * Additional {@link GatewayIntent}s to enable beyond the defaults.
     */
    public List<GatewayIntent> intents() {
        return Collections.emptyList();
    }

    /**
     * Customise the raw {@link JDABuilder} before it is built.
     * Called after intents and startup listeners have been applied.
     */
    public void configureBuilder(JDABuilder builder) {}

    /**
     * Called after JDA is ready and all infrastructure has been set up.
     */
    public void onReady(JDA jda) {}

    /**
     * Called by the shutdown hook before JDA is shut down.
     * Override to save data, close connections, etc.
     */
    public void onShutdown() {}

    // -------------------------------------------------------------------------
    // Reload
    // -------------------------------------------------------------------------

    /**
     * Reloads all configurations, locales, and additional config references,
     * then calls {@link #onReload()}.
     *
     * <p>This is the method invoked by the built-in {@code /reload} slash command.
     */
    public final void reloadAllConfigurations() {
        JdaBotLogger.info("Reloading all configurations...");
        this.configReference.reload();
        this.embedLocale.reload();
        this.systemLocale.reload();
        this.additionalConfigurations().stream()
                .filter(ref -> ref != null)
                .forEach(ConfigReference::reload);
        this.onReload();
        JdaBotLogger.info("All configurations reloaded.");
    }

    /**
     * Register additional console commands.
     * Called after the default {@link JdaConsoleManager} is constructed.
     */
    public void setupConsole(JdaConsoleManager manager) {}

    /** Whether to start the console reader thread. Defaults to {@code true}. */
    public boolean enableConsole() { return true; }

    /** Whether to install the JVM shutdown hook. Defaults to {@code true}. */
    public boolean installShutdownHook() { return true; }

    /** Whether to redirect {@code System.out/err} to SLF4J. Defaults to {@code true}. */
    public boolean redirectSystemStreams() { return true; }

    /** Whether to archive previous log files at startup. Defaults to {@code true}. */
    public boolean archiveLogs() { return true; }

    /**
     * Configuration format to use for the bot config file.
     * Defaults to {@link ConfigurationLoaderType#YAML}.
     */
    public ConfigurationLoaderType configLoaderType() { return ConfigurationLoaderType.YAML; }

    @Override
    public File getAddonFolder() {
        return this.addonFolder;
    }

    public JDA getJda() {
        return this.raw;
    }

    public C getConfig() {
        return this.configReference.get();
    }

    public JdaSlashCommandManager getSlashCommandManager() {
        return this.slashCommandManager;
    }

    public JdaConsoleManager getConsoleManager() {
        return this.consoleManager;
    }

    public PersistentEmbedManager getPersistentEmbedManager() {
        return this.persistentEmbedManager;
    }

    @SuppressWarnings("unchecked")
    private Class<C> createConfigClass() {
        return (Class<C>) this.createConfiguration().getClass();
    }

    private File resolveAddonFolder() {
        File base = this.file();
        if (base == null) base = new File(".");
        return base.isAbsolute() ? base : base.getAbsoluteFile();
    }

    private JDA buildJda() {
        // Token comes from the loaded config file
        String token = this.configReference.get().getToken();

        JDABuilder builder = JDABuilder.createDefault(token);

        List<GatewayIntent> extraIntents = this.intents();
        if (!extraIntents.isEmpty()) {
            builder.enableIntents(extraIntents);
        }

        for (ListenerAdapter listener : this.startupListeners()) {
            builder.addEventListeners(listener);
        }

        this.configureBuilder(builder);

        try {
            return builder.build().awaitReady();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("JDA startup interrupted", e);
        }
    }
}
