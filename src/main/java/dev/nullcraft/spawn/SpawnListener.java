package dev.nullcraft.spawn;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class SpawnListener implements Listener {

    /** Ticks after joining before a new player is moved, so other join handlers finish first. */
    private static final long NEW_PLAYER_DELAY_TICKS = 2L;

    private final NullCraftSpawn plugin;
    /** The server's own respawn location, before other plugins (EssentialsSpawn, Multiverse) change it. */
    private final Map<UUID, Location> vanillaRespawn = new HashMap<>();

    SpawnListener(NullCraftSpawn plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void rememberVanillaRespawn(PlayerRespawnEvent event) {
        vanillaRespawn.put(event.getPlayer().getUniqueId(), event.getRespawnLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void chooseRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location vanilla = vanillaRespawn.remove(player.getUniqueId());
        SpawnSettings settings = plugin.settings();
        SpotPool pool = plugin.pool();
        if (!settings.onDeath() || pool == null) {
            return;
        }
        RespawnAction action = RespawnDecision.decide(
                event.getRespawnReason() == PlayerRespawnEvent.RespawnReason.DEATH,
                player.getWorld().getName(),
                event.isBedSpawn() || event.isAnchorSpawn(),
                settings.respectBeds(),
                settings.deathWorlds());
        switch (action) {
            case KEEP_BED -> {
                if (vanilla != null) {
                    event.setRespawnLocation(vanilla);
                }
            }
            case RANDOM -> {
                Location spot = pool.take();
                if (spot != null) {
                    event.setRespawnLocation(spot);
                    plugin.messenger().respawned(player, spot);
                }
            }
            case LEAVE -> {
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void placeNewPlayer(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPlayedBefore() || !plugin.settings().newPlayers()) {
            return;
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            SpotPool pool = plugin.pool();
            if (!player.isOnline() || pool == null) {
                return;
            }
            Location spot = pool.take();
            if (spot == null) {
                return;
            }
            player.teleportAsync(spot, PlayerTeleportEvent.TeleportCause.PLUGIN).thenAccept(moved -> {
                if (moved && player.isOnline()) {
                    plugin.messenger().welcomed(player, spot);
                }
            });
        }, NEW_PLAYER_DELAY_TICKS);
    }

    @EventHandler
    public void forget(PlayerQuitEvent event) {
        vanillaRespawn.remove(event.getPlayer().getUniqueId());
    }
}
