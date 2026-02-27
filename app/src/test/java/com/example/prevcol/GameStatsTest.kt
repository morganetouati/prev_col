package com.example.prevcol

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import android.content.Context
import android.content.SharedPreferences

/**
 * Tests unitaires pour GameStats
 * Vérifie le système de points, niveaux, badges et streak
 */
class GameStatsTest {

    private lateinit var mockContext: Context
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor

    @Before
    fun setup() {
        mockContext = mock(Context::class.java)
        mockPrefs = mock(SharedPreferences::class.java)
        mockEditor = mock(SharedPreferences.Editor::class.java)

        `when`(mockContext.getSharedPreferences("game_stats", Context.MODE_PRIVATE))
            .thenReturn(mockPrefs)
        `when`(mockPrefs.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putInt(anyString(), anyInt())).thenReturn(mockEditor)
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        `when`(mockEditor.putBoolean(anyString(), anyBoolean())).thenReturn(mockEditor)
        `when`(mockEditor.putLong(anyString(), anyLong())).thenReturn(mockEditor)
    }

    // ── Tests Niveaux ─────────────────────────────────────────────────────────

    @Test
    fun `getLevel returns 1 for 0 points`() {
        `when`(mockPrefs.getInt("total_points", 0)).thenReturn(0)
        val stats = GameStats(mockContext)
        assertEquals(1, stats.getLevel())
    }

    @Test
    fun `getLevel returns 2 for 50 points`() {
        `when`(mockPrefs.getInt("total_points", 0)).thenReturn(50)
        val stats = GameStats(mockContext)
        assertEquals(2, stats.getLevel())
    }

    @Test
    fun `getLevel returns 3 for 200 points`() {
        `when`(mockPrefs.getInt("total_points", 0)).thenReturn(200)
        val stats = GameStats(mockContext)
        assertEquals(3, stats.getLevel())
    }

    @Test
    fun `getLevel returns 4 for 500 points`() {
        `when`(mockPrefs.getInt("total_points", 0)).thenReturn(500)
        val stats = GameStats(mockContext)
        assertEquals(4, stats.getLevel())
    }

    @Test
    fun `getLevel returns 5 for 1000 points`() {
        `when`(mockPrefs.getInt("total_points", 0)).thenReturn(1000)
        val stats = GameStats(mockContext)
        assertEquals(5, stats.getLevel())
    }

    // ── Tests Labels Niveaux ──────────────────────────────────────────────────

    @Test
    fun `getLevelLabel returns correct labels`() {
        val expectedLabels = mapOf(
            0 to "🌱 Débutant",
            50 to "🚶 Intermédiaire",
            200 to "👁️ Confirmé",
            500 to "⚡ Expert",
            1000 to "🏆 Maître"
        )

        expectedLabels.forEach { (points, expectedLabel) ->
            `when`(mockPrefs.getInt("total_points", 0)).thenReturn(points)
            val stats = GameStats(mockContext)
            assertEquals(expectedLabel, stats.getLevelLabel())
        }
    }

    // ── Tests Points vers prochain niveau ─────────────────────────────────────

    @Test
    fun `getPointsToNextLevel calculates correctly`() {
        `when`(mockPrefs.getInt("total_points", 0)).thenReturn(30)
        val stats = GameStats(mockContext)
        assertEquals(20, stats.getPointsToNextLevel()) // 50 - 30
    }

    @Test
    fun `getPointsToNextLevel returns 0 at max level`() {
        `when`(mockPrefs.getInt("total_points", 0)).thenReturn(1500)
        val stats = GameStats(mockContext)
        assertEquals(0, stats.getPointsToNextLevel())
    }

    // ── Tests Badges ──────────────────────────────────────────────────────────

    @Test
    fun `hasBadge returns false when badge not unlocked`() {
        `when`(mockPrefs.getBoolean("badge_first_alert", false)).thenReturn(false)
        val stats = GameStats(mockContext)
        assertFalse(stats.hasBadge("first_alert"))
    }

    @Test
    fun `hasBadge returns true when badge unlocked`() {
        `when`(mockPrefs.getBoolean("badge_first_alert", false)).thenReturn(true)
        val stats = GameStats(mockContext)
        assertTrue(stats.hasBadge("first_alert"))
    }

    @Test
    fun `ALL_BADGE_IDS contains 12 badges`() {
        assertEquals(12, GameStats.ALL_BADGE_IDS.size)
    }

    @Test
    fun `BADGE_DESCRIPTIONS maps all badges`() {
        assertEquals(GameStats.ALL_BADGE_IDS.size, GameStats.BADGE_DESCRIPTIONS.size)
        GameStats.ALL_BADGE_IDS.forEach { badgeId ->
            assertTrue("Missing description for $badgeId", 
                GameStats.BADGE_DESCRIPTIONS.containsKey(badgeId))
        }
    }

    // ── Tests Streak ──────────────────────────────────────────────────────────

    @Test
    fun `getStreak returns 0 initial`() {
        `when`(mockPrefs.getInt("streak", 0)).thenReturn(0)
        val stats = GameStats(mockContext)
        assertEquals(0, stats.getStreak())
    }

    // ── Tests Alert Count ─────────────────────────────────────────────────────

    @Test
    fun `getAlertCount returns 0 for new type`() {
        `when`(mockPrefs.getInt("alert_count_danger", 0)).thenReturn(0)
        val stats = GameStats(mockContext)
        assertEquals(0, stats.getAlertCount("danger"))
    }

    @Test
    fun `getAlertCount returns stored value`() {
        `when`(mockPrefs.getInt("alert_count_danger", 0)).thenReturn(42)
        val stats = GameStats(mockContext)
        assertEquals(42, stats.getAlertCount("danger"))
    }
}
