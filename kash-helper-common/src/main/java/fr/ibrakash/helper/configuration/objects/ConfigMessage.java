package fr.ibrakash.helper.configuration.objects;

import fr.ibrakash.helper.text.TextUtil;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public abstract class ConfigMessage<S, A> {

    private String message = "Not configured";

    protected ConfigMessage() {
    }

    protected ConfigMessage(String message) {
        this.message = normalize(message);
    }

    public String getMessage(Object... replacers) {
        return TextUtil.replaced(this.message, replacers);
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = normalize(message);
    }

    public void send(A audience, Object... replacers) {
        this.sendSerialized(audience, this.serialized(replacers), replacers);
    }

    public void broadcast(Object... replacers) {
        this.broadcastSerialized(this.serialized(replacers), replacers);
    }

    protected String normalize(String message) {
        return (message == null || message.isBlank()) ? "Not configured" : message;
    }

    protected abstract void sendSerialized(A audience, S serialized, Object... replacers);

    protected abstract void broadcastSerialized(S serialized, Object... replacers);

    public abstract S serialized(Object... replacers);
}
