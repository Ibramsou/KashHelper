package fr.ibrakash.helper.jda.admin.console;

import fr.ibrakash.helper.jda.admin.JdaAdminManager;
import fr.ibrakash.helper.jda.console.JdaConsoleCommand;

/**
 * Base type for console commands that operate on {@link JdaAdminManager} by Discord user ID.
 */
public abstract class JdaAdminConsoleCommand implements JdaConsoleCommand {

    protected final JdaAdminManager adminManager;

    protected JdaAdminConsoleCommand(JdaAdminManager adminManager) {
        this.adminManager = adminManager;
    }

    protected long parseUserId(String[] args) {
        if (args.length == 0) {
            error("Please provide a Discord user ID.");
            return -1L;
        }
        try {
            return Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            error("Invalid user ID: %s", args[0]);
            return -1L;
        }
    }
}

