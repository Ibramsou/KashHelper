package fr.ibrakash.helper.chunk;

import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.Objects;

public record ChunkPosition(String worldName, int chunkX, int chunkZ) {

    public static ChunkPosition of(Chunk chunk) {
        return new ChunkPosition(
                chunk.getWorld().getName(),
                chunk.getX(),
                chunk.getZ()
        );
    }

    public static ChunkPosition from(Location location) {
        return new ChunkPosition(
                Objects.requireNonNull(location.getWorld(), "location world is null").getName(),
                location.getBlockX() >> 4,
                location.getBlockZ() >> 4
        );
    }

}
