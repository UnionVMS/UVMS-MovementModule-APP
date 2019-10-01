package eu.europa.ec.fisheries.uvms.movement.service.entity;

import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.assertEquals;

public class MovementTest {

    @Test
    public void compareTo_equalTimestamps_expectEquals() {
        // Given
        Instant now = Instant.now();

        Movement movement1 = new Movement();
        movement1.setTimestamp(now);

        Movement movement2 = new Movement();
        movement2.setTimestamp(now);

        // When
        int i = movement1.compareTo(movement2);

        // Then
        assertEquals(0, i);
    }

    @Test
    public void compareTo_notEqualTimestamps_expectNotEquals() {
        // Given
        Instant now = Instant.now();

        Movement movement1 = new Movement();
        movement1.setTimestamp(now);

        Instant later = now.plusSeconds(5);
        Movement movement2 = new Movement();
        movement2.setTimestamp(later);

        // When
        int i = movement1.compareTo(movement2);

        // Then
        assertEquals(-1, i);
    }

    @Test
    public void compareTo_argumentNull_expectNotEquals() {
        // Given
        Movement movement = new Movement();

        // When
        int i = movement.compareTo(null);

        // Then
        assertEquals(1, i);
    }
}
