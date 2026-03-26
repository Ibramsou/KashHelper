package fr.ibrakash.helper.jda.configuration.objects.display;

import fr.ibrakash.helper.configuration.objects.ConfigMessage;
import fr.ibrakash.helper.jda.embed.parser.PersistentEmbedParser;
import fr.ibrakash.helper.jda.embed.render.PersistentEmbedRenderer;
import fr.ibrakash.helper.jda.embed.spec.PersistentEmbedSpec;
import fr.ibrakash.helper.text.TextUtil;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ConfigSerializable
public class JdaConfigMessage extends ConfigMessage<MessageCreateData, MessageChannel> {

    private static final PersistentEmbedParser EMBED_PARSER = new PersistentEmbedParser();
    private static final PersistentEmbedRenderer EMBED_RENDERER = new PersistentEmbedRenderer();

    public JdaConfigMessage() {
        super();
    }

    public JdaConfigMessage(String message) {
        super(message);
    }

    @Override
    protected void sendSerialized(MessageChannel audience, MessageCreateData serialized, Object... replacers) {
        audience.sendMessage(serialized).queue();
    }

    public void reply(IReplyCallback event, Object... replacers) {
        event.reply(this.serialized(replacers)).queue();
    }

    @Override
    protected void broadcastSerialized(MessageCreateData serialized, Object... replacers) {
        throw new UnsupportedOperationException("JDA broadcast is not supported by default");
    }

    @Override
    public MessageCreateData serialized(Object... replacers) {
        String raw = this.getMessage();

        if (looksLikeEmbedTemplate(raw)) {
            List<String> lines = List.of(raw.split("\\R"));
            PersistentEmbedSpec spec = EMBED_PARSER.parse(lines);
            var rendered = EMBED_RENDERER.render(spec, "locale", toPlaceholderMap(replacers));
            return EMBED_RENDERER.createData(rendered);
        }

        return new MessageCreateBuilder().setContent(TextUtil.replaced(raw, replacers)).build();
    }

    private static boolean looksLikeEmbedTemplate(String raw) {
        if (raw == null || raw.isBlank()) {
            return false;
        }
        return raw.contains("<container>") && raw.contains("</container>");
    }

    private static Map<String, Object> toPlaceholderMap(Object... replacers) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < replacers.length; i += 2) {
            if (i + 1 >= replacers.length) {
                break;
            }
            result.put(String.valueOf(replacers[i]), replacers[i + 1]);
        }
        return result;
    }
}

