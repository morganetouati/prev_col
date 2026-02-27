package com.example.prevcol

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StatsActivity : AppCompatActivity() {
    private lateinit var gameStats: GameStats

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)
        gameStats = GameStats(this)
        
        val pointsText = findViewById<TextView>(R.id.pointsText)
        val badgesText = findViewById<TextView>(R.id.badgesText)
        
        pointsText.text = "Points: ${gameStats.getTotalPoints()}"
        
        val badges = listOf(
            "first_alert" to "🏅 Première alerte détectée",
            "danger_zone" to "⚠️ 20 alertes rouges",
            "guardian" to "🛡️ 100 points gagnés",
            "speedster" to "⚡ Approche rapide détectée",
            "always_aware" to "👁️ 1000 points gagnés"
        )
        
        val unlockedBadges = badges.filter { gameStats.hasBadge(it.first) }.joinToString("\n") { it.second }
        badgesText.text = if (unlockedBadges.isEmpty()) "Aucun badge pour le moment." else unlockedBadges
    }
}
