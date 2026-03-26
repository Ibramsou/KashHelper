package fr.ibrakash.helper.jda.configuration.readers;

import fr.ibrakash.helper.jda.configuration.objects.display.JdaConfigMessage;
import fr.ibrakash.helper.platform.KashAddon;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Mandatory YAML-based system locale for every JDA bot.
 *
 * <p>This locale contains simple plain-text (or list-of-lines) messages used by the
 * KashHelper framework itself and by bot code that does not need embed formatting.
 */
public abstract class JdaSystemLocale extends JdaConfigurationLocale {

    public static final String DIRECT_MESSAGE_DISABLED = "direct-messages-disabled";
    public static final String NO_PERMISSION = "no-permission";
    public static final String ERROR_OCCURRED = "error-occurred";
    public static final String COMMAND_ONLY_GUILD = "command-only-guild";
    public static final String COMMAND_ONLY_DM = "command-only-dm";

    private static final Map<String, String> DEFAULT_MESSAGES = Map.ofEntries(
            Map.entry(NO_PERMISSION, "You do not have permission to perform this action."),
            Map.entry(DIRECT_MESSAGE_DISABLED, "Please enable your direct messages to receive this message."),
            Map.entry(ERROR_OCCURRED, "An unexpected error occurred. Please try again later."),
            Map.entry(COMMAND_ONLY_GUILD, "This command can only be used in a server."),
            Map.entry(COMMAND_ONLY_DM, "This command can only be used in direct messages.")
    );

    protected JdaSystemLocale(KashAddon<?> addon) {
        super(addon);
    }

    @Override
    public void reload() {
        this.pathMap.clear();
        DEFAULT_MESSAGES.forEach((key, value) -> this.pathMap.put(key, this.fromString(value)));
        super.reload();
    }

    @Override
    public boolean preLoad() {
        return true;
    }

    @Override
    protected List<NodeFilter> nodeFilters() {
        return List.of((path, nodeName, node) -> !path.contains("."));
    }

    @Override
    public void readNode(String nodeName, ConfigurationNode node) throws SerializationException {
        this.pathMap.put(nodeName, this.buildValue(node));
    }

    public String resolve(String path, Object... replacers) {
        JdaConfigMessage msg = this.message(path);
        if (msg == null) {
            return null;
        }
        return msg.getMessage(replacers);
    }

    public CompletableFuture<Void> sendPrivately(
            String path,
            User user,
            IReplyCallback dmDisabledFallbackEvent,
            Object... replacers
    ) {
        JdaConfigMessage message = this.message(path);
        if (message == null) {
            return CompletableFuture.completedFuture(null);
        }

        MessageCreateData data = message.serialized(replacers);
        String content = data.getContent();
        if ((content == null || content.isBlank()) && data.getEmbeds().isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        return user.openPrivateChannel()
                .submit()
                .thenAccept(channel -> channel.sendMessage(data).queue())
                .exceptionallyCompose(error -> {
                    if (dmDisabledFallbackEvent != null) {
                        reply(DIRECT_MESSAGE_DISABLED, dmDisabledFallbackEvent);
                    }
                    return CompletableFuture.completedFuture(null);
                });
    }
}
