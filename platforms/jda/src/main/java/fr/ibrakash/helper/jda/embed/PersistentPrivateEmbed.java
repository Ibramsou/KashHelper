package fr.ibrakash.helper.jda.embed;

import fr.ibrakash.helper.jda.configuration.readers.JdaSystemLocale;
import fr.ibrakash.helper.persistence.entity.PersistedColumn;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

import java.util.concurrent.CompletableFuture;

public abstract class PersistentPrivateEmbed extends PersistentEmbed {

    @PersistedColumn("user-id")
    protected final long userId;

    protected PersistentPrivateEmbed() {
        this(-1L);
    }

    protected PersistentPrivateEmbed(long userId) {
        this.userId = userId;
    }

    public String dmDisabledMessagePath() {
        return JdaSystemLocale.DIRECT_MESSAGE_DISABLED;
    }

    public Object[] dmDisabledMessageReplacers() {
        return new Object[0];
    }

    public CompletableFuture<Void> reloadMessage(IReplyCallback fallbackReplyEvent) {
        return this.reloadMessageWithStatus(fallbackReplyEvent).thenApply(ignored -> null);
    }

    public CompletableFuture<Boolean> reloadMessageWithStatus(IReplyCallback fallbackReplyEvent) {
        this.markActive();
        return this.addon().getPersistentEmbedManager().reload(this, fallbackReplyEvent)
                .thenApply(message -> {
                    if (message != null) {
                        this.updateMessageId(message.getIdLong());
                        return true;
                    }
                    return false;
                });
    }

    @Override
    protected CompletableFuture<MessageChannel> resolveChannel() {
        if (this.userId <= 0L) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("PersistentPrivateEmbed requires a valid user id"));
        }
        return requireAddon().getJda().retrieveUserById(this.userId)
                .submit()
                .thenCompose(user -> user.openPrivateChannel().submit())
                .thenApply(channel -> channel);
    }

    @Override
    protected CompletableFuture<Message> handleReloadFailure(IReplyCallback fallback, Throwable error) {
        if (fallback != null) {
            this.systemLocale().reply(this.dmDisabledMessagePath(), fallback, this.dmDisabledMessageReplacers());
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.failedFuture(error);
    }

    public long getUserId() {
        return userId;
    }
}
