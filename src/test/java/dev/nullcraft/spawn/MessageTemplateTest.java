package dev.nullcraft.spawn;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageTemplateTest {

    @Test
    void replacesThePlaceholders() {
        String text = MessageTemplate.fill("{tag} Welcome {player}! You spawned at {x}, {z}.",
                Map.of("tag", "[N]", "player", "Steve", "x", "12", "z", "-40"));
        assertEquals("[N] Welcome Steve! You spawned at 12, -40.", text);
    }

    @Test
    void leavesUnknownPlaceholdersAsTheyAre() {
        assertEquals("hi {who}", MessageTemplate.fill("hi {who}", Map.of("x", "1")));
    }
}
