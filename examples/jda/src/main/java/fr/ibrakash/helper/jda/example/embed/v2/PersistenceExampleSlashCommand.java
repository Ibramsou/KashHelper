package fr.ibrakash.helper.jda.example.embed.v2;

import fr.ibrakash.helper.jda.command.JdaSlashCommand;
import fr.ibrakash.helper.jda.example.JdaExample;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class PersistenceExampleSlashCommand implements JdaSlashCommand {

    private static final String TARGET_CHANNEL_OPTION = "channel";

    private final JdaExample addon;

    public PersistenceExampleSlashCommand(JdaExample addon) {
        this.addon = addon;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        long channelId = this.resolveChannelId(event);
        long ownerId = event.getUser().getIdLong();

        event.deferReply(true).queue(hook -> {
            PersistenceExample embed = this.addon.requirePersistenceExampleRepository().getOrCreate(channelId, ownerId);
            embed.reload().whenComplete((ignored, error) -> {
                if (error != null) {
                    hook.editOriginal("Unable to send the persistent embed: " + error.getMessage()).queue();
                    return;
                }
                this.addon.requirePersistenceExampleRepository().save(embed);
                hook.editOriginal("Persistent embed posted or updated in <#" + channelId + ">.").queue();
            });
        });
    }

    @Override
    public String name() {
        return "persistence-example";
    }

    @Override
    public String description() {
        return "Posts or reloads the persistent embed stored in the database.";
    }

    @Override
    public List<OptionData> options() {
        return List.of(new OptionData(OptionType.CHANNEL, TARGET_CHANNEL_OPTION, "Target channel", false));
    }

    private long resolveChannelId(SlashCommandInteractionEvent event) {
        OptionMapping channelOption = event.getOption(TARGET_CHANNEL_OPTION);
        if (channelOption != null) {
            return channelOption.getAsChannel().getIdLong();
        }
        return event.getChannel().getIdLong();
    }
}


