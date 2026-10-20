package dev.nullcraft.spawn;

import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

/** Finds safe spots inside the spawn area. Everything except {@link #findAsync()} runs on the main thread. */
final class SafeSpotFinder {

    private final World world;
    private final SpawnArea area;
    private final SafetyRule rule;
    private final int maxAttempts;

    SafeSpotFinder(World world, SpawnArea area, SafetyRule rule, int maxAttempts) {
        this.world = world;
        this.area = area;
        this.rule = rule;
        this.maxAttempts = maxAttempts;
    }

    World world() {
        return world;
    }

    SpawnArea area() {
        return area;
    }

    /** Loads chunks asynchronously (never generates new ones); completes with null after maxAttempts misses. */
    CompletableFuture<Location> findAsync() {
        return attempt(1);
    }

    private CompletableFuture<Location> attempt(int number) {
        if (number > maxAttempts) {
            return CompletableFuture.completedFuture(null);
        }
        SpawnArea.Column column = area.randomColumn(ThreadLocalRandom.current());
        // Paper completes chunk futures on the main thread, so check() is safe here.
        return world.getChunkAtAsync(column.x() >> 4, column.z() >> 4, false).thenCompose(chunk -> {
            Location spot = chunk == null ? null : check(column.x(), column.z());
            return spot != null ? CompletableFuture.completedFuture(spot) : attempt(number + 1);
        });
    }

    /** Fallback when no ready spot is left: may load chunks synchronously. */
    Location findNow() {
        for (int i = 0; i < maxAttempts; i++) {
            SpawnArea.Column column = area.randomColumn(ThreadLocalRandom.current());
            if (!world.isChunkGenerated(column.x() >> 4, column.z() >> 4)) {
                continue;
            }
            Location spot = check(column.x(), column.z());
            if (spot != null) {
                return spot;
            }
        }
        return null;
    }

    /** Where to stand on top of this column, or null when it is not safe. */
    Location check(int x, int z) {
        Block ground = world.getHighestBlockAt(x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES);
        if (ground.getY() <= world.getMinHeight() || ground.getY() + 2 >= world.getMaxHeight()) {
            return null;
        }
        Block feet = ground.getRelative(BlockFace.UP);
        if (!rule.isSafe(info(ground), info(feet), info(feet.getRelative(BlockFace.UP)))) {
            return null;
        }
        float yaw = ThreadLocalRandom.current().nextFloat() * 360f - 180f;
        return new Location(world, x + 0.5, ground.getY() + 1, z + 0.5, yaw, 0f);
    }

    /** Players can change blocks at any time, so a stored spot is checked again before use. */
    boolean isStillSafe(Location spot) {
        Block feet = spot.getBlock();
        return rule.isSafe(info(feet.getRelative(BlockFace.DOWN)), info(feet), info(feet.getRelative(BlockFace.UP)));
    }

    private static BlockInfo info(Block block) {
        return new BlockInfo(block.getType().name().toLowerCase(Locale.ROOT), block.getType().isSolid(),
                block.isLiquid(), block.isPassable());
    }
}
