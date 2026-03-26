package fr.ibrakash.helper.jda.console;

import fr.ibrakash.helper.jda.logging.JdaBotLogger;

/**
 * A single console command that can be typed into the server stdin.
 *
 * <p>Implement this interface, then register instances with {@link JdaConsoleManager}.
 */
public interface JdaConsoleCommand {

    /**
     * Invoked when the command is entered in the console.
     *
     * @param args the whitespace-split arguments that follow the command keyword
     */
    void execute(String[] args);

    /** The identifier used to trigger this command (case-insensitive). */
    String command();

    default void success(String message, Object... formats) {
        JdaBotLogger.info(message, formats);
    }

    default void error(String message, Object... formats) {
        JdaBotLogger.error(message, formats);
    }
}

