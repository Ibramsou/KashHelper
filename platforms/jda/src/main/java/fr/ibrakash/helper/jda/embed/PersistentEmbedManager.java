package fr.ibrakash.helper.jda.embed;

import fr.ibrakash.helper.jda.embed.parser.PersistentEmbedParser;
import fr.ibrakash.helper.jda.embed.render.PersistentEmbedRenderer;
import fr.ibrakash.helper.jda.embed.spec.PersistentEmbedSpec;
import fr.ibrakash.helper.jda.text.JdaTextReplacer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class PersistentEmbedManager extends ListenerAdapter {

    private final JDA jda;
    private final PersistentEmbedParser parser;
    private final PersistentEmbedRenderer renderer;
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<ButtonHandlerRegistration>> buttonHandlers;
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<SelectHandlerRegistration>> selectHandlers;

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

        String customId = this.customId(embed, buttonId);
        this.buttonHandlers.compute(customId, (ignored, registrations) -> {
            CopyOnWriteArrayList<ButtonHandlerRegistration> list = registrations == null
                    ? new CopyOnWriteArrayList<>()
                    : registrations;
            list.removeIf(registration -> registration.embed() == embed);
            list.add(new ButtonHandlerRegistration(embed, action));
            return list;
        });
    }

    public void registerSelectAction(PersistentEmbed embed, String selectId, Consumer<StringSelectInteractionEvent> action) {
        Objects.requireNonNull(embed, "embed");
        Objects.requireNonNull(selectId, "selectId");
        Objects.requireNonNull(action, "action");

        String customId = this.customId(embed, selectId);
        this.selectHandlers.compute(customId, (ignored, registrations) -> {
            CopyOnWriteArrayList<SelectHandlerRegistration> list = registrations == null
                    ? new CopyOnWriteArrayList<>()
                    : registrations;
            list.removeIf(registration -> registration.embed() == embed);
            list.add(new SelectHandlerRegistration(embed, action));
            return list;
        });
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
            JdaTextReplacer replacer = buildReplacer(embed);
            spec = this.parser.parse(lines, replacer);
            rendered = this.renderer.render(spec, embed.embedPath(), null);
        } catch (Exception exception) {
            return CompletableFuture.failedFuture(exception);
        }

        return embed.resolveChannel()
                .thenCompose(channel -> this.sendOrEdit(embed, channel, rendered))
                .exceptionallyCompose(error -> embed.handleReloadFailure(dmFallbackReplyEvent, error));
    }

    public CompletableFuture<Void> destroy(PersistentEmbed embed) {
        Objects.requireNonNull(embed, "embed");
        if (embed.messageId() <= 0L) {
            return CompletableFuture.completedFuture(null);
        }

        return embed.resolveChannel()
                .thenCompose(channel -> channel.deleteMessageById(embed.messageId()).submit())
                .exceptionally(ignored -> null)
                .thenApply(ignored -> null);
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
        CopyOnWriteArrayList<ButtonHandlerRegistration> registrations = this.buttonHandlers.get(event.getComponentId());
        if (registrations == null || registrations.isEmpty()) {
            return;
        }

        long messageId = event.getMessageIdLong();
        for (ButtonHandlerRegistration registration : registrations) {
            if (registration.embed().messageId() == messageId) {
                registration.action().accept(event);
                return;
            }
        }
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        CopyOnWriteArrayList<SelectHandlerRegistration> registrations = this.selectHandlers.get(event.getComponentId());
        if (registrations == null || registrations.isEmpty()) {
            return;
        }

        long messageId = event.getMessageIdLong();
        for (SelectHandlerRegistration registration : registrations) {
            if (registration.embed().messageId() == messageId) {
                registration.action().accept(event);
                return;
            }
        }
    }

    private record ButtonHandlerRegistration(PersistentEmbed embed, Consumer<ButtonInteractionEvent> action) {
    }

    private record SelectHandlerRegistration(PersistentEmbed embed, Consumer<StringSelectInteractionEvent> action) {
    }
}

