package fr.ibrakash.helper.jda.admin.console;

import fr.ibrakash.helper.jda.admin.JdaAdminManager;

/** Console command: {@code admin-add <userId>} */
public final class JdaAddAdminConsoleCommand extends JdaAdminConsoleCommand {

    public JdaAddAdminConsoleCommand(JdaAdminManager adminManager) {
        super(adminManager);
    }

    @Override
    public void execute(String[] args) {
        long userId = parseUserId(args);
        if (userId == -1L) return;

        if (adminManager.isAdmin(userId)) {
            error("User %d is already an admin.", userId);
            return;
        }

        adminManager.addAdmin(userId);
        success("Added admin: %d", userId);
    }

    @Override
    public String command() { return "admin-add"; }
}

