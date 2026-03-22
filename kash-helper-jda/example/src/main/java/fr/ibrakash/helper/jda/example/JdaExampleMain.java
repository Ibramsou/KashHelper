package fr.ibrakash.helper.jda.example;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Minimal JDA bootstrap example without locale service.
 */
public final class JdaExampleMain extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("ping")) return;
        event.reply("pong").setEphemeral(true).queue();
    }

    public static void main(String[] args) throws Exception {
        String token = System.getenv("BOT_TOKEN");
        if (token == null) {
            System.out.println("Set BOT_TOKEN env var to run the example.");
            return;
        }

        JDA jda = JDABuilder.createDefault(token).build().awaitReady();
        jda.addEventListener(new JdaExampleMain());

        System.out.println("Bot ready: " + jda.getSelfUser().getAsTag());
    }
}
