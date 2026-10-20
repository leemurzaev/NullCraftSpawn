package dev.nullcraft.spawn;

import java.util.random.RandomGenerator;

/** A square around a center: X and Z are each within center +/- radius (inclusive). */
public record SpawnArea(int centerX, int centerZ, int radius) {

    public SpawnArea {
        if (radius < 1) {
            throw new IllegalArgumentException("radius must be at least 1, was " + radius);
        }
    }

    public record Column(int x, int z) {
    }

    public Column randomColumn(RandomGenerator random) {
        return new Column(centerX + random.nextInt(-radius, radius + 1),
                centerZ + random.nextInt(-radius, radius + 1));
    }
}
