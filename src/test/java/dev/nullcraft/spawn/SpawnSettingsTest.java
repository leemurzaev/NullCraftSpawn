package dev.nullcraft.spawn;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpawnSettingsTest {

    private static SpawnSettings parse(String yaml) throws InvalidConfigurationException {
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString(yaml);
        return SpawnSettings.from(config);
    }

    @Test
    void emptyConfigUsesTheDefaults() throws InvalidConfigurationException {
        SpawnSettings settings = parse("");
        assertEquals("world", settings.world());
        assertTrue(settings.useWorldSpawn());
        assertEquals(350, settings.radius());
        assertTrue(settings.newPlayers());
        assertTrue(settings.onDeath());
        assertTrue(settings.respectBeds());
        assertEquals(Set.of("world", "world_nether", "world_the_end"), settings.deathWorlds());
        assertEquals(40, settings.maxAttempts());
        assertEquals(6, settings.poolSize());
        assertTrue(settings.unsafeBlocks().contains("lava"));
    }

    @Test
    void readsCustomValues() throws InvalidConfigurationException {
        SpawnSettings settings = parse("""
                world: anarchy
                center:
                  use-world-spawn: false
                  x: 100
                  z: -50
                radius: 500
                new-players: false
                respect-beds: false
                death-worlds: [Anarchy, anarchy_nether]
                max-attempts: 10
                pool-size: 2
                unsafe-blocks: [LAVA]
                """);
        assertEquals("anarchy", settings.world());
        assertFalse(settings.useWorldSpawn());
        assertEquals(100, settings.centerX());
        assertEquals(-50, settings.centerZ());
        assertEquals(500, settings.radius());
        assertFalse(settings.newPlayers());
        assertFalse(settings.respectBeds());
        assertEquals(Set.of("anarchy", "anarchy_nether"), settings.deathWorlds());
        assertEquals(10, settings.maxAttempts());
        assertEquals(2, settings.poolSize());
        assertEquals(Set.of("lava"), settings.unsafeBlocks());
    }

    @Test
    void radiusBelowOneFallsBackToTheDefault() throws InvalidConfigurationException {
        assertEquals(350, parse("radius: 0").radius());
    }

    @Test
    void readsTheMessages() throws InvalidConfigurationException {
        SpawnSettings settings = parse("""
                messages:
                  tag: '[N]'
                  first-join: ''
                  respawn: '{tag} back at {x}, {z}'
                """);
        assertEquals("[N]", settings.messages().tag());
        assertEquals("", settings.messages().firstJoin());
        assertEquals("{tag} back at {x}, {z}", settings.messages().respawn());
    }
}
