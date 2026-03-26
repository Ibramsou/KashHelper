package fr.ibrakash.helper.jda.console;

import fr.ibrakash.helper.jda.logging.JdaBotLogger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Manages all {@link JdaConsoleCommand} instances and dispatches stdin input to them.
 *
 * <p>A reader thread is started automatically on construction.  The built-in
 * {@code exit} and {@code stop} keywords call {@code System.exit(0)}.
 *
 * <p>Usage inside a {@link fr.ibrakash.helper.jda.platform.KashJdaAddon}:
 * <pre>{@code
 * consoleManager = new JdaConsoleManager();
 * consoleManager.register(new MyCustomCommand());
 * }</pre>
 */
public class JdaConsoleManager {

    private final Map<String, JdaConsoleCommand> commands = new HashMap<>();

    public JdaConsoleManager() {
        // Built-ins
        this.register(new JdaExitConsoleCommand());

        Thread thread = new Thread(new JdaConsoleReader(this), "jda-console-reader");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Registers one or more console commands.
     *
     * @param commands the commands to register
     */
    public void register(JdaConsoleCommand... commands) {
        for (JdaConsoleCommand cmd : commands) {
            Objects.requireNonNull(cmd, "command");
            this.commands.put(cmd.command().toLowerCase(), cmd);
        }
    }

    /**
     * Handles a raw command string read from stdin.
     *
     * @param identifier the command keyword (already lower-cased)
     * @param args       the remaining arguments
     */
    public void handleCommand(String identifier, String[] args) {
        JdaConsoleCommand command = this.commands.get(identifier);
        if (command == null) {
            JdaBotLogger.error("'%s' is not a recognised console command.", identifier);
            return;
        }
        command.execute(args);
    }

    // -------------------------------------------------------------------------
    // Built-in commands
    // -------------------------------------------------------------------------

    private static final class JdaExitConsoleCommand implements JdaConsoleCommand {
        @Override
        public void execute(String[] args) {
            JdaBotLogger.info("Stopping bot…");
            System.exit(0);
        }

        @Override
        public String command() { return "stop"; }
    }
}

