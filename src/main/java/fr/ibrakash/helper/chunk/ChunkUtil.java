package fr.ibrakash.helper.chunk;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ChunkUtil {

    private static final ExecutorService CHUNK_GENERATOR_SERVICE = Executors.newCachedThreadPool();

    public static CompletableFuture<List<Chunk>> getGenerateChunksIn(Location pos1, Location pos2) {
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX()) >> 4;
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX()) >> 4;
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ()) >> 4;
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ()) >> 4;
        int width  = (maxX - minX) + 1;
        int height = (maxZ - minZ) + 1;
        int chunkCount = width * height;

        final World world = pos1.getWorld();

        CountDownLatch latch = new CountDownLatch(chunkCount);
        List<Chunk> generatedChunks = new ArrayList<>();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (world.isChunkLoaded(x, z)) {
                    generatedChunks.add(world.getChunkAt(x, z));
                    latch.countDown();
                } else {
                    world.getChunkAtAsync(x, z, true).thenAccept(chunk -> {
                        generatedChunks.add(chunk);
                        latch.countDown();
                    });
                }
            }
        }

        if (generatedChunks.size() == chunkCount) {
            return CompletableFuture.completedFuture(generatedChunks);
        }

        CompletableFuture<List<Chunk>> future = new CompletableFuture<>();

        CHUNK_GENERATOR_SERVICE.execute(() -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            future.complete(generatedChunks);
        });

        return future;
    }

    public static void getChunksIn(Location pos1, Location pos2, Consumer<Chunk> consumer) {
        getChunksIn(pos1, pos2, (world, x, z) -> consumer.accept(world.getChunkAt(x, z)));
    }

    public static void getChunkPositionsIn(Location pos1, Location pos2, Consumer<ChunkPosition> consumer) {
        getChunksIn(pos1, pos2, (world, x, z) -> consumer.accept(new ChunkPosition(world.getName(), x, z)));
    }

    public static void getChunksIn(Location pos1, Location pos2, ChunkConsumer consumer) {
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX()) >> 4;
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX()) >> 4;
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ()) >> 4;
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ()) >> 4;

        final World world = pos1.getWorld();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                consumer.accept(world, x, z);
            }
        }
    }

    public static Set<ChunkPosition> getViewingChunks(Location location, int viewDistance) {
        return getViewingChunks(ChunkPosition.from(location), viewDistance);
    }

    public static Set<ChunkPosition> getViewingChunks(ChunkPosition position, int viewDistance) {
        Set<ChunkPosition> viewingChunks = new HashSet<>();
        fillViewingChunks(position, new HashSet<>(), viewDistance);
        return viewingChunks;
    }

    public static void fillViewingChunks(ChunkPosition position, Set<ChunkPosition> viewingChunks, int viewDistance) {
        viewingChunks.clear();

        int rangeBlocks = Math.max(0, viewDistance);

        int chunkRadius = (rangeBlocks + 15) >> 4;

        int centerX = position.chunkX();
        int centerZ = position.chunkZ();
        var worldId = position.worldName();

        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                int x = centerX + dx;
                int z = centerZ + dz;

                viewingChunks.add(new ChunkPosition(worldId, x, z));
            }
        }
    }

    public static void recalculateViewingChunks(ChunkPosition oldChunk, ChunkPosition newChunk, Set<ChunkPosition> viewingChunks, int viewDistance) {
        if (!oldChunk.worldName().equals(newChunk.worldName())) {
            fillViewingChunks(newChunk, viewingChunks, viewDistance);
            return;
        }

        int range = (Math.max(0, viewDistance) + 15) >> 4;

        int oldX = oldChunk.chunkX();
        int oldZ = oldChunk.chunkZ();
        int newX = newChunk.chunkX();
        int newZ = newChunk.chunkZ();

        int dx = newX - oldX;
        int dz = newZ - oldZ;

        if (Math.abs(dx) > 1 || Math.abs(dz) > 1) {
            fillViewingChunks(newChunk, viewingChunks, viewDistance);
            return;
        }

        if (dx == 0 && dz == 0) return;

        String world = oldChunk.worldName();

        // Shift X
        if (dx == 1) { // Est
            int removeX = oldX - range;
            int addX    = newX + range;
            removeColumn(world, removeX, newZ, range, viewingChunks);
            addColumn(world, addX, newZ, range, viewingChunks);
        } else if (dx == -1) { // West
            int removeX = oldX + range;
            int addX    = newX - range;
            removeColumn(world, removeX, newZ, range, viewingChunks);
            addColumn(world, addX, newZ, range, viewingChunks);
        }

        // Shift Z
        if (dz == 1) { // South
            int removeZ = oldZ - range;
            int addZ    = newZ + range;
            removeRow(world, newX, removeZ, range, viewingChunks);
            addRow(world, newX, addZ, range, viewingChunks);
        } else if (dz == -1) { // North
            int removeZ = oldZ + range;
            int addZ    = newZ - range;
            removeRow(world, newX, removeZ, range, viewingChunks);
            addRow(world, newX, addZ, range, viewingChunks);
        }
    }

    private static void addColumn(String world, int x, int centerZ, int range, Set<ChunkPosition> viewingChunks) {
        for (int z = centerZ - range; z <= centerZ + range; z++) {
            viewingChunks.add(new ChunkPosition(world, x, z));
        }
    }

    private static void removeColumn(String world, int x, int centerZ, int range, Set<ChunkPosition> viewingChunks) {
        for (int z = centerZ - range; z <= centerZ + range; z++) {
            viewingChunks.remove(new ChunkPosition(world, x, z));
        }
    }

    private static void addRow(String world, int centerX, int z, int range, Set<ChunkPosition> viewingChunks) {
        for (int x = centerX - range; x <= centerX + range; x++) {
            viewingChunks.add(new ChunkPosition(world, x, z));
        }
    }

    private static void removeRow(String world, int centerX, int z, int range, Set<ChunkPosition> viewingChunks) {
        for (int x = centerX - range; x <= centerX + range; x++) {
            viewingChunks.remove(new ChunkPosition(world, x, z));
        }
    }
}
