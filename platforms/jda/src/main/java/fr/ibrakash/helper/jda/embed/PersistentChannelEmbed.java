package fr.ibrakash.helper.jda.embed;

import fr.ibrakash.helper.jda.platform.KashJdaAddon;

/**
 * Persistent embed routed to a classic guild/text channel.
 */
public abstract class PersistentChannelEmbed extends PersistentEmbed {

    private final long constructorChannelId;

    protected PersistentChannelEmbed(KashJdaAddon<?, ?, ?> addon) {
        this(addon, -1L);
    }

    protected PersistentChannelEmbed(KashJdaAddon<?, ?, ?> addon, long channelId) {
        super(addon);
        this.constructorChannelId = channelId;
    }

    public long channelId() {
        return this.constructorChannelId;
    }
}
