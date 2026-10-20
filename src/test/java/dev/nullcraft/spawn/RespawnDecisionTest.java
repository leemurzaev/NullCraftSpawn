package dev.nullcraft.spawn;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RespawnDecisionTest {

    private static final Set<String> WORLDS = Set.of("world", "world_nether", "world_the_end");

    @Test
    void deathWithoutBedGoesToRandomSpot() {
        assertEquals(RespawnAction.RANDOM, RespawnDecision.decide(true, "world", false, true, WORLDS));
    }

    @Test
    void deathWithBedKeepsTheBedWhenBedsWin() {
        assertEquals(RespawnAction.KEEP_BED, RespawnDecision.decide(true, "world", true, true, WORLDS));
    }

    @Test
    void deathWithBedGoesToRandomSpotWhenBedsDoNotWin() {
        assertEquals(RespawnAction.RANDOM, RespawnDecision.decide(true, "world", true, false, WORLDS));
    }

    @Test
    void respawnThatIsNotADeathIsLeftAlone() {
        assertEquals(RespawnAction.LEAVE, RespawnDecision.decide(false, "world_the_end", false, true, WORLDS));
    }

    @Test
    void deathInAnotherWorldIsLeftAlone() {
        assertEquals(RespawnAction.LEAVE, RespawnDecision.decide(true, "plots", false, true, WORLDS));
    }

    @Test
    void deathWorldNamesIgnoreCase() {
        assertEquals(RespawnAction.RANDOM, RespawnDecision.decide(true, "World_Nether", false, true, WORLDS));
    }
}
