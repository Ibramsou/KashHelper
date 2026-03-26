package fr.ibrakash.helper.jda.logging;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An {@link OutputStream} that forwards each line to an SLF4J {@link Logger}.
 * Useful for redirecting {@code System.out} and {@code System.err} to the log file.
 *
 * <pre>
 * System.setOut(new java.io.PrintStream(
 *     new JdaLoggingOutputStream(LoggerFactory.getLogger("SystemOut"), JdaLoggingOutputStream.LogLevel.INFO), true));
 * System.setErr(new java.io.PrintStream(
 *     new JdaLoggingOutputStream(LoggerFactory.getLogger("SystemErr"), JdaLoggingOutputStream.LogLevel.ERROR), true));
 * </pre>
 */
public final class JdaLoggingOutputStream extends OutputStream {

    public enum LogLevel { INFO, WARN, ERROR, DEBUG }

    private final Logger logger;
    private final LogLevel level;
    private final StringBuilder buffer = new StringBuilder();

    public JdaLoggingOutputStream(Logger logger, LogLevel level) {
        this.logger = logger;
        this.level = level;
    }

    @Override
    public void write(int b) throws IOException {
        if (b == '\n') {
            flush();
        } else {
            buffer.append((char) b);
        }
    }

    @Override
    public void flush() {
        if (buffer.isEmpty()) return;
        String message = buffer.toString().trim();
        buffer.setLength(0);
        switch (level) {
            case INFO  -> logger.info(message);
            case WARN  -> logger.warn(message);
            case ERROR -> logger.error(message);
            case DEBUG -> logger.debug(message);
        }
    }
}

