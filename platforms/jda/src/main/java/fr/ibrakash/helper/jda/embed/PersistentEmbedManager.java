package fr.ibrakash.helper.jda.embed;

import fr.ibrakash.helper.jda.embed.parser.PersistentEmbedParser;
import fr.ibrakash.helper.jda.embed.render.PersistentEmbedRenderer;
import fr.ibrakash.helper.jda.embed.spec.PersistentEmbedSpec;
import fr.ibrakash.helper.jda.text.JdaTextReplacer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Runtime manager for config-driven persistent embeds.
 */
public class PersistentEmbedManager extends ListenerAdapter {

    private final JDA jda;
    private final PersistentEmbedParser parser;
    private final PersistentEmbedRenderer renderer;
    private final ConcurrentHashMap<String, Consumer<ButtonInteractionEvent>> buttonHandlers;
    private final ConcurrentHashMap<String, Consumer<StringSelectInteractionEvent>> selectHandlers;

    public PersistentEmbedManager(JDA jda) {
        this(jda, new PersistentEmbedParser(), new PersistentEmbedRenderer());
    }

    public PersistentEmbedManager(JDA jda, PersistentEmbedParser parser, PersistentEmbedRenderer renderer) {
        this.jda = Objects.requireNonNull(jda, "jda");
        this.parser = Objects.requireNonNull(parser, "parser");
        this.renderer = Objects.requireNonNull(renderer, "renderer");
        this.buttonHandlers = new ConcurrentHashMap<>();
        this.selectHandlers = new ConcurrentHashMap<>();
        this.jda.addEventListener(this);
    }

    public void registerAction(PersistentEmbed embed, String buttonId, Consumer<ButtonInteractionEvent> action) {
        Objects.requireNonNull(embed, "embed");
        Objects.requireNonNull(buttonId, "buttonId");
        Objects.requireNonNull(action, "action");
        this.buttonHandlers.put(this.customId(embed, buttonId), action);
    }

    public void registerSelectAction(PersistentEmbed embed, String selectId, Consumer<StringSelectInteractionEvent> action) {
        Objects.requireNonNull(embed, "embed");
        Objects.requireNonNull(selectId, "selectId");
        Objects.requireNonNull(action, "action");
        this.selectHandlers.put(this.customId(embed, selectId), action);
    }

    public CompletableFuture<Message> reload(PersistentEmbed embed) {
        return this.reload(embed, null);
    }

    public CompletableFuture<Message> reload(PersistentPrivateEmbed embed, IReplyCallback dmFallbackReplyEvent) {
        return this.reload((PersistentEmbed) embed, dmFallbackReplyEvent);
    }

    private CompletableFuture<Message> reload(PersistentEmbed embed, IReplyCallback dmFallbackReplyEvent) {
        Objects.requireNonNull(embed, "embed");

        List<String> lines;
        PersistentEmbedSpec spec;
        PersistentEmbedRenderer.RenderedPersistentEmbed rendered;

        try {
            lines = embed.localeConfig().lines(embed.embedPath());
            // Build the replacer from placeholders (mirrors Paper menus approach)
            JdaTextReplacer replacer = buildReplacer(embed);
            spec = this.parser.parse(lines, replacer);
            // Pass empty placeholders since they're already baked into the spec via replacer
            rendered = this.renderer.render(spec, embed.embedPath(), null);
        } catch (Exception exception) {
            return CompletableFuture.failedFuture(exception);
        }

        if (embed instanceof PersistentChannelEmbed channelEmbed) {
            return this.reloadChannelEmbed(channelEmbed, rendered);
        }

        if (embed instanceof PersistentPrivateEmbed privateEmbed) {
            return this.reloadPrivateEmbed(privateEmbed, rendered, dmFallbackReplyEvent);
        }

        return CompletableFuture.failedFuture(
                new IllegalStateException("Unsupported PersistentEmbed type: " + embed.getClass().getName()));
    }

