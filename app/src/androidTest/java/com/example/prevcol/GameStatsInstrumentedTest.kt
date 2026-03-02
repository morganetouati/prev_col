package com.example.prevcol

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests instrumentés (sur device/émulateur)
 * Vérifie l'intégration avec le contexte Android réel
 */
@RunWith(AndroidJUnit4::class)
class GameStatsInstrumentedTest {

    private lateinit var gameStats: GameStats

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        gameStats = GameStats(context)
        gameStats.reset() // Clean state pour chaque test
    }

    @Test
    fun initialPointsAreZero() {
        assertEquals(0, gameStats.getTotalPoints())
    }

    @Test
    fun addPointsIncreasesTotal() {
        gameStats.addPoints(10)
        assertEquals(10, gameStats.getTotalPoints())
        
        gameStats.addPoints(15)
        assertEquals(25, gameStats.getTotalPoints())
    }

    @Test
    fun levelProgressionWorks() {
        assertEquals(1, gameStats.getLevel())
        
        gameStats.addPoints(50)
        assertEquals(2, gameStats.getLevel())
        
        gameStats.addPoints(150)
        assertEquals(3, gameStats.getLevel())
    }

    @Test
    fun badgeUnlockingWorks() {
        assertFalse(gameStats.hasBadge("first_alert"))
        
        gameStats.unlockBadge("first_alert")
        assertTrue(gameStats.hasBadge("first_alert"))
    }

    @Test
    fun recordAlertUnlocksFirstAlertBadge() {
        assertFalse(gameStats.hasBadge("first_alert"))
        
        gameStats.recordAlert("danger")
        
        assertTrue(gameStats.hasBadge("first_alert"))
        assertEquals(1, gameStats.getAlertCount("danger"))
    }

    @Test
    fun recordAlertAddsPoints() {
        val initialPoints = gameStats.getTotalPoints()
        
        gameStats.recordAlert("danger")  // +5 points
        assertEquals(initialPoints + 5, gameStats.getTotalPoints())
        
        gameStats.recordAlert("rapid")  // +10 points
        assertEquals(initialPoints + 15, gameStats.getTotalPoints())
    }

    @Test
    fun getAllBadgesReturnsUnlockedOnly() {
        assertTrue(gameStats.getAllBadges().isEmpty())
        
        gameStats.unlockBadge("first_alert")
        gameStats.unlockBadge("speedster")
        
        val badges = gameStats.getAllBadges()
        assertEquals(2, badges.size)
        assertTrue(badges.contains("first_alert"))
        assertTrue(badges.contains("speedster"))
    }

    @Test
    fun resetClearsAllData() {
        gameStats.addPoints(100)
        gameStats.unlockBadge("first_alert")
        
        gameStats.reset()
        
        assertEquals(0, gameStats.getTotalPoints())
        assertFalse(gameStats.hasBadge("first_alert"))
        assertEquals(0, gameStats.getStreak())
    }

    @Test
    fun typeCounterIncrements() {
        assertEquals(0, gameStats.getDetectionTypeCount(ObjectType.ADULTE))
        
        gameStats.recordAlert("danger", ObjectType.ADULTE)
        assertEquals(1, gameStats.getDetectionTypeCount(ObjectType.ADULTE))
        
        gameStats.recordAlert("danger", ObjectType.ADULTE)
        assertEquals(2, gameStats.getDetectionTypeCount(ObjectType.ADULTE))
    }

    @Test
    fun protecteurBadgeUnlocksAt10ChildDetections() {
        repeat(9) {
            gameStats.recordAlert("danger", ObjectType.ENFANT)
        }
        assertFalse(gameStats.hasBadge("protecteur"))
        
        gameStats.recordAlert("danger", ObjectType.ENFANT)
        assertTrue(gameStats.hasBadge("protecteur"))
    }

    @Test
    fun packageNameIsCorrect() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.regardsaumonde.app", context.packageName)
    }
}
