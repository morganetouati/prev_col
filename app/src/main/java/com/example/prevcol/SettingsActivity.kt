package com.example.prevcol

import android.content.Context
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageHelper.applyLanguage(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)

        // Bouton retour
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        // Switch vibrations
        val vibrationSwitch = findViewById<SwitchMaterial>(R.id.vibrationSwitch)
        vibrationSwitch.isChecked = prefs.getBoolean("vibration_enabled", true)
        vibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("vibration_enabled", isChecked).apply()
        }

        // Switch alertes sonores
        val soundSwitch = findViewById<SwitchMaterial>(R.id.soundSwitch)
        soundSwitch.isChecked = prefs.getBoolean("sound_enabled", true)
        soundSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("sound_enabled", isChecked).apply()
        }

        // Switch mode nuit
        val nightModeSwitch = findViewById<SwitchMaterial>(R.id.nightModeSwitch)
        nightModeSwitch.isChecked = prefs.getBoolean("night_mode", false)
        nightModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("night_mode", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }
}
