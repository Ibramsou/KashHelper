package fr.ibrakash.helper.jda.embed;

import fr.ibrakash.helper.jda.configuration.readers.JdaConfigurationLocale;
import fr.ibrakash.helper.jda.configuration.readers.JdaSystemLocale;
import fr.ibrakash.helper.jda.platform.KashJdaAddon;
import fr.ibrakash.helper.persistence.entity.PersistenceLifecycle;
import fr.ibrakash.helper.persistence.entity.PersistedColumn;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class PersistentEmbed implements PersistenceLifecycle {

    private volatile transient boolean removed;

    @PersistedColumn("message_id")
    private volatile long messageId;

    public abstract KashJdaAddon<?, ?, ?> addon();

    public abstract String embedPath();

    public JdaConfigurationLocale localeConfig() {
        return requireAddon().getEmbedLocale();
    }

    public JdaSystemLocale systemLocale() {
        return requireAddon().getSystemLocale();
    }

    public Map<String, Object> placeholders() {
        return Collections.emptyMap();
    }

    public CompletableFuture<Void> reload(long messageId) {
        this.messageId = messageId;
        return this.reload();
    }

    public CompletableFuture<Void> reload() {
        this.markActive();
        return requireAddon().getPersistentEmbedManager().reload(this)
                .thenAccept(message -> {
                    if (message != null) {
                        this.updateMessageId(message.getIdLong());
                    }
                });
    }

    @Override
    public void onDeserialized() {
    }

    public CompletableFuture<Void> destroyMessage() {
        return requireAddon().getPersistentEmbedManager().destroy(this).thenRun(() -> this.removed = true);
    }

    public boolean isRemoved() {
        return this.removed;
    }

    public long messageId() {
        return this.messageId;
    }

    protected void buttonAction(String buttonId, Consumer<ButtonInteractionEvent> action) {
        requireAddon().getPersistentEmbedManager().registerAction(this, buttonId, action);
    }

    protected void selectAction(String selectId, Consumer<StringSelectInteractionEvent> action) {
        requireAddon().getPersistentEmbedManager().registerSelectAction(this, selectId, action);
    }

    protected void updateMessageId(long messageId) {
        this.messageId = messageId;
    }

    protected void markActive() {
        this.removed = false;
    }

    protected abstract CompletableFuture<MessageChannel> resolveChannel();

    protected CompletableFuture<Message> handleReloadFailure(IReplyCallback fallback, Throwable error) {
        return CompletableFuture.failedFuture(error);
    }

    protected final KashJdaAddon<?, ?, ?> requireAddon() {
        KashJdaAddon<?, ?, ?> a = addon();
        if (a == null) {
            throw new IllegalStateException(
                    "addon() returned null on " + getClass().getSimpleName()
                            + ". Ensure addon() is properly implemented before calling embed methods.");
        }
        return a;
    }
}
