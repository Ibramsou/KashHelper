package fr.ibrakash.helper.jda.embed;

import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.util.concurrent.CompletableFuture;

public abstract class PersistentChannelEmbed extends PersistentEmbed {

    private final long constructorChannelId;

    protected PersistentChannelEmbed() {
        this(-1L);
    }

    protected PersistentChannelEmbed(long channelId) {
        this.constructorChannelId = channelId;
    }

    public long channelId() {
        return this.constructorChannelId;
    }

    @Override
    protected CompletableFuture<MessageChannel> resolveChannel() {
        long channelId = channelId();
        if (channelId <= 0L) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("PersistentChannelEmbed requires a valid channel id"));
        }
        MessageChannel channel = requireAddon().getJda().getChannelById(MessageChannel.class, channelId);
        if (channel != null) return CompletableFuture.completedFuture(channel);
        return CompletableFuture.failedFuture(new IllegalStateException("Channel not found: " + channelId));
    }
}
