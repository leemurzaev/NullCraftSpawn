package dev.nullcraft.spawn;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;
import java.util.Locale;

/** /ncspawn reload | status | test */
final class SpawnCommand implements TabExecutor {

    private static final List<String> SUBCOMMANDS = List.of("reload", "status", "test");

    private final NullCraftSpawn plugin;

    SpawnCommand(NullCraftSpawn plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String sub = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
        Messenger messenger = plugin.messenger();
        switch (sub) {
            case "reload" -> {
                boolean active = plugin.reload();
                messenger.admin(sender, active ? "Reloaded. " + describe() : "Reloaded, but the world was not found: random spawn is off.");
            }
            case "status" -> {
                SpotPool pool = plugin.pool();
                if (pool == null) {
                    messenger.admin(sender, "Random spawn is off: the world was not found.");
                    return true;
                }
                messenger.admin(sender, describe());
                messenger.admin(sender, "Ready spots: " + pool.ready() + "/" + pool.size());
                for (Location spot : pool.readySpots()) {
                    messenger.admin(sender, " - " + spot.getBlockX() + ", " + spot.getBlockY() + ", " + spot.getBlockZ());
                }
            }
            case "test" -> {
                SpotPool pool = plugin.pool();
                if (!(sender instanceof Player player)) {
                    messenger.admin(sender, "Only a player can test, use status from the console.");
                } else if (pool == null) {
                    messenger.admin(sender, "Random spawn is off: the world was not found.");
                } else {
                    Location spot = pool.take();
                    if (spot == null) {
                        messenger.admin(sender, "No safe spot found.");
                    } else {
                        player.teleportAsync(spot, PlayerTeleportEvent.TeleportCause.PLUGIN);
                        messenger.admin(sender, "Test spot: " + spot.getBlockX() + ", " + spot.getBlockY() + ", " + spot.getBlockZ());
                    }
                }
            }
            default -> messenger.admin(sender, "/" + label + " <reload|status|test>");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) {
            return List.of();
        }
        String typed = args[0].toLowerCase(Locale.ROOT);
        return SUBCOMMANDS.stream().filter(sub -> sub.startsWith(typed)).toList();
    }

    private String describe() {
        SpawnSettings settings = plugin.settings();
        SpotPool pool = plugin.pool();
        SpawnArea area = pool.finder().area();
        return "World " + pool.finder().world().getName()
                + ", X " + (area.centerX() - area.radius()) + " to " + (area.centerX() + area.radius())
                + ", Z " + (area.centerZ() - area.radius()) + " to " + (area.centerZ() + area.radius())
                + ", new players " + onOff(settings.newPlayers())
                + ", deaths " + onOff(settings.onDeath())
                + ", beds win " + onOff(settings.respectBeds()) + ".";
    }

    private static String onOff(boolean value) {
        return value ? "on" : "off";
    }
}
