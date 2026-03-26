package fr.ibrakash.helper.jda.admin.console;

import fr.ibrakash.helper.jda.admin.JdaAdminManager;

/** Console command: {@code admin-remove <userId>} */
public final class JdaRemoveAdminConsoleCommand extends JdaAdminConsoleCommand {

    public JdaRemoveAdminConsoleCommand(JdaAdminManager adminManager) {
        super(adminManager);
    }

    @Override
    public void execute(String[] args) {
        long userId = parseUserId(args);
        if (userId == -1L) return;

        if (!adminManager.isAdmin(userId)) {
            error("User %d is not an admin.", userId);
            return;
        }

        adminManager.removeAdmin(userId);
        success("Removed admin: %d", userId);
    }

    @Override
    public String command() { return "admin-remove"; }
}

