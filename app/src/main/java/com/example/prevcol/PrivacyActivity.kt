package com.example.prevcol

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

/**
 * Écran affiché au 1er lancement : politique de confidentialité + permissions
 */
class PrivacyActivity : AppCompatActivity() {
    
    // Apply saved language before onCreate
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageHelper.applyLanguage(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Restaurer le mode jour/nuit depuis les préférences
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val nightMode = prefs.getBoolean("night_mode", false)
        AppCompatDelegate.setDefaultNightMode(
            if (nightMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        
        // Si déjà accepté, aller directement à MainActivity
        if (prefs.getBoolean("privacy_accepted", false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        
        setContentView(R.layout.activity_privacy)
        
        // Setup language button
        setupLanguageButton()

        findViewById<Button>(R.id.acceptPrivacyButton).setOnClickListener {
            // Marque le consentement
            getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit().putBoolean("privacy_accepted", true).apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Lien vers politique de confidentialité
        findViewById<TextView>(R.id.policyLink).setOnClickListener {
            val intent = Intent(android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse("https://morganetouati.github.io/prev_col/privacy/"))
            startActivity(intent)
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
}
