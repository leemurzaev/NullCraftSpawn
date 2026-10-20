package dev.nullcraft.spawn;

/** What the safety check needs to know about one block. */
public record BlockInfo(String material, boolean solid, boolean liquid, boolean passable) {
}
