package fr.ibrakash.helper.jda.embed;

import fr.ibrakash.helper.jda.configuration.readers.JdaSystemLocale;
import fr.ibrakash.helper.jda.platform.KashJdaAddon;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

import java.util.concurrent.CompletableFuture;

/**
 * Persistent embed routed to a user DM.
 */
public abstract class PersistentPrivateEmbed extends PersistentEmbed {

    private final long constructorUserId;

    protected PersistentPrivateEmbed(KashJdaAddon<?, ?, ?> addon) {
        this(addon, -1L);
    }

    protected PersistentPrivateEmbed(KashJdaAddon<?, ?, ?> addon, long userId) {
        super(addon);
        this.constructorUserId = userId;
    }

    public long userId() {
        return this.constructorUserId;
    }

    /**
     * The system-locale path used when a DM cannot be delivered.
     * Defaults to {@link JdaSystemLocale#DIRECT_MESSAGE_DISABLED}.
     * Override to use a different key.
     */
    public String dmDisabledMessagePath() {
        return JdaSystemLocale.DIRECT_MESSAGE_DISABLED;
    }

    public Object[] dmDisabledMessageReplacers() {
        return new Object[0];
    }

    public CompletableFuture<Void> reloadMessage(IReplyCallback fallbackReplyEvent) {
        return this.reloadMessageWithStatus(fallbackReplyEvent).thenApply(ignored -> null);
    }

    /**
     * @return true when a DM message was actually sent/updated, false when fallback handling was used.
     */
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
}
