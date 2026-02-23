package fr.ibrakash.helper.configuration.serialization;

import fr.ibrakash.helper.utils.LegacyUtil;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;
import java.util.Objects;

@ParametersAreNonnullByDefault
public class SoundSerializer implements TypeSerializer<Sound> {

    private static final SoundSerializer INSTANCE = new SoundSerializer();

    public static SoundSerializer get() {
        return INSTANCE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public Sound deserialize(Type type, ConfigurationNode node) throws SerializationException {
        final String typeName = node.getString() == null ? "ENTITY_VILLAGER_NO" : node.getString().toUpperCase();
        try {
            return LegacyUtil.getSound(typeName);
        } catch (Exception exception) {
            throw new SerializationException(typeName + " is not a valid sound name");
        }
    }

    @Override
    public void serialize(Type type, @Nullable Sound obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.set("ENTITY_VILLAGER_NO");
            return;
        }

        node.set(Objects.requireNonNull(Registry.SOUNDS.getKey(obj)).getKey().toUpperCase().replace("\\.", "_"));
    }
}