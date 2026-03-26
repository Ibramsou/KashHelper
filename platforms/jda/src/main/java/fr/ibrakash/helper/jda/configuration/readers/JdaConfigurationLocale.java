package fr.ibrakash.helper.jda.configuration.readers;

import fr.ibrakash.helper.configuration.readers.LocaleConfigurationReader;
import fr.ibrakash.helper.jda.configuration.objects.display.JdaConfigMessage;
import fr.ibrakash.helper.platform.KashAddon;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class JdaConfigurationLocale extends LocaleConfigurationReader<MessageCreateData, MessageChannel, JdaConfigMessage> {

    protected JdaConfigurationLocale(KashAddon<?> addon) {
        super(addon);
    }

    public JdaConfigMessage message(String path) {
        this.ensureLoaded();
        return this.get(path);
    }

    public List<String> lines(String path) {
        this.ensureLoaded();

        ConfigurationNode resolved = this.resolvePath(path);
        if (resolved.empty()) {
            return List.of();
        }

        if (resolved.isList()) {
            try {
                List<String> lines = resolved.getList(String.class);
                return lines == null ? List.of() : lines;
            } catch (SerializationException ignored) {
                return List.of();
            }
        }

        String value = resolved.getString();
        if (value == null || value.isBlank()) {
            return List.of();
        }

        return List.of(value.split("\\R"));
    }

    public MessageCreateData serialized(String path, Object... replacers) {
        JdaConfigMessage message = this.message(path);
        if (message == null) {
            return new MessageCreateBuilder().setContent("Not configured").build();
        }
        return message.serialized(replacers);
    }

    /**
     * Convenience method returning a serialized payload from config.
     * Useful when callers want to send/reply manually while still relying on locale embed definitions.
     */
    public MessageCreateData send(String path, Object... replacers) {
        return this.serialized(path, replacers);
    }

    /**
     * Convenience alias for {@link #serialized(String, Object...)}.
     */
    public MessageCreateData reply(String path, Object... replacers) {
        return this.serialized(path, replacers);
    }


    /**
     * Sends to a guild/text channel.
     * Works for both embed messages and plain-text messages.
     */
    public void send(String path, MessageChannel channel, Object... replacers) {
        JdaConfigMessage message = this.message(path);
        if (message != null) {
            message.send(channel, replacers);
        }
    }

    /**
     * Sends privately to a user DM.
     * Works for both embed messages and plain-text messages.
     * Returns a CompletableFuture so callers can react if DMs are disabled.
     */
    public CompletableFuture<Void> sendPrivately(String path, User user, Object... replacers) {
        JdaConfigMessage message = this.message(path);
        if (message == null) {
            return CompletableFuture.completedFuture(null);
        }

        return user.openPrivateChannel()
                .submit()
                .thenAccept(channel -> message.send(channel, replacers));
    }

    /**
     * Replies to an interaction event (slash command, button, etc.).
     */
    public void reply(String path, IReplyCallback event, Object... replacers) {
        JdaConfigMessage message = this.message(path);
        if (message != null) {
            message.reply(event, replacers);
        }
    }

    private void ensureLoaded() {
        if (this.node == null) {
            this.reload();
        }
    }

    @Override
    protected Class<JdaConfigMessage> messageType() {
        return JdaConfigMessage.class;
    }

    @Override
    protected JdaConfigMessage fromString(String value) {
        return new JdaConfigMessage(value);
    }
}
