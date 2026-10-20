package dev.nullcraft.spawn;

/** What to do with a respawn. */
public enum RespawnAction {
    /** Put the player on a random safe spot. */
    RANDOM,
    /** The player's bed or respawn anchor wins: restore the vanilla respawn location. */
    KEEP_BED,
    /** Not ours to handle. */
    LEAVE
}
