package fr.ibrakash.helper.jda.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;
import java.util.List;

/**
 * Represents a single JDA slash command.
 *
 * <p>Implement this interface and register your command with {@link JdaSlashCommandManager}.
 *
 * <pre>{@code
 * public class PingCommand implements JdaSlashCommand {
 *     public void execute(SlashCommandInteractionEvent event) {
 *         event.reply("Pong!").setEphemeral(true).queue();
 *     }
 *     public String name()        { return "ping"; }
 *     public String description() { return "Pong!"; }
 * }
 * }</pre>
 */
public interface JdaSlashCommand {

    /**
     * Executes the command logic when the slash command is triggered.
     *
     * @param event the JDA slash command interaction event
     */
    void execute(SlashCommandInteractionEvent event);

    /** The command name as registered in Discord (lowercase, no spaces). */
    String name();

    /** The description shown in the Discord command picker. */
    String description();

    /** Optional options (parameters). Defaults to an empty list. */
    default List<OptionData> options() {
        return Collections.emptyList();
    }

    /**
     * Discord permissions required to use this command.
     * An empty list means no restriction.
     */
    default List<Permission> permissions() {
        return Collections.emptyList();
    }
}

