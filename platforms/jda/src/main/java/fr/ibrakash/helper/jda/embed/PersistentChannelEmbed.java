package fr.ibrakash.helper.jda.embed;

import fr.ibrakash.helper.persistence.entity.PersistedColumn;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.util.concurrent.CompletableFuture;

public abstract class PersistentChannelEmbed extends PersistentEmbed {

    @PersistedColumn("channel_id")
    protected final long channelId;

    protected PersistentChannelEmbed() {
        this(-1L);
    }

    protected PersistentChannelEmbed(long channelId) {
        this.channelId = channelId;
    }
    @Override
    protected CompletableFuture<MessageChannel> resolveChannel() {
        if (channelId <= 0L) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("PersistentChannelEmbed requires a valid channel id"));
        }
        MessageChannel channel = requireAddon().getJda().getChannelById(MessageChannel.class, channelId);
        if (channel != null) return CompletableFuture.completedFuture(channel);
        return CompletableFuture.failedFuture(new IllegalStateException("Channel not found: " + channelId));
    }

    public long getChannelId() {
        return channelId;
    }
}
