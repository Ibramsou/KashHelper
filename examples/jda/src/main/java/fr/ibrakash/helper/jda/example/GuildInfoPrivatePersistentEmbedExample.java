package fr.ibrakash.helper.jda.example;

import fr.ibrakash.helper.jda.embed.PersistentPrivateEmbed;
import fr.ibrakash.helper.jda.platform.KashJdaAddon;

import java.util.Map;

/**
 * DM version of persistent embed with configurable fallback message path.
 */
public class GuildInfoPrivatePersistentEmbedExample extends PersistentPrivateEmbed {

    private static final String PATH = "guild-info-dm.container";

    private final long targetUserId;

    public GuildInfoPrivatePersistentEmbedExample(KashJdaAddon<?, ?, ?> addon, long targetUserId) {
        super(addon, targetUserId);
        this.targetUserId = targetUserId;
    }

    @Override
    public String embedPath() {
        return PATH;
    }

    @Override
    public long userId() {
        return this.targetUserId;
    }

    @Override
    public Map<String, Object> placeholders() {
        return Map.of(
                "%user_name%", "member",
                "%score%", 87
        );
    }
}
