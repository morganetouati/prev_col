package com.example.prevcol

import android.app.Activity
import android.os.Bundle
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
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
     * En debug, les IDs de test sont acceptés normalement.
     * En release, si les IDs de test sont encore présents → bannière désactivée.
     */
    fun initialize(activity: Activity, onReady: (Boolean) -> Unit) {
        // En release uniquement : bloquer si IDs test encore présents
        if (!BuildConfig.DEBUG && BuildConfig.ADMOB_IS_TEST_IDS) {
            Log.e(TAG, "IDs AdMob de test détectés en release: AdMob désactivé")
            onReady(false)
            return
        }

        // En debug avec IDs test : skip UMP consent, initialiser directement
        if (BuildConfig.DEBUG && BuildConfig.ADMOB_IS_TEST_IDS) {
            Log.d(TAG, "Mode debug avec IDs test: skip consentement UMP")
            canRequestAds = true
            initializeMobileAds(activity.applicationContext) {
                onReady(true)
            }
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
     * Charge une bannière publicitaire adaptative (pleine largeur)
     */
    fun loadBanner(adView: AdView) {
        if (!isInitialized || !canRequestAds) {
            Log.d(TAG, "Consentement non accordé ou AdMob non initialisé: bannière masquée")
            adView.visibility = View.GONE
            return
        }

        adView.visibility = View.VISIBLE

        // Taille adaptative basée sur la largeur d'écran
        val activity = adView.context as? Activity
        if (activity != null) {
            val adSize = getAdaptiveBannerSize(activity)
            adView.setAdSize(adSize)
        }

        val adRequestBuilder = AdRequest.Builder()

        // Respecter le choix de consentement UMP pour les ads personnalisées
        if (BuildConfig.ADMOB_IS_TEST_IDS) {
            // En mode test, pas besoin de NPA
            Log.d(TAG, "Mode test: chargement bannière avec IDs de test")
        }

        val adRequest = adRequestBuilder.build()
        
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d(TAG, "Bannière chargée avec succès")
                adView.visibility = View.VISIBLE
            }
            
            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.e(TAG, "Erreur chargement bannière: code=${error.code} msg=${error.message}")
                // Garder l'espace visible mais vide plutôt que GONE
                // pour que le layout reste stable
                adView.visibility = View.GONE
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

    /**
     * Calcule la taille de bannière adaptative basée sur la largeur d'écran
     */
    private fun getAdaptiveBannerSize(activity: Activity): AdSize {
        val display = activity.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        val widthPixels = outMetrics.widthPixels.toFloat()
        val density = outMetrics.density
        val adWidth = (widthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }

    /**
     * Crée et charge une bannière adaptative dans le container fourni.
     * La bannière s'adapte automatiquement à la largeur de l'écran
     * (téléphone, tablette 7", tablette 10"+).
     */
    fun loadAdaptiveBanner(activity: Activity, adContainer: FrameLayout) {
        if (!isInitialized || !canRequestAds) {
            Log.d(TAG, "Consentement non accordé ou AdMob non initialisé: bannière masquée")
            adContainer.visibility = View.GONE
            return
        }

        // Nettoyer l'ancienne bannière si présente
        adContainer.removeAllViews()

        val adView = AdView(activity)
        adView.adUnitId = BuildConfig.ADMOB_BANNER_ID
        adView.setAdSize(getAdaptiveBannerSize(activity))
        
        adContainer.addView(adView)
        adContainer.visibility = View.VISIBLE

        val adRequest = AdRequest.Builder().build()
        
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d(TAG, "Bannière adaptative chargée avec succès")
                adContainer.visibility = View.VISIBLE
            }
            
            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.e(TAG, "Erreur chargement bannière adaptative: code=${error.code} msg=${error.message}")
                if (!BuildConfig.DEBUG) {
                    adContainer.visibility = View.GONE
                }
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
