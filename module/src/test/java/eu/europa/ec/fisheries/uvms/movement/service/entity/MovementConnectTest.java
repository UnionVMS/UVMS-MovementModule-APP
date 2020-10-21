package eu.europa.ec.fisheries.uvms.movement.service.entity;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class MovementConnectTest {
    @Test
    public void compareTo_equalIds_expectEquals() {
        // Given
        UUID uuid = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");

        MovementConnect movementConnect1 = new MovementConnect();
        movementConnect1.setId(uuid);

        MovementConnect movementConnect2 = new MovementConnect();
        movementConnect2.setId(uuid);

        // When
        int i = movementConnect1.compareTo(movementConnect2);

        // Then
        assertEquals(0, i);
    }

    @Test
    public void compareTo_notEqualIds_expectNotEquals() {
        // Given
        UUID uuid1 = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        UUID uuid2 = UUID.fromString("a28a6cff-f193-4f24-91ec-19bac9f3b44b");

        MovementConnect movementConnect1 = new MovementConnect();
        movementConnect1.setId(uuid1);

        MovementConnect movementConnect2 = new MovementConnect();
        movementConnect2.setId(uuid2);

        // When
        int i = movementConnect1.compareTo(movementConnect2);

        // Then
        assertEquals(1, i);
    }

    @Test
    public void compareTo_argumentNull_expectNotEquals() {
        // Given
        MovementConnect movementConnect = new MovementConnect();

        // When
        int i = movementConnect.compareTo(null);

        // Then
        assertEquals(1, i);
    }
}
