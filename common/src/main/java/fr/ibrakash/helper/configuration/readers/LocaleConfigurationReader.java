package fr.ibrakash.helper.configuration.readers;

import fr.ibrakash.helper.configuration.objects.ConfigMessage;
import fr.ibrakash.helper.platform.KashAddon;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;
import java.util.Objects;

public abstract class LocaleConfigurationReader<V, R, M extends ConfigMessage<V, R>> extends SingleConfigurationReader<M> {

    protected LocaleConfigurationReader(KashAddon<?> addon) {
        super(addon);
    }

    public void send(String path, R audience, Object... replacers) {
        M message = this.get(path);
        if (message != null) {
            message.send(audience, replacers);
        }
    }

    public void broadcast(String path, Object... replacers) {
        M message = this.get(path);
        if (message != null) {
            message.broadcast(replacers);
        }
    }

    public V serialized(String path, Object... replacers) {
        M message = this.get(path);
        return (message != null) ? message.serialized(replacers) : null;
    }

    public M get(String path) {
        return this.resolve(path);
    }

    @Override
    public M buildValue(ConfigurationNode node) throws SerializationException {
        if (node.isMap()) {
            M entry = node.get(this.messageType());
            return Objects.requireNonNullElseGet(entry, this::fallBackValue);
        }

        if (node.isList()) {
            List<String> lines = node.getList(String.class);
            String merged = (lines == null || lines.isEmpty()) ? null : String.join("\n", lines);
            return this.fromString(merged);
        }

        return this.fromString(node.getString());
    }

    @Override
    public M fallBackValue() {
        return this.fromString(null);
    }

    protected abstract Class<M> messageType();

    protected abstract M fromString(String value);
}