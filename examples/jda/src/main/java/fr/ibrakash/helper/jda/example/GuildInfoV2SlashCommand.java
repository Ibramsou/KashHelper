package fr.ibrakash.helper.jda.example;

import fr.ibrakash.helper.jda.command.JdaSlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GuildInfoV2SlashCommand implements JdaSlashCommand {

    private static final String TARGET_CHANNEL_OPTION = "channel";

    private final JdaExample addon;
    private final ConcurrentHashMap<Long, GuildInfoV2PersistentEmbedExample> embedsByChannelId;

    public GuildInfoV2SlashCommand(JdaExample addon) {
        this.addon = addon;
        this.embedsByChannelId = new ConcurrentHashMap<>();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        long channelId = resolveChannelId(event);
        GuildInfoV2PersistentEmbedExample embed = this.embedsByChannelId.computeIfAbsent(
                channelId,
                GuildInfoV2PersistentEmbedExample::new
        );

        event.deferReply(true).queue(hook ->
                embed.reload().whenComplete((ignored, error) -> {
                    if (error != null) {
                        hook.editOriginal("❌ Erreur: " + error.getMessage()).queue();
                        return;
                    }
                    hook.editOriginal("✅ V2 embed posté/mis à jour dans <#" + channelId + ">.").queue();
                })
        );
    }

    @Override
    public String name() {
        return "guild-info-v2";
    }

    @Override
    public String description() {
        return "Poste l'embed V2 (Components V2 Container) de guild-info.";
    }

    @Override
    public List<OptionData> options() {
        return List.of(new OptionData(OptionType.CHANNEL, TARGET_CHANNEL_OPTION, "Salon cible", false));
    }

    private long resolveChannelId(SlashCommandInteractionEvent event) {
        OptionMapping opt = event.getOption(TARGET_CHANNEL_OPTION);
        if (opt != null) return opt.getAsChannel().getIdLong();
        return event.getChannel().getIdLong();
    }
}

