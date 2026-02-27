package com.example.prevcol

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError

/**
 * Gestionnaire de publicités AdMob
 * 
 * Pour passer en production :
 * 1. Créer un compte AdMob : https://admob.google.com
 * 2. Créer une application dans AdMob
 * 3. Créer un bloc d'annonces "Bannière"
 * 4. Remplacer les IDs dans res/values/strings.xml :
 *    - admob_app_id : votre ID application
 *    - admob_banner_id : votre ID bloc bannière
 */
object AdManager {
    
    private const val TAG = "AdManager"
    private var isInitialized = false
    
    /**
     * Initialise le SDK AdMob (à appeler une seule fois au démarrage)
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        
        MobileAds.initialize(context) { initializationStatus ->
            Log.d(TAG, "AdMob initialisé: $initializationStatus")
            isInitialized = true
        }
    }
    
    /**
     * Charge une bannière publicitaire
     */
    fun loadBanner(adView: AdView) {
        val adRequest = AdRequest.Builder().build()
        
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d(TAG, "Bannière chargée avec succès")
            }
            
            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.e(TAG, "Erreur chargement bannière: ${error.message}")
            }
            
            override fun onAdOpened() {
                Log.d(TAG, "Bannière ouverte (clic)")
            }
            
            override fun onAdClicked() {
                Log.d(TAG, "Clic sur bannière")
            }
            
            override fun onAdClosed() {
                Log.d(TAG, "Bannière fermée")
            }
        }
        
        adView.loadAd(adRequest)
    }
}
