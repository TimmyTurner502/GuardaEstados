package com.sjocol.guardaestados.ui.components

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError

@Composable
fun ShowRewardedAd(
    context: Context,
    onRewardEarned: () -> Unit,
    onAdClosed: () -> Unit
) {
    var rewardedAd by remember { mutableStateOf<RewardedAd?>(null) }
    var adLoaded by remember { mutableStateOf(false) }
    var adShown by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            "ca-app-pub-3940256099942544/5224354917", // ID de prueba oficial
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
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
        if (adLoaded && rewardedAd != null && !adShown) {
            rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    onAdClosed()
                }
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    onAdClosed()
                }
            }
            rewardedAd?.show(context as Activity) { rewardItem: RewardItem ->
                onRewardEarned()
            }
            adShown = true
        }
    }
} 