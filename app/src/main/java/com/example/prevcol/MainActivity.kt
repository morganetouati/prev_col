package com.example.prevcol

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdView

class MainActivity : AppCompatActivity() {

    private lateinit var gameStats: GameStats
    private lateinit var adView: AdView
    
    // Apply saved language before onCreate
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageHelper.applyLanguage(newBase))
    }
    
    // Demande permission caméra
    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Toast.makeText(this, getString(R.string.camera_granted), Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, getString(R.string.camera_denied), Toast.LENGTH_LONG).show()
        }
    }
    
    // Demande permission overlay
    private val requestOverlayLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
            Toast.makeText(this, getString(R.string.overlay_granted), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gameStats = GameStats(this)

        // Si l'utilisateur n'a pas encore accepté la politique de confidentialité → redirect
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        if (!prefs.getBoolean("privacy_accepted", false)) {
            startActivity(Intent(this, PrivacyActivity::class.java))
            finish()
            return
        }
        
        // Initialise AdMob et charge la bannière
        AdManager.initialize(this)
        adView = findViewById(R.id.adView)
        AdManager.loadBanner(adView)

        // Actualise l'affichage au démarrage
        updateStatsDisplay()
        
        // Setup language button
        setupLanguageButton()
        
        // Demande permissions au premier lancement
        requestPermissionsIfNeeded()

        // Bouton actualiser
        findViewById<Button>(R.id.refreshButton).setOnClickListener {
            updateStatsDisplay()
            Toast.makeText(this, getString(R.string.stats_refreshed), Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupLanguageButton() {
        val languageButton = findViewById<Button>(R.id.languageButton)
        
        // Update button text with current flag
        languageButton.text = LanguageHelper.getCurrentFlag(this)
        
        languageButton.setOnClickListener {
            showLanguageDialog()
        }
    }
    
    private fun showLanguageDialog() {
        val languages = LanguageHelper.LANGUAGES
        val languageNames = languages.map { it.second }.toTypedArray()
        val currentLang = LanguageHelper.getSavedLanguage(this)
        val currentIndex = languages.indexOfFirst { it.first == currentLang }.coerceAtLeast(0)
        
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.language_dialog_title))
            .setSingleChoiceItems(languageNames, currentIndex) { dialog, which ->
                val selectedLang = languages[which].first
                if (selectedLang != currentLang) {
                    LanguageHelper.changeLanguage(this, selectedLang)
                }
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        updateStatsDisplay()
    }

    private fun requestPermissionsIfNeeded() {
        // Permission caméra d'abord (plus importante)
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission.launch(android.Manifest.permission.CAMERA)
        }
        
        // Permission overlay (optionnelle - pour le radar HUD)
        // On demande seulement si pas déjà refusé récemment
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val overlayAsked = prefs.getBoolean("overlay_asked", false)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this) && !overlayAsked) {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.permission_dialog_title))
                .setMessage(getString(R.string.permission_dialog_message))
                .setPositiveButton(getString(R.string.permission_overlay_button)) { _, _ ->
                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                        requestOverlayLauncher.launch(intent)
                    } catch (e: Exception) {
                        Toast.makeText(this, "Paramètres > Applications > Regards au monde > Autoriser la superposition", Toast.LENGTH_LONG).show()
                    }
                }
                .setNegativeButton(getString(R.string.permission_later)) { _, _ ->
                    // Marque comme demandé pour ne pas redemander
                    prefs.edit().putBoolean("overlay_asked", true).apply()
                    Toast.makeText(this, getString(R.string.overlay_optional_message), Toast.LENGTH_LONG).show()
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun updateStatsDisplay() {
        val totalPoints = gameStats.getTotalPoints()
        val badges = gameStats.getAllBadges()
        val dangerAlerts = gameStats.getAlertCount("danger")
        val rapidAlerts = gameStats.getAlertCount("rapid")

        findViewById<TextView>(R.id.totalPointsText).text =
            getString(R.string.points_label, totalPoints, gameStats.getLevelLabel())

        val ptsNext = gameStats.getPointsToNextLevel()
        findViewById<TextView>(R.id.totalAlertsText).text =
            if (ptsNext > 0) getString(R.string.alerts_label, dangerAlerts) + "  (" + getString(R.string.next_level_label, ptsNext) + ")"
            else getString(R.string.alerts_label, dangerAlerts) + "  🏆"

        val streak = gameStats.getStreak()
        val streakStr = if (streak > 0) "  " + getString(R.string.streak_label, streak) else ""
        findViewById<TextView>(R.id.rapidApproachText).text =
            getString(R.string.rapid_label, rapidAlerts) + streakStr

        val badgesText = findViewById<TextView>(R.id.badgesText)
        if (badges.isEmpty()) {
            badgesText.text = getString(R.string.no_badges)
        } else {
            val list = badges.mapNotNull { GameStats.BADGE_DESCRIPTIONS[it] }
            badgesText.text = list.joinToString("\n\n")        }
    }

}