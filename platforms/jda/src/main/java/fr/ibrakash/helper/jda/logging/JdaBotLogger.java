package fr.ibrakash.helper.jda.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Centralised logging helper for JDA bots, inspired by Cordzy's {@code Logging} class.
 * <p>Uses SLF4J under the hood; configure the backend (Logback, etc.) in {@code logback.xml}.
 */
public final class JdaBotLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdaBotLogger.class);

    private JdaBotLogger() {}

    public static void debug(String message, Object... formats) {
        log(message, LOGGER::debug, formats);
    }

    public static void info(String message, Object... formats) {
        log(message, LOGGER::info, formats);
    }

    public static void warn(String message, Object... formats) {
        log(message, LOGGER::warn, formats);
    }

    public static void error(String message, Object... formats) {
        error(message, null, formats);
    }

    public static void error(String message, Throwable throwable, Object... formats) {
        log(message, s -> {
            if (throwable == null) {
                LOGGER.error(s);
            } else {
                LOGGER.error(s, throwable);
            }
        }, formats);
    }

    private static void log(String input, Consumer<String> logging, Object... formats) {
        String message = (formats == null || formats.length == 0) ? input : String.format(input, formats);
        logging.accept(message);
    }
}

