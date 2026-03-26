package fr.ibrakash.helper.jda.platform;

import fr.ibrakash.helper.jda.logging.JdaBotLogger;
import net.dv8tion.jda.api.JDA;

/**
 * JVM shutdown hook that gracefully shuts down the JDA instance.
 *
 * <p>It is installed automatically by {@link KashJdaAddon} unless you override
 * {@link KashJdaAddon#installShutdownHook()} to return {@code false}.
 */
public class JdaShutdownHook extends Thread {

    private final KashJdaAddon<?, ?, ?> addon;

    public JdaShutdownHook(KashJdaAddon<?, ?, ?> addon) {
        super("jda-shutdown-hook");
        this.addon = addon;
    }

    @Override
    public void run() {
        JdaBotLogger.info("Shutting down bot…");
        this.addon.onShutdown();

        JDA jda = this.addon.getJda();
        if (jda != null) {
            JdaBotLogger.info("Shutting down JDA…");
            jda.shutdownNow();
            JdaBotLogger.info("JDA shut down.");
        }
    }
}

