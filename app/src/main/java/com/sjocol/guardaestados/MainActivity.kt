package com.sjocol.guardaestados

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.sjocol.guardaestados.navigation.AppNavGraph
import com.sjocol.guardaestados.ui.theme.GuardaEstadosTheme
import com.sjocol.guardaestados.AppState
import androidx.compose.runtime.remember
import com.sjocol.guardaestados.ui.components.AdConsentDialog
import com.sjocol.guardaestados.ui.components.AdConsentManager
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.content.res.Configuration
import android.content.Context

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Aplicar el locale guardado antes de Compose
        val prefs = getSharedPreferences("guardaestados_prefs", MODE_PRIVATE)
        val lang = prefs.getString("locale_lang", null)
        val country = prefs.getString("locale_country", null)
        if (lang != null) {
            val locale = if (country.isNullOrEmpty()) java.util.Locale(lang) else java.util.Locale(lang, country)
            val config = Configuration(resources.configuration)
            config.setLocale(locale)
            @Suppress("DEPRECATION")
            resources.updateConfiguration(config, resources.displayMetrics)
        }
        
        // Inicializar AdMob con consentimiento
        val hasConsent = AdConsentManager.hasUserConsent(this)
        AdConsentManager.initializeAds(this, hasConsent)
        
        setContent {
            val appState = remember { AppState(this) }
            var showConsentDialog by remember { mutableStateOf(!hasConsent) }
            
            // Mostrar diálogo de consentimiento si es necesario
            if (showConsentDialog) {
                AdConsentDialog(
                    onConsentGiven = {
                        AdConsentManager.setUserConsent(this, true)
                        AdConsentManager.initializeAds(this, true)
                        showConsentDialog = false
                    },
                    onConsentDenied = {
                        AdConsentManager.setUserConsent(this, false)
                        showConsentDialog = false
                    }
                )
            }

            // Usar idiomaKey como key para forzar reinicialización global
            androidx.compose.runtime.key(appState.idiomaKey) {
                GuardaEstadosTheme(themeType = appState.themeType, themeMode = appState.themeMode) {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        val navController = rememberNavController()
                        AppNavGraph(
                            navController = navController,
                            appState = appState
                        )
                    }
                }
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("guardaestados_prefs", Context.MODE_PRIVATE)
        val lang = prefs.getString("locale_lang", null)
        val country = prefs.getString("locale_country", null)
        val locale = if (lang != null) {
            if (country.isNullOrEmpty()) java.util.Locale(lang) else java.util.Locale(lang, country)
        } else {
            java.util.Locale.getDefault()
        }
        val config = android.content.res.Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GuardaEstadosTheme {
        Greeting("Android")
    }
}