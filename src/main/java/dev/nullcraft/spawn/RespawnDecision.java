package dev.nullcraft.spawn;

import java.util.Locale;
import java.util.Set;

public final class RespawnDecision {

    private RespawnDecision() {
    }

    /**
     * @param causedByDeath true for a respawn after dying (not after leaving the End)
     * @param deathWorld    the world the player died in
     * @param bedOrAnchor   the server is respawning the player at a bed or charged respawn anchor
     * @param deathWorlds   lower-case names of the worlds whose deaths lead to a random spot
     */
    public static RespawnAction decide(boolean causedByDeath, String deathWorld, boolean bedOrAnchor,
                                       boolean respectBeds, Set<String> deathWorlds) {
        if (!causedByDeath || !deathWorlds.contains(deathWorld.toLowerCase(Locale.ROOT))) {
            return RespawnAction.LEAVE;
        }
        if (bedOrAnchor && respectBeds) {
            return RespawnAction.KEEP_BED;
        }
        return RespawnAction.RANDOM;
    }
}
