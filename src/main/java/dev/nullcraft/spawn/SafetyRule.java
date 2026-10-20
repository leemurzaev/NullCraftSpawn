package dev.nullcraft.spawn;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/** A spot is safe when the ground holds the player and the two blocks above are free and harmless. */
public final class SafetyRule {

    private final Set<String> unsafeMaterials;

    public SafetyRule(Set<String> unsafeMaterials) {
        this.unsafeMaterials = unsafeMaterials.stream()
                .map(name -> name.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    public boolean isSafe(BlockInfo ground, BlockInfo feet, BlockInfo head) {
        return ground.solid() && !ground.liquid() && !isUnsafe(ground) && isFree(feet) && isFree(head);
    }

    private boolean isFree(BlockInfo block) {
        return block.passable() && !block.liquid() && !isUnsafe(block);
    }

    private boolean isUnsafe(BlockInfo block) {
        return unsafeMaterials.contains(block.material().toLowerCase(Locale.ROOT));
    }
}
