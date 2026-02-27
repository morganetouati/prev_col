package com.example.prevcol

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

/**
 * Helper class for managing app language changes.
 * Supports: French, English, Spanish, German, Italian, Hebrew, Arabic
 */
object LanguageHelper {
    
    private const val PREFS_NAME = "language_prefs"
    private const val KEY_LANGUAGE = "selected_language"
    
    // Supported languages with their codes
    val LANGUAGES = listOf(
        "fr" to "🇫🇷 Français",
        "en" to "🇬🇧 English",
        "es" to "🇪🇸 Español",
        "de" to "🇩🇪 Deutsch",
        "it" to "🇮🇹 Italiano",
        "iw" to "🇮🇱 עברית",  // Hebrew uses 'iw' on Android
        "ar" to "🇸🇦 العربية"
    )
    
    /**
     * Get current saved language code or default to French
     */
    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, "fr") ?: "fr"
    }
    
    /**
     * Save the selected language
     */
    fun saveLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
    }
    
    /**
     * Apply the saved language to the context.
     * Call this in attachBaseContext of activities.
     */
    fun applyLanguage(context: Context): Context {
        val languageCode = getSavedLanguage(context)
        return updateResources(context, languageCode)
    }
    
    /**
     * Change language and restart the activity to apply changes.
     */
    @Suppress("DEPRECATION")
    fun changeLanguage(activity: Activity, languageCode: String) {
        saveLanguage(activity, languageCode)
        
        // Restart the activity to apply new language
        val intent = activity.intent
        activity.finish()
        activity.startActivity(intent)
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
    
    /**
     * Update the context resources with the specified locale.
     */
    @Suppress("DEPRECATION")
    private fun updateResources(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            config.setLayoutDirection(locale)
            context.createConfigurationContext(config)
        } else {
            config.locale = locale
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }
    
    /**
     * Get the display name for a language code.
     */
    fun getLanguageName(languageCode: String): String {
        return LANGUAGES.find { it.first == languageCode }?.second ?: "🇫🇷 Français"
    }
    
    /**
     * Get flag emoji for current language (for button display).
     */
    fun getCurrentFlag(context: Context): String {
        return when (getSavedLanguage(context)) {
            "fr" -> "🇫🇷"
            "en" -> "🇬🇧"
            "es" -> "🇪🇸"
            "de" -> "🇩🇪"
            "it" -> "🇮🇹"
            "iw" -> "🇮🇱"
            "ar" -> "🇸🇦"
            else -> "🇫🇷"
        }
    }
    
    /**
     * Check if the current language is RTL (Right-to-Left).
     */
    fun isRtl(context: Context): Boolean {
        val lang = getSavedLanguage(context)
        return lang == "iw" || lang == "ar"
    }
}
