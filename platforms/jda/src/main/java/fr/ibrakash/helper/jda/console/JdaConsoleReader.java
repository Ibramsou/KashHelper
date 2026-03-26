package fr.ibrakash.helper.jda.console;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * Background thread that reads lines from {@code System.in} and dispatches them to
 * {@link JdaConsoleManager}.
 */
final class JdaConsoleReader implements Runnable {

    private final JdaConsoleManager manager;

    JdaConsoleReader(JdaConsoleManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;
                dispatch(trimmed);
            }
        } catch (Exception e) {
            // stdin closed – normal during shutdown
        }
    }

    private void dispatch(String input) {
        String[] parts      = input.split("\\s+");
        String   identifier = parts[0].toLowerCase();
        String[] args       = Arrays.copyOfRange(parts, 1, parts.length);
        this.manager.handleCommand(identifier, args);
    }
}

