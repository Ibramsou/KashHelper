package fr.ibrakash.helper.jda.embed;

import fr.ibrakash.helper.jda.configuration.readers.JdaConfigurationLocale;
import fr.ibrakash.helper.jda.configuration.readers.JdaSystemLocale;
import fr.ibrakash.helper.jda.platform.KashJdaAddon;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Base type for an embed message that can be recreated and edited over time.
 */
public abstract class PersistentEmbed {

    private final KashJdaAddon<?, ?, ?> addon;
    private volatile long messageId;
    private volatile boolean removed;

    protected PersistentEmbed(KashJdaAddon<?, ?, ?> addon) {
        this.addon = Objects.requireNonNull(addon, "addon");
    }

    public JdaConfigurationLocale localeConfig() {
        return this.addon.getEmbedLocale();
    }

    public JdaSystemLocale systemLocale() {
        return this.addon.getSystemLocale();
    }

    public abstract String embedPath();

    public Map<String, Object> placeholders() {
        return Collections.emptyMap();
    }

    public CompletableFuture<Void> reloadMessage() {
        this.markActive();
        return this.addon.getPersistentEmbedManager().reload(this)
                .thenAccept(message -> {
                    if (message != null) {
                        this.updateMessageId(message.getIdLong());
                    }
                });
    }

    public CompletableFuture<Void> destroyMessage() {
        return this.addon.getPersistentEmbedManager().destroy(this).thenRun(() -> this.removed = true);
    }

    public boolean isRemoved() {
        return this.removed;
    }

    public long messageId() {
        return this.messageId;
    }

    protected void buttonAction(String buttonId, Consumer<ButtonInteractionEvent> action) {
        this.addon.getPersistentEmbedManager().registerAction(this, buttonId, action);
    }

    protected void selectAction(String selectId, Consumer<StringSelectInteractionEvent> action) {
        this.addon.getPersistentEmbedManager().registerSelectAction(this, selectId, action);
    }

    protected void updateMessageId(long messageId) {
        this.messageId = messageId;
    }

    protected void markActive() {
        this.removed = false;
    }

    protected KashJdaAddon<?, ?, ?> addon() {
        return this.addon;
    }
}
