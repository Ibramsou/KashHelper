package fr.ibrakash.helper.jda.configuration.objects.display;

import fr.ibrakash.helper.configuration.objects.ConfigMessage;
import fr.ibrakash.helper.text.TextUtil;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

/**
 * Simple plain-text message for JDA system locale.
 *
 * <p>Supports either a single string or a list of strings (joined with \n by
 * the locale reader). Placeholders are handled by {@link TextUtil}.
 */
@ConfigSerializable
public class JdaPlainTextMessage extends ConfigMessage<MessageCreateData, MessageChannel> {

    public JdaPlainTextMessage() {
        super();
    }

    public JdaPlainTextMessage(String message) {
        super(message);
    }

    @Override
    protected void sendSerialized(MessageChannel audience, MessageCreateData serialized, Object... replacers) {
        audience.sendMessage(serialized).queue();
    }

    @Override
    protected void broadcastSerialized(MessageCreateData serialized, Object... replacers) {
        // No global broadcast implementation for JDA by default.
        throw new UnsupportedOperationException("JDA broadcast is not supported by default");
    }

    public void reply(IReplyCallback event, Object... replacers) {
        event.reply(this.serialized(replacers)).setEphemeral(true).queue();
    }

    @Override
    public MessageCreateData serialized(Object... replacers) {
        String content = this.getMessage(replacers);
        return new MessageCreateBuilder().setContent(content).build();
    }
}