    public CompletableFuture<Void> destroy(PersistentEmbed embed) {
        Objects.requireNonNull(embed, "embed");
        if (embed.messageId() <= 0L) {
            return CompletableFuture.completedFuture(null);
        }

        if (embed instanceof PersistentChannelEmbed channelEmbed) {
            return this.resolveChannel(channelEmbed)
                    .thenCompose(channel -> channel.deleteMessageById(embed.messageId()).submit())
                    .exceptionally(ignored -> null)
                    .thenApply(ignored -> null);
        }

        if (embed instanceof PersistentPrivateEmbed privateEmbed) {
            return this.resolvePrivateChannel(privateEmbed)
                    .thenCompose(channel -> channel.deleteMessageById(embed.messageId()).submit())
                    .exceptionally(ignored -> null)
                    .thenApply(ignored -> null);
        }

        return CompletableFuture.failedFuture(
                new IllegalStateException("Unsupported PersistentEmbed type: " + embed.getClass().getName()));
    }

    private CompletableFuture<Message> reloadChannelEmbed(
            PersistentChannelEmbed embed,
            PersistentEmbedRenderer.RenderedPersistentEmbed rendered
    ) {
        return this.resolveChannel(embed)
                .thenCompose(channel -> this.sendOrEdit(embed, channel, rendered));
    }

    private CompletableFuture<Message> reloadPrivateEmbed(
            PersistentPrivateEmbed embed,
            PersistentEmbedRenderer.RenderedPersistentEmbed rendered,
            IReplyCallback dmFallbackReplyEvent
    ) {
        return this.resolvePrivateChannel(embed)
                .thenCompose(channel -> this.sendOrEdit(embed, channel, rendered))
                .exceptionallyCompose(error -> this.handlePrivateSendFailure(embed, dmFallbackReplyEvent, error));
    }

    private CompletableFuture<Message> sendOrEdit(
            PersistentEmbed embed,
            MessageChannel channel,
            PersistentEmbedRenderer.RenderedPersistentEmbed rendered
    ) {
        if (embed.messageId() <= 0L || embed.isRemoved()) {
            return channel.sendMessage(this.renderer.createData(rendered)).submit();
        }

        return channel.retrieveMessageById(embed.messageId()).submit()
                .thenCompose(message -> message.editMessage(this.renderer.editData(rendered)).submit())
                .exceptionallyCompose(ignored -> channel.sendMessage(this.renderer.createData(rendered)).submit());
    }

    private CompletableFuture<Message> handlePrivateSendFailure(
            PersistentPrivateEmbed embed,
            IReplyCallback dmFallbackReplyEvent,
            Throwable error
    ) {
        if (dmFallbackReplyEvent != null) {
            // Use the system locale for the DM-disabled message so users don't
            // have to configure it in their embed locale.
            embed.systemLocale().reply(embed.dmDisabledMessagePath(), dmFallbackReplyEvent, embed.dmDisabledMessageReplacers());
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.failedFuture(error);
    }

    private CompletableFuture<MessageChannel> resolveChannel(PersistentChannelEmbed embed) {
        long channelId = embed.channelId();
        if (channelId <= 0L) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("PersistentChannelEmbed requires a valid channel id"));
        }
        MessageChannel channel = this.jda.getChannelById(MessageChannel.class, channelId);
        if (channel != null) return CompletableFuture.completedFuture(channel);
        return CompletableFuture.failedFuture(new IllegalStateException("Channel not found: " + channelId));
    }

    private CompletableFuture<PrivateChannel> resolvePrivateChannel(PersistentPrivateEmbed embed) {
        long userId = embed.userId();
        if (userId <= 0L) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("PersistentPrivateEmbed requires a valid user id"));
        }
        return this.jda.retrieveUserById(userId)
                .submit()
                .thenCompose(user -> user.openPrivateChannel().submit());
    }

    /** Builds a JdaTextReplacer from the embed's placeholders map. */
    private JdaTextReplacer buildReplacer(PersistentEmbed embed) {
        var placeholders = embed.placeholders();
        if (placeholders == null || placeholders.isEmpty()) return null;
        JdaTextReplacer replacer = new JdaTextReplacer();
        placeholders.forEach(replacer::add);
        return replacer;
    }

    private String customId(PersistentEmbed embed, String componentId) {
        return embed.embedPath() + ":" + componentId;
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        Consumer<ButtonInteractionEvent> handler = this.buttonHandlers.get(event.getComponentId());
        if (handler != null) handler.accept(event);
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        Consumer<StringSelectInteractionEvent> handler = this.selectHandlers.get(event.getComponentId());
        if (handler != null) handler.accept(event);
    }
}

