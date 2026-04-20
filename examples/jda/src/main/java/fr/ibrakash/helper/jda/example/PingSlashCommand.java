package fr.ibrakash.helper.jda.example;

import fr.ibrakash.helper.jda.command.JdaSlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class PingSlashCommand implements JdaSlashCommand {

    private final JdaExample addon;

    public PingSlashCommand(JdaExample addon) {
        this.addon = addon;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        long gatewayPing = event.getJDA().getGatewayPing();
        this.addon.getEmbedLocale().reply(
                "ping",
                event,
                "%gateway_ping%", gatewayPing
        );
    }

    @Override
    public String name() {
        return "ping";
    }

    @Override
    public String description() {
        return "Checks if the bot is alive.";
    }
}
