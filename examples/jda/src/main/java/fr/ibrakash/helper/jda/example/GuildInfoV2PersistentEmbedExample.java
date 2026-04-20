package fr.ibrakash.helper.jda.example;

import fr.ibrakash.helper.jda.embed.PersistentChannelEmbed;
import fr.ibrakash.helper.jda.logging.JdaBotLogger;
import fr.ibrakash.helper.jda.platform.KashJdaAddon;

import java.util.Map;

public class GuildInfoV2PersistentEmbedExample extends PersistentChannelEmbed {

    private static final String PATH = "guild-info-v2";

    private String selectedPeriod = "7d";

    public GuildInfoV2PersistentEmbedExample() {
        this(-1);
    }

    public GuildInfoV2PersistentEmbedExample(long targetChannelId) {
        super(targetChannelId);
        this.registerActions();
    }

    @Override
    public KashJdaAddon<?, ?, ?> addon() {
        return JdaExample.getInstance();
    }

    @Override
    public String embedPath() {
        return PATH;
    }

    @Override
    public void onDeserialized() {
        this.registerActions();
    }

    private void registerActions() {
        this.buttonAction("refresh_guild", event -> {
            this.reload();
            event.reply("✅ V2 embed refreshed!").setEphemeral(true).queue();
        });

        this.buttonAction("details_guild", event ->
                event.reply("📋 V2 Details for period: **" + selectedPeriod + "**")
                        .setEphemeral(true).queue());

        this.buttonAction("export_csv", event ->
                event.reply("📥 Exporting CSV for period: **" + selectedPeriod + "**")
                        .setEphemeral(true).queue());

        this.selectAction("guild_period", event -> {
            this.selectedPeriod = event.getValues().get(0);
            JdaBotLogger.info("V2 period changed to: %s", selectedPeriod);
            this.reload();
            event.reply("📆 Period changed to **" + selectedPeriod + "**").setEphemeral(true).queue();
        });
    }

    @Override
    public Map<String, Object> placeholders() {
        return Map.ofEntries(
                Map.entry("%guild_name%",     "My Guild"),
                Map.entry("%guild_icon%",     "https://cdn.discordapp.com/embed/avatars/0.png"),
                Map.entry("%member_count%",   "1 034"),
                Map.entry("%online_count%",   "212"),
                Map.entry("%boost_level%",    "Level 2"),
                Map.entry("%score%",          87),
                Map.entry("%creation_date%",  "Jan 15, 2022"),
                // These would be real chart image URLs in production
                Map.entry("%chart_members%",  "https://cdn.discordapp.com/embed/avatars/1.png"),
                Map.entry("%chart_activity%", "https://cdn.discordapp.com/embed/avatars/2.png"),
                Map.entry("%period%",         selectedPeriod)
        );
    }
}
