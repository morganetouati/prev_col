package com.example.prevcol

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar

class GameStats(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("game_stats", Context.MODE_PRIVATE)

    // ── Points ────────────────────────────────────────────────────────────────
    fun addPoints(points: Int) {
        prefs.edit().putInt("total_points", getTotalPoints() + points).apply()
    }
    fun getTotalPoints(): Int = prefs.getInt("total_points", 0)

    // ── Niveau ────────────────────────────────────────────────────────────────
    fun getLevel(): Int = when (getTotalPoints()) {
        in 0..49       -> 1   // Débutant
        in 50..199     -> 2   // Intermédiaire
        in 200..499    -> 3   // Confirmé
        in 500..999    -> 4   // Expert
        else           -> 5   // Maître
    }
    fun getLevelLabel(): String = when (getLevel()) {
        1 -> "🌱 Débutant"
        2 -> "🚶 Intermédiaire"
        3 -> "👁️ Confirmé"
        4 -> "⚡ Expert"
        else -> "🏆 Maître"
    }
    fun getPointsToNextLevel(): Int {
        val thresholds = listOf(50, 200, 500, 1000, Int.MAX_VALUE)
        val next = thresholds[getLevel() - 1]
        return if (next == Int.MAX_VALUE) 0 else next - getTotalPoints()
    }

    // ── Streak journalier ────────────────────────────────────────────────────
    private fun todayKey(): String {
        val c = Calendar.getInstance()
        return "${c.get(Calendar.YEAR)}-${c.get(Calendar.DAY_OF_YEAR)}"
    }
    fun recordDailyUse() {
        val today = todayKey()
        if (prefs.getString("last_use_day", "") == today) return
        val yesterday = run {
            val c = Calendar.getInstance(); c.add(Calendar.DAY_OF_YEAR, -1)
            "${c.get(Calendar.YEAR)}-${c.get(Calendar.DAY_OF_YEAR)}"
        }
        val streak = if (prefs.getString("last_use_day", "") == yesterday)
            prefs.getInt("streak", 0) + 1 else 1
        prefs.edit()
            .putString("last_use_day", today)
            .putInt("streak", streak)
            .apply()
        if (streak >= 5) unlockBadge("streak_5")
        if (streak >= 30) unlockBadge("streak_30")
    }
    fun getStreak(): Int = prefs.getInt("streak", 0)

    // ── Type alert counters ───────────────────────────────────────────────────
    fun getDetectionTypeCount(type: ObjectType): Int =
        prefs.getInt("type_count_${type.name}", 0)

    private fun incrementTypeCount(type: ObjectType) {
        val k = "type_count_${type.name}"
        prefs.edit().putInt(k, prefs.getInt(k, 0) + 1).apply()
    }

    // ── Badges ────────────────────────────────────────────────────────────────
    fun unlockBadge(badgeId: String) {
        val key = "badge_$badgeId"
        if (!prefs.contains(key)) {
            prefs.edit()
                .putBoolean(key, true)
                .putLong("badge_time_$badgeId", System.currentTimeMillis())
                .apply()
        }
    }
    fun hasBadge(id: String): Boolean = prefs.getBoolean("badge_$id", false)

    fun getAllBadges(): List<String> = ALL_BADGE_IDS.filter { hasBadge(it) }

    // ── Record alert ──────────────────────────────────────────────────────────
    fun recordAlert(type: String, objectType: ObjectType? = null) {
        val count = getAlertCount(type)
        prefs.edit().putInt("alert_count_$type", count + 1).apply()
        recordDailyUse()

        objectType?.let { incrementTypeCount(it) }

        when (type) {
            "danger" -> {
                if (count + 1 == 1) unlockBadge("first_alert")
                if (count + 1 >= 20) unlockBadge("danger_zone")
                if (count + 1 >= 100) unlockBadge("centurion")
            }
            "rapid" -> {
                unlockBadge("speedster")
                if (getAlertCount("rapid") + 1 >= 10) unlockBadge("speedster_10")
            }
        }

        // Badges type-spécifiques
        objectType?.let {
            val typeCount = getDetectionTypeCount(it) // déjà incrémenté
            when (it) {
                ObjectType.ENFANT, ObjectType.BEBE ->
                    if (typeCount >= 10) unlockBadge("protecteur")
                ObjectType.PETIT_CHIEN, ObjectType.MOYEN_CHIEN, ObjectType.GRAND_CHIEN ->
                    if (typeCount >= 10) unlockBadge("ami_betes")
                else -> {}
            }
        }

        val pts = when (type) { "danger" -> 5; "rapid" -> 10; else -> 0 }
        addPoints(pts)

        val total = getTotalPoints()
        if (total >= 100) unlockBadge("guardian")
        if (total >= 1000) unlockBadge("always_aware")
        if (total >= 500) unlockBadge("expert_500")
    }

    fun getAlertCount(type: String): Int = prefs.getInt("alert_count_$type", 0)

    fun reset() { prefs.edit().clear().apply() }

    companion object {
        val ALL_BADGE_IDS = listOf(
            "first_alert", "danger_zone", "centurion",
            "guardian", "expert_500", "always_aware",
            "speedster", "speedster_10",
            "streak_5", "streak_30",
            "protecteur", "ami_betes"
        )

        val BADGE_DESCRIPTIONS = mapOf(
            "first_alert"   to "🥉 Premier Regard — 1ère alerte évitée",
            "danger_zone"   to "🥈 Zone Dangereuse — 20 alertes danger",
            "centurion"     to "🏅 Centurion — 100 alertes danger",
            "guardian"      to "🥇 Gardien de la Rue — 100 points",
            "expert_500"    to "💎 Expert — 500 points",
            "always_aware"  to "👁️ Maître Vigilant — 1000 points",
            "speedster"     to "⚡ Speedster — 1ère approche rapide",
            "speedster_10"  to "🌩️ Éclair — 10 approches rapides",
            "streak_5"      to "🔥 En feu — 5 jours consécutifs",
            "streak_30"     to "💪 Iron Will — 30 jours de suite",
            "protecteur"    to "👶 Protecteur — 10 enfants/bébés détectés",
            "ami_betes"     to "🐾 Ami des bêtes — 10 animaux détectés"
        )
    }
}

