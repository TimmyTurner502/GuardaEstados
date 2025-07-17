package com.sjocol.guardaestados.ui.components

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import androidx.compose.ui.Modifier

@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val adView = AdView(context)
            adView.adUnitId = "ca-app-pub-3940256099942544/6300978111" // Banner de prueba oficial
            adView.setAdSize(AdSize.BANNER)
            adView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            adView.loadAd(AdRequest.Builder().build())
            adView
        }
    )
} 