package dev.nullcraft.spawn;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Map;

/** Player-facing texts from the messages section of config.yml. */
final class Messenger {

    private static final Title.Times TITLE_TIMES =
            Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(4), Duration.ofSeconds(1));

    private final NullCraftSpawn plugin;

    Messenger(NullCraftSpawn plugin) {
        this.plugin = plugin;
    }

    void welcomed(Player player, Location spot) {
        SpawnSettings.Messages messages = plugin.settings().messages();
        Map<String, String> values = values(messages, player, spot);
        String title = MessageTemplate.fill(messages.firstJoinTitle(), values);
        String subtitle = MessageTemplate.fill(messages.firstJoinSubtitle(), values);
        if (!title.isEmpty() || !subtitle.isEmpty()) {
            player.showTitle(Title.title(color(title), color(subtitle), TITLE_TIMES));
        }
        send(player, messages.firstJoin(), values);
    }

    /** Sent one tick later, so it arrives after the respawn screen closes. */
    void respawned(Player player, Location spot) {
        SpawnSettings.Messages messages = plugin.settings().messages();
        Map<String, String> values = values(messages, player, spot);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                send(player, messages.respawn(), values);
            }
        }, 1L);
    }

    /** For admin command replies: the tag, then the text. */
    void admin(CommandSender sender, String text) {
        sender.sendMessage(color(plugin.settings().messages().tag() + " &7" + text));
    }

    private static void send(Player player, String template, Map<String, String> values) {
        if (!template.isEmpty()) {
            player.sendMessage(color(MessageTemplate.fill(template, values)));
        }
    }

    private static Map<String, String> values(SpawnSettings.Messages messages, Player player, Location spot) {
        return Map.of("tag", messages.tag(), "player", player.getName(),
                "x", Integer.toString(spot.getBlockX()), "z", Integer.toString(spot.getBlockZ()));
    }

    private static Component color(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }
}
