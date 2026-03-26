package fr.ibrakash.helper.jda.admin;

import fr.ibrakash.helper.jda.command.JdaSlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Base type for slash commands that require an active admin session.
 *
 * <p>Override {@link #executeAdmin(SlashCommandInteractionEvent)} instead of
 * {@link #execute(SlashCommandInteractionEvent)}.  The admin check is performed
 * automatically via the {@link JdaAdminManager} supplied at construction.
 */
public abstract class JdaAdminSlashCommand implements JdaSlashCommand {

    private final JdaAdminManager adminManager;

    protected JdaAdminSlashCommand(JdaAdminManager adminManager) {
        this.adminManager = adminManager;
    }

    @Override
    public final void execute(SlashCommandInteractionEvent event) {
        this.adminManager.requireAdmin(event, () -> this.executeAdmin(event));
    }

    /**
     * Invoked only when the caller passes the admin check.
     *
     * @param event the slash command interaction
     */
    public abstract void executeAdmin(SlashCommandInteractionEvent event);

    protected JdaAdminManager adminManager() {
        return this.adminManager;
    }
}

