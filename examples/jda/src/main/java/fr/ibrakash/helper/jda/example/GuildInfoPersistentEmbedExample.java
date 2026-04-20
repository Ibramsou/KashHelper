package fr.ibrakash.helper.jda.example;

import fr.ibrakash.helper.jda.embed.PersistentChannelEmbed;
import fr.ibrakash.helper.jda.logging.JdaBotLogger;
import fr.ibrakash.helper.jda.platform.KashJdaAddon;

import java.util.Map;

public class GuildInfoPersistentEmbedExample extends PersistentChannelEmbed {

    private static final String PATH = "guild-info.container";

    private final transient KashJdaAddon<?, ?, ?> addon;
    private String selectedPeriod = "7d";

    public GuildInfoPersistentEmbedExample(KashJdaAddon<?, ?, ?> addon, long targetChannelId) {
        super(targetChannelId);
        this.addon = addon;
        this.registerActions();
    }

    @Override
    public KashJdaAddon<?, ?, ?> addon() {
        return this.addon;
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
            event.reply("✅ Embed refreshed!").setEphemeral(true).queue();
        });

        this.buttonAction("details_guild", event ->
                event.reply("📋 Details for period: **" + selectedPeriod + "**")
                        .setEphemeral(true).queue());

        this.selectAction("guild_period", event -> {
            this.selectedPeriod = event.getValues().get(0);
            JdaBotLogger.info("Period changed to: %s", selectedPeriod);
            this.reload();
            event.reply("📆 Period changed to **" + selectedPeriod + "**").setEphemeral(true).queue();
        });
    }

    @Override
    public Map<String, Object> placeholders() {
        return Map.ofEntries(
                Map.entry("%guild_title%",                    "My Guild"),
                Map.entry("%guild_name%",                     "My Guild"),
                Map.entry("%bot_avatar%",                     ""),
                Map.entry("%guild_icon%",                     ""),
                Map.entry("%banner_url%",                     ""),
                Map.entry("%locale_display%",                 "fr-FR"),
                Map.entry("%economic_value%",                 42),
                Map.entry("%ad_estimation_economic_value%",   128),
                Map.entry("%members_score%",                  78),
                Map.entry("%activity_score%",                 92),
                Map.entry("%ad_click_score%",                 65),
                Map.entry("%score%",                          87),
                Map.entry("%members_estimation%",             "~1 200"),
                Map.entry("%member_count%",                   "1 034"),
                Map.entry("%online_count%",                   "212"),
                Map.entry("%boost_level%",                    "Level 2"),
                Map.entry("%creation_date%",                  "Jan 15, 2022"),
                Map.entry("%last_update%",                    "now"),
                Map.entry("%period%",                         selectedPeriod)
        );
    }
}
