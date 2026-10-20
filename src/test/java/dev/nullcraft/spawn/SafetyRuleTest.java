package dev.nullcraft.spawn;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SafetyRuleTest {

    private static final BlockInfo AIR = new BlockInfo("air", false, false, true);
    private static final BlockInfo GRASS = new BlockInfo("grass_block", true, false, false);

    private final SafetyRule rule = new SafetyRule(Set.of("lava", "magma_block", "cactus", "fire", "sweet_berry_bush"));

    @Test
    void solidGroundWithTwoFreeBlocksAboveIsSafe() {
        assertTrue(rule.isSafe(GRASS, AIR, AIR));
    }

    @Test
    void harmlessPlantAtFeetIsSafe() {
        assertTrue(rule.isSafe(GRASS, new BlockInfo("short_grass", false, false, true), AIR));
    }

    @Test
    void waterSurfaceIsUnsafe() {
        assertFalse(rule.isSafe(new BlockInfo("water", false, true, true), AIR, AIR));
    }

    @Test
    void unsafeGroundBlockIsUnsafe() {
        assertFalse(rule.isSafe(new BlockInfo("magma_block", true, false, false), AIR, AIR));
    }

    @Test
    void nonSolidGroundIsUnsafe() {
        assertFalse(rule.isSafe(new BlockInfo("short_grass", false, false, true), AIR, AIR));
    }

    @Test
    void blockedFeetIsUnsafe() {
        assertFalse(rule.isSafe(GRASS, new BlockInfo("stone", true, false, false), AIR));
    }

    @Test
    void blockedHeadIsUnsafe() {
        assertFalse(rule.isSafe(GRASS, AIR, new BlockInfo("oak_leaves", true, false, false)));
    }

    @Test
    void liquidAtFeetIsUnsafe() {
        assertFalse(rule.isSafe(GRASS, new BlockInfo("water", false, true, true), AIR));
    }

    @Test
    void harmfulPassableBlockAtFeetIsUnsafe() {
        assertFalse(rule.isSafe(GRASS, new BlockInfo("sweet_berry_bush", false, false, true), AIR));
    }

    @Test
    void unsafeNamesIgnoreCase() {
        SafetyRule upperCase = new SafetyRule(Set.of("MAGMA_BLOCK"));
        assertFalse(upperCase.isSafe(new BlockInfo("magma_block", true, false, false), AIR, AIR));
    }
}
