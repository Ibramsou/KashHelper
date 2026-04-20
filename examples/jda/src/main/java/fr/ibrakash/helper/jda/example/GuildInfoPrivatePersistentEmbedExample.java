package fr.ibrakash.helper.jda.example;

import fr.ibrakash.helper.jda.embed.PersistentPrivateEmbed;
import fr.ibrakash.helper.jda.platform.KashJdaAddon;

import java.util.Map;

public class GuildInfoPrivatePersistentEmbedExample extends PersistentPrivateEmbed {

    private static final String PATH = "guild-info-dm.container";


    public GuildInfoPrivatePersistentEmbedExample(long targetUserId) {
        super(targetUserId);
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
    public Map<String, Object> placeholders() {
        return Map.of(
                "%user_name%", "member",
                "%score%", 87
        );
    }
}
