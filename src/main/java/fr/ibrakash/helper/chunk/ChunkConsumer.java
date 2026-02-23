package fr.ibrakash.helper.chunk;

import org.bukkit.World;

@FunctionalInterface
public interface ChunkConsumer {

    void accept(World world, int x, int z);
}
