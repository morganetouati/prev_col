package com.example.prevcol

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class StatsActivity : AppCompatActivity() {
    private lateinit var gameStats: GameStats

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageHelper.applyLanguage(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)
        gameStats = GameStats(this)

        val pointsText = findViewById<TextView>(R.id.pointsText)
        val badgesText = findViewById<TextView>(R.id.badgesText)

        pointsText.text = getString(
            R.string.points_label,
            gameStats.getTotalPoints(),
            gameStats.getLevelLabel()
        )

        // Afficher tous les 12 badges (debloqués + verrouillés)
        val unlockedBadges = gameStats.getAllBadges().toSet()
        val ssb = SpannableStringBuilder()

        val countText = "${unlockedBadges.size}/${GameStats.ALL_BADGE_IDS.size} " +
            "${getString(R.string.badges_unlocked)}\n\n"
        ssb.append(countText)
        ssb.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.accent)),
            0, ssb.length, 0
        )

        for ((index, badgeId) in GameStats.ALL_BADGE_IDS.withIndex()) {
            val isUnlocked = badgeId in unlockedBadges
            val description = GameStats.BADGE_DESCRIPTIONS[badgeId] ?: badgeId
            val displayText = if (isUnlocked) {
                "\u2705 $description"
            } else {
                "\uD83D\uDD12 ${description.replace(Regex("^[^\\s]+"), "\u2753")}"
            }

            val start = ssb.length
            ssb.append(displayText)
            val end = ssb.length

            if (!isUnlocked) {
                ssb.setSpan(
                    ForegroundColorSpan(Color.parseColor("#666666")),
                    start, end, 0
                )
            }

            if (index < GameStats.ALL_BADGE_IDS.size - 1) {
                ssb.append("\n\n")
            }
        }

        badgesText.text = ssb
    }
}
