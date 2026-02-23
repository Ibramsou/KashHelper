package fr.ibrakash.helper.configuration.serialization;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;

@ParametersAreNonnullByDefault
public class WorldSerializer implements TypeSerializer<World> {

    private static final WorldSerializer INSTANCE = new WorldSerializer();

    public static WorldSerializer get() {
        return INSTANCE;
    }

    @Override
    public World deserialize(Type type, ConfigurationNode node) throws SerializationException {
        final String worldName = node.getString() == null ? "null" : node.getString();
        final World world = Bukkit.getWorld(worldName);
        if (world == null) throw new SerializationException("Could not find world with name " + worldName);
        return world;
    }

    @Override
    public void serialize(Type type, @Nullable World obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.set("null");
            return;
        }

        node.set(obj.getName());
    }
}
