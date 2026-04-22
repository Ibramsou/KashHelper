package fr.ibrakash.helper.jda.command;

import fr.ibrakash.helper.jda.configuration.readers.JdaSystemLocale;
import fr.ibrakash.helper.jda.platform.KashJdaAddon;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;

/**
 * Built-in slash command that reloads all bot configurations.
 *
 * <p>Automatically registered by {@link KashJdaAddon} at startup.
 * Requires {@link Permission#ADMINISTRATOR} to execute.
 */
public class ReloadSlashCommand implements JdaSlashCommand {

    private final KashJdaAddon<?, ?, ?> addon;

    public ReloadSlashCommand(KashJdaAddon<?, ?, ?> addon) {
        this.addon = addon;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        this.addon.reloadAllConfigurations();
        event.reply(this.addon.getSystemLocale().serialized(JdaSystemLocale.CONFIG_RELOADED))
                .setEphemeral(true)
                .queue();
    }

    @Override
    public String name() {
        return "reload";
    }

    @Override
    public String description() {
        return "Reload all bot configurations.";
    }

    @Override
    public List<Permission> permissions() {
        return List.of(Permission.ADMINISTRATOR);
    }
}


