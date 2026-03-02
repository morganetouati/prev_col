package com.example.prevcol

import android.app.Activity
import android.os.Bundle
import android.content.Context
import android.util.Log
import android.view.View
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform

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
    
    private var canRequestAds = false

    /**
     * Initialise consentement + SDK AdMob.
     * `onReady(true)` signifie qu'une annonce peut être demandée.
     */
    fun initialize(activity: Activity, onReady: (Boolean) -> Unit) {
        if (!BuildConfig.DEBUG && BuildConfig.ADMOB_IS_TEST_IDS) {
            Log.e(TAG, "IDs AdMob de test détectés en release: AdMob désactivé")
            onReady(false)
            return
        }

        val consentInfo = UserMessagingPlatform.getConsentInformation(activity)
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInfo.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError: FormError? ->
                    if (formError != null) {
                        Log.w(TAG, "Erreur formulaire consentement: ${formError.message}")
                    }
                    canRequestAds = consentInfo.canRequestAds()
                    initializeMobileAds(activity.applicationContext) {
                        onReady(canRequestAds)
                    }
                }
            },
            { requestError ->
                Log.w(TAG, "Erreur update consentement: ${requestError.message}")
                canRequestAds = consentInfo.canRequestAds()
                initializeMobileAds(activity.applicationContext) {
                    onReady(canRequestAds)
                }
            }
        )
    }

    private fun initializeMobileAds(context: Context, onComplete: () -> Unit) {
        if (isInitialized) {
            onComplete()
            return
        }

        MobileAds.initialize(context) { initializationStatus ->
            Log.d(TAG, "AdMob initialisé: $initializationStatus")
            isInitialized = true
            onComplete()
        }
    }
    
    /**
     * Charge une bannière publicitaire
     */
    fun loadBanner(adView: AdView) {
        if (!isInitialized || !canRequestAds) {
            Log.d(TAG, "Consentement non accordé ou AdMob non initialisé: bannière masquée")
            adView.visibility = View.GONE
            return
        }

        adView.visibility = View.VISIBLE

        val npaBundle = Bundle().apply {
            putString("npa", "1")
        }

        val adRequest = AdRequest.Builder()
            .addNetworkExtrasBundle(AdMobAdapter::class.java, npaBundle)
            .build()
        
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

    fun showPrivacyOptions(activity: Activity, onComplete: (Boolean) -> Unit) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError: FormError? ->
            if (formError != null) {
                Log.w(TAG, "Impossible d'ouvrir les options de confidentialité: ${formError.message}")
                onComplete(false)
                return@showPrivacyOptionsForm
            }

            val consentInfo = UserMessagingPlatform.getConsentInformation(activity)
            canRequestAds = consentInfo.canRequestAds()
            onComplete(true)
        }
    }
}
