package dev.nullcraft.spawn;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpawnAreaTest {

    @Test
    void randomColumnsStayInsideTheSquare() {
        SpawnArea area = new SpawnArea(0, 0, 350);
        Random random = new Random(42);
        for (int i = 0; i < 10_000; i++) {
            SpawnArea.Column column = area.randomColumn(random);
            assertTrue(column.x() >= -350 && column.x() <= 350, "x out of range: " + column.x());
            assertTrue(column.z() >= -350 && column.z() <= 350, "z out of range: " + column.z());
        }
    }

    @Test
    void squareIsCenteredOnTheGivenCenter() {
        SpawnArea area = new SpawnArea(100, -200, 50);
        Random random = new Random(7);
        for (int i = 0; i < 5_000; i++) {
            SpawnArea.Column column = area.randomColumn(random);
            assertTrue(column.x() >= 50 && column.x() <= 150, "x out of range: " + column.x());
            assertTrue(column.z() >= -250 && column.z() <= -150, "z out of range: " + column.z());
        }
    }

    @Test
    void bothEdgesCanBeChosen() {
        SpawnArea area = new SpawnArea(0, 0, 1);
        Random random = new Random(1);
        Set<Integer> xs = new HashSet<>();
        for (int i = 0; i < 1_000; i++) {
            xs.add(area.randomColumn(random).x());
        }
        assertEquals(Set.of(-1, 0, 1), xs);
    }

    @Test
    void rejectsRadiusBelowOne() {
        assertThrows(IllegalArgumentException.class, () -> new SpawnArea(0, 0, 0));
    }
}
