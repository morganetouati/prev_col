package com.example.prevcol

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests unitaires pour DemoDetectionSimulator et ObjectType
 * Vérifie la logique de simulation et les types d'objets
 */
class DemoDetectionSimulatorTest {

    // ── Tests ObjectType ──────────────────────────────────────────────────────

    @Test
    fun `ObjectType values count is 6`() {
        assertEquals(6, ObjectType.values().size)
    }

    @Test
    fun `ObjectType ADULTE has correct properties`() {
        val adulte = ObjectType.ADULTE
        assertEquals("Adulte", adulte.label)
        assertEquals("🧑", adulte.icon)
        assertEquals(1.6f, adulte.heightRange.first, 0.01f)
        assertEquals(1.9f, adulte.heightRange.second, 0.01f)
    }

    @Test
    fun `ObjectType ENFANT has correct properties`() {
        val enfant = ObjectType.ENFANT
        assertEquals("Enfant", enfant.label)
        assertEquals("🧒", enfant.icon)
        assertEquals(1.1f, enfant.heightRange.first, 0.01f)
        assertEquals(1.5f, enfant.heightRange.second, 0.01f)
    }

    @Test
    fun `ObjectType BEBE has correct properties`() {
        val bebe = ObjectType.BEBE
        assertEquals("Bébé", bebe.label)
        assertEquals("👶", bebe.icon)
        assertEquals(0.8f, bebe.heightRange.first, 0.01f)
        assertEquals(1.1f, bebe.heightRange.second, 0.01f)
    }

    @Test
    fun `ObjectType dog types have increasing height ranges`() {
        val petit = ObjectType.PETIT_CHIEN.heightRange
        val moyen = ObjectType.MOYEN_CHIEN.heightRange
        val grand = ObjectType.GRAND_CHIEN.heightRange

        // Petit < Moyen < Grand
        assertTrue(petit.second <= moyen.first)
        assertTrue(moyen.second <= grand.first)
    }

    @Test
    fun `all ObjectTypes have non-empty labels`() {
        ObjectType.values().forEach { type ->
            assertTrue("${type.name} label is empty", type.label.isNotEmpty())
        }
    }

    @Test
    fun `all ObjectTypes have icon emojis`() {
        ObjectType.values().forEach { type ->
            assertTrue("${type.name} icon is empty", type.icon.isNotEmpty())
        }
    }

    // ── Tests MovementDirection ───────────────────────────────────────────────

    @Test
    fun `MovementDirection has 5 values`() {
        assertEquals(5, DemoDetectionSimulator.MovementDirection.values().size)
    }

    @Test
    fun `MovementDirection contains expected values`() {
        val values = DemoDetectionSimulator.MovementDirection.values().map { it.name }
        assertTrue(values.contains("APPROACHING"))
        assertTrue(values.contains("RECEDING"))
        assertTrue(values.contains("MOVING_LEFT"))
        assertTrue(values.contains("MOVING_RIGHT"))
        assertTrue(values.contains("STATIONARY"))
    }

    // ── Tests Thresholds ──────────────────────────────────────────────────────

    @Test
    fun `getAdaptedThresholds returns pair`() {
        val thresholds = DemoDetectionSimulator.getAdaptedThresholds()
        assertTrue(thresholds.first > 0)
        assertTrue(thresholds.second > 0)
        // Alert threshold > Danger threshold
        assertTrue(thresholds.first >= thresholds.second)
    }

    // ── Tests Simulation ──────────────────────────────────────────────────────

    @Test
    fun `simulateDetection returns nullable Float`() {
        // La simulation peut retourner null ou une distance
        val result = DemoDetectionSimulator.simulateDetection()
        // Si détection, distance entre 0.5 et 10
        if (result != null) {
            assertTrue("Distance should be >= 0.5", result >= 0.5f)
            assertTrue("Distance should be <= 10", result <= 10f)
        }
    }

    @Test
    fun `currentAngle is within bounds`() {
        // Angle entre -90° et +90°
        val angle = DemoDetectionSimulator.currentAngle
        assertTrue("Angle should be >= -90", angle >= -90f)
        assertTrue("Angle should be <= 90", angle <= 90f)
    }

    @Test
    fun `objectHeight is within type range`() {
        val height = DemoDetectionSimulator.objectHeight
        val type = DemoDetectionSimulator.currentObjectType
        
        // La hauteur doit être dans la plage du type
        assertTrue("Height should be >= min", height >= type.heightRange.first)
        assertTrue("Height should be <= max", height <= type.heightRange.second)
    }

    // ── Tests Movement Direction Logic ────────────────────────────────────────

    @Test
    fun `getMovementDirection returns valid direction`() {
        val direction = DemoDetectionSimulator.getMovementDirection()
        val validDirections = DemoDetectionSimulator.MovementDirection.values()
        assertTrue(validDirections.contains(direction))
    }
}
