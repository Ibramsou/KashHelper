package fr.ibrakash.helper.jda.example;

import fr.ibrakash.helper.jda.command.JdaSlashCommand;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sends/refreshes the guild-info DM embed.
 */
public class GuildInfoDmSlashCommand implements JdaSlashCommand {

    private static final String TARGET_USER_OPTION = "user";

    private final JdaExample addon;
    private final ConcurrentHashMap<Long, GuildInfoPrivatePersistentEmbedExample> embedsByUserId;

    public GuildInfoDmSlashCommand(JdaExample addon) {
        this.addon = addon;
        this.embedsByUserId = new ConcurrentHashMap<>();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        User target = this.resolveUser(event);
        long userId = target.getIdLong();

        GuildInfoPrivatePersistentEmbedExample embed = this.embedsByUserId.computeIfAbsent(
                userId,
                id -> new GuildInfoPrivatePersistentEmbedExample(this.addon, id)
        );

        embed.reloadMessageWithStatus(event).whenComplete((dmSent, error) -> {
            if (error != null) {
                if (!event.isAcknowledged()) {
                    event.reply("Impossible d'envoyer le DM: " + error.getMessage())
                            .setEphemeral(true)
                            .queue();
                }
                return;
            }

            if (Boolean.TRUE.equals(dmSent)) {
                if (!event.isAcknowledged()) {
                    event.reply("DM envoye a " + target.getAsMention() + ".")
                            .setEphemeral(true)
                            .queue();
                }
                return;
            }

            // Fallback was used (e.g. DMs disabled): never claim the DM was sent.
            if (!event.isAcknowledged()) {
                event.reply("Impossible d'envoyer le DM.")
                        .setEphemeral(true)
                        .queue();
            }
        });
    }

    @Override
    public String name() {
        return "guild-info-dm";
    }

    @Override
    public String description() {
        return "Envoie l'embed de test en DM (avec fallback si DM desactive).";
    }

    @Override
    public List<OptionData> options() {
        return List.of(new OptionData(OptionType.USER, TARGET_USER_OPTION, "Utilisateur cible", false));
    }

    private User resolveUser(SlashCommandInteractionEvent event) {
        OptionMapping targetOption = event.getOption(TARGET_USER_OPTION);
        return targetOption == null ? event.getUser() : targetOption.getAsUser();
    }
}
