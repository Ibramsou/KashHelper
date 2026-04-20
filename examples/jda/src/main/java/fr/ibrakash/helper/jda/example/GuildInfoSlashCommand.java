package fr.ibrakash.helper.jda.example;

import fr.ibrakash.helper.jda.command.JdaSlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GuildInfoSlashCommand implements JdaSlashCommand {

    private static final String TARGET_CHANNEL_OPTION = "channel";

    private final JdaExample addon;
    private final ConcurrentHashMap<Long, GuildInfoPersistentEmbedExample> embedsByChannelId;

    public GuildInfoSlashCommand(JdaExample addon) {
        this.addon = addon;
        this.embedsByChannelId = new ConcurrentHashMap<>();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        long channelId = this.resolveChannelId(event);
        GuildInfoPersistentEmbedExample embed = this.embedsByChannelId.computeIfAbsent(
                channelId,
                id -> new GuildInfoPersistentEmbedExample(this.addon, id)
        );

        event.deferReply(true).queue(hook ->
                embed.reload().whenComplete((ignored, error) -> {
                    if (error != null) {
                        hook.editOriginal("Impossible d'envoyer l'embed: " + error.getMessage()).queue();
                        return;
                    }
                    hook.editOriginal("Embed guild-info poste ou mis a jour dans <#" + channelId + ">.").queue();
                })
        );
    }

    @Override
    public String name() {
        return "guild-info";
    }

    @Override
    public String description() {
        return "Poste ou met a jour l'embed de test interactif dans un salon.";
    }

    @Override
    public List<OptionData> options() {
        return List.of(new OptionData(OptionType.CHANNEL, TARGET_CHANNEL_OPTION, "Salon cible", false));
    }

    private long resolveChannelId(SlashCommandInteractionEvent event) {
        OptionMapping channelOption = event.getOption(TARGET_CHANNEL_OPTION);
        if (channelOption != null) {
            return channelOption.getAsChannel().getIdLong();
        }
        return event.getChannel().getIdLong();
    }
}
