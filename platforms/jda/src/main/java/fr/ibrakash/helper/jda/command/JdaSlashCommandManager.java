package fr.ibrakash.helper.jda.command;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Manages registration and dispatching of {@link JdaSlashCommand} instances.
 *
 * <p>Usage:
 * <pre>{@code
 * JdaSlashCommandManager manager = new JdaSlashCommandManager();
 * manager.register(new PingCommand());
 * manager.registerAll(jda);   // uploads commands to Discord and attaches the listener
 * }</pre>
 */
public class JdaSlashCommandManager extends ListenerAdapter {

    private final List<JdaSlashCommand> commands = new ArrayList<>();

    /**
     * Registers a slash command so that it will be uploaded and dispatched.
     *
     * @param command the command to register
     */
    public void register(JdaSlashCommand command) {
        Objects.requireNonNull(command, "command");
        this.commands.add(command);
    }

    /**
     * Registers multiple slash commands at once.
     *
     * @param commands the commands to register
     */
    public void register(JdaSlashCommand... commands) {
        for (JdaSlashCommand cmd : commands) {
            this.register(cmd);
        }
    }

    /**
     * Uploads all registered commands globally to Discord and attaches this manager as a JDA event listener.
     *
     * @param jda the active JDA instance
     */
    public void registerAll(JDA jda) {
        this.registerAll(jda, null);
    }

    /**
     * Uploads all registered commands to Discord and attaches this manager as a JDA event listener.
     * If a {@code devGuildId} is provided, commands are registered on that guild only
     * (instant availability), otherwise they are registered globally.
     *
     * @param jda        the active JDA instance
     * @param devGuildId the dev guild ID, or {@code null}/empty for global registration
     */
    public void registerAll(JDA jda, String devGuildId) {
        Objects.requireNonNull(jda, "jda");

        List<SlashCommandData> data = this.commands.stream()
                .map(this::buildCommand)
                .toList();

        if (devGuildId != null && !devGuildId.isEmpty()) {
            net.dv8tion.jda.api.entities.Guild guild = jda.getGuildById(devGuildId);
            if (guild != null) {
                guild.updateCommands().addCommands(data).queue();
            } else {
                // Fallback to global if guild not found
                jda.updateCommands().addCommands(data).queue();
            }
        } else {
            jda.updateCommands().addCommands(data).queue();
        }
        jda.addEventListener(this);
    }

    /** Returns an unmodifiable view of the registered commands. */
    public List<JdaSlashCommand> commands() {
        return Collections.unmodifiableList(this.commands);
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        for (JdaSlashCommand cmd : this.commands) {
            if (cmd.name().equals(event.getName())) {
                cmd.execute(event);
                return;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Internals
    // -------------------------------------------------------------------------

    private SlashCommandData buildCommand(JdaSlashCommand cmd) {
        SlashCommandData data = Commands.slash(cmd.name(), cmd.description())
                .addOptions(cmd.options());
        List<Permission> permissions = cmd.permissions();
        if (!permissions.isEmpty()) {
            data.setDefaultPermissions(DefaultMemberPermissions.enabledFor(permissions.toArray(Permission[]::new)));
        }
        return data;
    }
}

