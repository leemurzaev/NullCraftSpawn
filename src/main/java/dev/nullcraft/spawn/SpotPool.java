package dev.nullcraft.spawn;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps a few safe spots ready, with their chunks held loaded, so a respawn never waits for a search.
 * Main thread only.
 */
final class SpotPool {

    /** Ticks a used spot's chunk stays held, so it is still loaded when the player arrives. */
    private static final long RELEASE_DELAY_TICKS = 100L;

    private final Plugin plugin;
    private final SafeSpotFinder finder;
    private final int size;
    private final Deque<Location> spots = new ArrayDeque<>();
    private final Map<Long, Integer> holds = new HashMap<>();
    private int searching;
    private boolean closed;

    SpotPool(Plugin plugin, SafeSpotFinder finder, int size) {
        this.plugin = plugin;
        this.finder = finder;
        this.size = size;
    }

    SafeSpotFinder finder() {
        return finder;
    }

    int ready() {
        return spots.size();
    }

    int size() {
        return size;
    }

    List<Location> readySpots() {
        return List.copyOf(spots);
    }

    /**
     * Starts at most the missing number of searches. A search can complete synchronously when its chunk is
     * already loaded, so a failed search must not start another one from here, or a fully unsafe area would
     * spin forever on the main thread.
     */
    void refill() {
        int missing = size - spots.size() - searching;
        for (int i = 0; i < missing && !closed && spots.size() + searching < size; i++) {
            searching++;
            finder.findAsync().whenComplete(this::onSearchDone);
        }
    }

    private void onSearchDone(Location spot, Throwable error) {
        searching--;
        if (closed) {
            return;
        }
        if (error != null) {
            plugin.getLogger().warning("Spot search failed: " + error);
        } else if (spot == null) {
            plugin.getLogger().warning("No safe spot found in the area; check radius and unsafe-blocks");
        } else {
            hold(spot);
            spots.add(spot);
            refill();
        }
    }

    /** A safe spot for a player right now, or null when none could be found. */
    Location take() {
        Location spot;
        while ((spot = spots.poll()) != null) {
            releaseLater(spot);
            if (finder.isStillSafe(spot)) {
                refill();
                return spot;
            }
        }
        refill();
        return finder.findNow();
    }

    void close() {
        closed = true;
        spots.clear();
        holds.clear();
        finder.world().removePluginChunkTickets(plugin);
    }

    private void hold(Location spot) {
        long key = chunkKey(spot);
        if (holds.merge(key, 1, Integer::sum) == 1) {
            finder.world().addPluginChunkTicket(spot.getBlockX() >> 4, spot.getBlockZ() >> 4, plugin);
        }
    }

    private void releaseLater(Location spot) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (closed) {
                return;
            }
            long key = chunkKey(spot);
            Integer left = holds.computeIfPresent(key, (k, count) -> count > 1 ? count - 1 : null);
            if (left == null) {
                World world = finder.world();
                world.removePluginChunkTicket(spot.getBlockX() >> 4, spot.getBlockZ() >> 4, plugin);
            }
        }, RELEASE_DELAY_TICKS);
    }

    private static long chunkKey(Location spot) {
        return ((long) (spot.getBlockX() >> 4) << 32) ^ ((spot.getBlockZ() >> 4) & 0xffffffffL);
    }
}
