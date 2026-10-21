package com.example.sanjutools.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdConfig {
    // User's Production AdMob IDs
    const val PROD_APP_ID = "ca-app-pub-9852743065586031~4261686272"
    const val PROD_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-9852743065586031/8958470028"

    // Official Google Sample / Test Ad Unit IDs (guaranteed safe during development)
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    // Use test ads during debug/testing to avoid invalid traffic policy violations.
    // Set to false when publishing a signed release to Google Play.
    var useTestAds: Boolean = BuildConfig.DEBUG

    val interstitialAdUnitId: String
        get() = if (useTestAds) TEST_INTERSTITIAL_AD_UNIT_ID else PROD_INTERSTITIAL_AD_UNIT_ID
}

class AdManager private constructor() {

    private var interstitialAd: InterstitialAd? = null
    private var isLoadingAd: Boolean = false
    private var lastAdShowTimeMs: Long = 0L
    private var toolTransitionCount: Int = 0

    // Frequency capping: show at most once every 45 seconds and only after at least 3 tool switches
    private val minIntervalBetweenAdsMs: Long = 45_000L
    private val minTransitionsBeforeAd: Int = 3

    fun initialize(context: Context) {
        try {
            MobileAds.initialize(context) { status ->
                Log.d(TAG, "Google Mobile Ads SDK initialized: $status")
                loadInterstitial(context.applicationContext)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing MobileAds: ${e.message}")
        }
    }

    fun loadInterstitial(context: Context) {
        if (interstitialAd != null || isLoadingAd) {
            return
        }

        isLoadingAd = true
        val adRequest = AdRequest.Builder().build()
        val adUnitId = AdConfig.interstitialAdUnitId
        Log.d(TAG, "Loading interstitial ad with ID: $adUnitId (testMode: ${AdConfig.useTestAds})")

        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoadingAd = false
                    Log.d(TAG, "Interstitial ad successfully loaded and cached.")

                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Interstitial ad was dismissed.")
                            interstitialAd = null
                            lastAdShowTimeMs = System.currentTimeMillis()
                            loadInterstitial(context)
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Log.e(TAG, "Failed to show interstitial ad: ${adError.message}")
                            interstitialAd = null
                            loadInterstitial(context)
                        }

                        override fun onAdShowedFullScreenContent() {
                            Log.d(TAG, "Interstitial ad displayed on screen.")
                            interstitialAd = null
                        }
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "Failed to load interstitial ad: ${loadAdError.message}")
                    interstitialAd = null
                    isLoadingAd = false
                }
            }
        )
    }

    /**
     * Checks frequency capping and presents the interstitial ad if ready at a natural transition.
     * Always invokes [onProceed] either after the ad is dismissed or immediately if no ad is shown.
     */
    fun showInterstitialIfAllowed(
        activity: Activity,
        onProceed: () -> Unit
    ) {
        toolTransitionCount++
        val now = System.currentTimeMillis()
        val timeSinceLastAd = now - lastAdShowTimeMs

        val ad = interstitialAd
        val isEligible = toolTransitionCount >= minTransitionsBeforeAd && timeSinceLastAd >= minIntervalBetweenAdsMs

        if (ad != null && isEligible) {
            Log.d(TAG, "Showing interstitial ad at natural transition (transition #$toolTransitionCount)")
            toolTransitionCount = 0
            val previousCallback = ad.fullScreenContentCallback

            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    previousCallback?.onAdDismissedFullScreenContent()
                    onProceed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    previousCallback?.onAdFailedToShowFullScreenContent(adError)
                    onProceed()
                }

                override fun onAdShowedFullScreenContent() {
                    previousCallback?.onAdShowedFullScreenContent()
                }
            }

            ad.show(activity)
        } else {
            // No ad ready or frequency cap not reached; proceed smoothly without interruption
            onProceed()
            // Make sure next ad is loading in background if not already cached
            if (interstitialAd == null && !isLoadingAd) {
                loadInterstitial(activity.applicationContext)
            }
        }
    }

    companion object {
        private const val TAG = "SanjuAdManager"

        @Volatile
        private var instance: AdManager? = null

        fun getInstance(): AdManager {
            return instance ?: synchronized(this) {
                instance ?: AdManager().also { instance = it }
            }
        }
    }
}
