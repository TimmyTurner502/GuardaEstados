package com.sjocol.guardaestados.ui.components

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.sjocol.guardaestados.R

@Composable
fun AdConsentDialog(
    onConsentGiven: () -> Unit,
    onConsentDenied: () -> Unit
) {
    var showDialog by remember { mutableStateOf(true) }
    
    if (showDialog) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Consentimiento de Anuncios",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Esta aplicación utiliza anuncios para mantener el servicio gratuito. " +
                               "Los anuncios son proporcionados por Google AdMob y pueden recopilar datos " +
                               "para personalizar la experiencia publicitaria.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                showDialog = false
                                onConsentDenied()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Rechazar")
                        }
                        
                        Button(
                            onClick = {
                                showDialog = false
                                onConsentGiven()
                            }
                        ) {
                            Text("Aceptar")
                        }
                    }
                }
            }
        }
    }
}

object AdConsentManager {
    private const val CONSENT_PREF = "ad_consent"
    private const val CONSENT_GIVEN = "consent_given"
    
    fun hasUserConsent(context: Context): Boolean {
        val prefs = context.getSharedPreferences(CONSENT_PREF, Context.MODE_PRIVATE)
        return prefs.getBoolean(CONSENT_GIVEN, false)
    }
    
    fun setUserConsent(context: Context, consent: Boolean) {
        val prefs = context.getSharedPreferences(CONSENT_PREF, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(CONSENT_GIVEN, consent).apply()
    }
    
    fun initializeAds(context: Context, consent: Boolean) {
        if (consent) {
            // Inicializar AdMob con consentimiento
            MobileAds.initialize(context) { }
        } else {
            // Inicializar AdMob sin consentimiento (anuncios limitados)
            val configuration = RequestConfiguration.Builder()
                .setTestDeviceIds(listOf("TEST_DEVICE_ID"))
                .build()
            MobileAds.setRequestConfiguration(configuration)
            MobileAds.initialize(context) { }
        }
    }
} 