package com.example.prevcol

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var adContainer: FrameLayout
    private lateinit var eyeToggleButton: ImageButton
    private lateinit var statusText: TextView
    private var isDetectionActive = false
    private var isToggleInProgress = false
    
    // Apply saved language before onCreate
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageHelper.applyLanguage(newBase))
    }
    
    // Receiver pour les changements d'état du service de détection
    private val detectionStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            isToggleInProgress = false
            if (::eyeToggleButton.isInitialized) {
                eyeToggleButton.isEnabled = true
            }
            updateEyeState()
        }
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
    
    // Demande permission notifications (API 33+)
    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }
    
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
        
        // Restaurer le mode jour/nuit depuis les préférences
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val nightMode = prefs.getBoolean("night_mode", false)
        AppCompatDelegate.setDefaultNightMode(
            if (nightMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        
        setContentView(R.layout.activity_main)

        // Si l'utilisateur n'a pas encore accepté la politique de confidentialité → redirect
        if (!prefs.getBoolean("privacy_accepted", false)) {
            startActivity(Intent(this, PrivacyActivity::class.java))
            finish()
            return
        }
        
        // Si premier lancement → montrer l'onboarding
        if (!prefs.getBoolean("onboarding_seen", false)) {
            startActivity(Intent(this, OnboardingActivity::class.java))
        }
        
        // Initialise AdMob et charge la bannière adaptative
        adContainer = findViewById(R.id.adContainer)
        AdManager.initialize(this) { canRequestAds ->
            if (canRequestAds) {
                AdManager.loadAdaptiveBanner(this@MainActivity, adContainer)
            } else {
                adContainer.visibility = android.view.View.GONE
            }
        }

        // Bouton œil toggle
        eyeToggleButton = findViewById(R.id.eyeToggleButton)
        statusText = findViewById(R.id.statusText)
        updateEyeState()
        
        eyeToggleButton.setOnClickListener {
            toggleDetection()
        }
        
        // Bouton réglages
        findViewById<ImageButton>(R.id.settingsButton).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        
        // Setup language button
        setupLanguageButton()
        
        // Demande permissions au premier lancement
        requestPermissionsIfNeeded()

        val privacyOptionsBtn = findViewById<Button>(R.id.privacyOptionsButton)
        // Afficher le bouton uniquement si le formulaire de consentement est disponible
        val consentInfo = com.google.android.ump.UserMessagingPlatform.getConsentInformation(this)
        if (consentInfo.privacyOptionsRequirementStatus ==
            com.google.android.ump.ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED) {
            privacyOptionsBtn.visibility = android.view.View.VISIBLE
        } else {
            privacyOptionsBtn.visibility = android.view.View.GONE
        }
        privacyOptionsBtn.setOnClickListener {
            AdManager.showPrivacyOptions(this) { updated ->
                if (updated) {
                    AdManager.loadAdaptiveBanner(this@MainActivity, adContainer)
                    Toast.makeText(this, getString(R.string.privacy_options_updated), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, getString(R.string.privacy_options_unavailable), Toast.LENGTH_SHORT).show()
                }
            }
        }

        findViewById<Button>(R.id.privacyPolicyButton).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW,
                Uri.parse("https://morganetouati.github.io/prev_col/privacy/"))
            startActivity(intent)
        }

        findViewById<Button>(R.id.appDescriptionButton).setOnClickListener {
            // Ouvrir l'onboarding pour revoir le mode d'emploi
            startActivity(Intent(this, OnboardingActivity::class.java))
        }
        
        // Enregistrer le receiver pour les changements d'état
        val filter = IntentFilter("com.example.prevcol.DETECTION_STATE_CHANGED")
        ContextCompat.registerReceiver(this, detectionStateReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }
    
    private fun toggleDetection() {
        if (isToggleInProgress) return

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        isDetectionActive = prefs.getBoolean("detection_active", false)

        isToggleInProgress = true
        eyeToggleButton.isEnabled = false

        try {
            if (isDetectionActive) {
                val stopIntent = Intent(this, DetectionService::class.java).apply {
                    action = DetectionService.ACTION_STOP
                }
                startService(stopIntent)
            } else {
                val startIntent = Intent(this, DetectionService::class.java).apply {
                    action = DetectionService.ACTION_START
                }
                ContextCompat.startForegroundService(this, startIntent)
            }
        } catch (_: Exception) {
            isToggleInProgress = false
            eyeToggleButton.isEnabled = true
            updateEyeState()
            Toast.makeText(this, getString(R.string.surveillance_toggle_error), Toast.LENGTH_LONG).show()
            return
        }

        eyeToggleButton.postDelayed({
            updateEyeState()
            isToggleInProgress = false
            eyeToggleButton.isEnabled = true
        }, 900)
    }
    
    private fun updateEyeState() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        isDetectionActive = prefs.getBoolean("detection_active", false)
        
        if (isDetectionActive) {
            eyeToggleButton.setImageResource(R.drawable.ic_eye_active)
            statusText.text = getString(R.string.surveillance_active)
            statusText.setTextColor(ContextCompat.getColor(this, R.color.safe_green))
        } else {
            eyeToggleButton.setImageResource(R.drawable.ic_eye_inactive)
            statusText.text = getString(R.string.surveillance_inactive)
            statusText.setTextColor(ContextCompat.getColor(this, R.color.text_hint))
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
        updateEyeState()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(detectionStateReceiver) } catch (_: Exception) {}
    }

    private fun requestPermissionsIfNeeded() {
        // Permission caméra d'abord (plus importante)
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission.launch(android.Manifest.permission.CAMERA)
        } else if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            // Notifications (API 33+) — demandé après la caméra pour éviter 2 popups
            requestNotificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        
        // Permission overlay (optionnelle - pour le radar HUD)
        // On demande seulement si pas déjà refusé récemment
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val overlayAsked = prefs.getBoolean("overlay_asked", false)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this) && !overlayAsked) {
            val overlayDialog = AlertDialog.Builder(this)
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
                .create()

            overlayDialog.setOnShowListener {
                // Forcer un gris foncé lisible pour le texte du message de permission
                val darkGray = android.graphics.Color.parseColor("#333333")
                overlayDialog.findViewById<TextView>(android.R.id.message)?.setTextColor(darkGray)
                // Titre aussi en gris foncé
                val titleId = resources.getIdentifier("alertTitle", "id", "android")
                if (titleId != 0) {
                    overlayDialog.findViewById<TextView>(titleId)?.setTextColor(darkGray)
                }
                // Boutons en gris foncé
                overlayDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(darkGray)
                overlayDialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(darkGray)
            }
            overlayDialog.show()
        }
    }
}