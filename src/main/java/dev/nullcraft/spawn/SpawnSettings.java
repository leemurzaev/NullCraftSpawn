package dev.nullcraft.spawn;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public record SpawnSettings(String world, boolean useWorldSpawn, int centerX, int centerZ, int radius,
                            boolean newPlayers, boolean onDeath, boolean respectBeds, Set<String> deathWorlds,
                            int maxAttempts, int poolSize, Set<String> unsafeBlocks, Messages messages) {

    /** Texts with &amp; colour codes. An empty text is not sent. Placeholders: {tag} {player} {x} {z}. */
    public record Messages(String tag, String firstJoinTitle, String firstJoinSubtitle, String firstJoin,
                           String respawn) {
    }

    private static final int DEFAULT_RADIUS = 350;
    private static final List<String> DEFAULT_DEATH_WORLDS = List.of("world", "world_nether", "world_the_end");
    private static final List<String> DEFAULT_UNSAFE_BLOCKS = List.of("lava", "magma_block", "cactus", "fire",
            "soul_fire", "campfire", "soul_campfire", "powder_snow", "sweet_berry_bush", "wither_rose",
            "pointed_dripstone", "cobweb");

    public static SpawnSettings from(ConfigurationSection config) {
        int radius = config.getInt("radius", DEFAULT_RADIUS);
        return new SpawnSettings(
                config.getString("world", "world"),
                config.getBoolean("center.use-world-spawn", true),
                config.getInt("center.x", 0),
                config.getInt("center.z", 0),
                radius >= 1 ? radius : DEFAULT_RADIUS,
                config.getBoolean("new-players", true),
                config.getBoolean("on-death", true),
                config.getBoolean("respect-beds", true),
                lowerCase(config.isList("death-worlds") ? config.getStringList("death-worlds") : DEFAULT_DEATH_WORLDS),
                Math.max(1, config.getInt("max-attempts", 40)),
                Math.max(1, config.getInt("pool-size", 6)),
                lowerCase(config.isList("unsafe-blocks") ? config.getStringList("unsafe-blocks") : DEFAULT_UNSAFE_BLOCKS),
                new Messages(
                        config.getString("messages.tag", "&8[&5&lNULL&d&lCRAFT &f&lBETA&8]"),
                        config.getString("messages.first-join-title", "&5&lNULL&d&lCRAFT"),
                        config.getString("messages.first-join-subtitle", "&7OP ANARCHY &8| &f&lBETA"),
                        config.getString("messages.first-join",
                                "{tag} &7Welcome &f{player}&7! You spawned at &f{x}&7, &f{z}&7."),
                        config.getString("messages.respawn", "{tag} &7You died and respawned at &f{x}&7, &f{z}&7.")));
    }

    private static Set<String> lowerCase(List<String> names) {
        return names.stream().map(name -> name.toLowerCase(Locale.ROOT)).collect(Collectors.toUnmodifiableSet());
    }
}
