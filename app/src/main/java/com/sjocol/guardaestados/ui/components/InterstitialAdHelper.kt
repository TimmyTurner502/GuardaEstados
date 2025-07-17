package com.sjocol.guardaestados.ui.components

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError

@Composable
fun ShowInterstitialAd(
    context: Context,
    onAdClosed: () -> Unit
) {
    var interstitialAd by remember { mutableStateOf<InterstitialAd?>(null) }
    var adLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            "ca-app-pub-3940256099942544/1033173712", // ID de prueba oficial
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    adLoaded = true
                }
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adLoaded = false
                    onAdClosed() // Si falla, permite la acción igual
                }
            }
        )
    }

    LaunchedEffect(adLoaded) {
        if (adLoaded && interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    onAdClosed()
                }
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    onAdClosed()
                }
            }
            interstitialAd?.show(context as Activity)
        }
    }
} 