package com.sjocol.guardaestados.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sjocol.guardaestados.ui.screens.HomeScreen
import com.sjocol.guardaestados.ui.screens.SettingsScreen
import com.sjocol.guardaestados.AppState
import com.sjocol.guardaestados.ui.screens.MensajeScreen
import com.sjocol.guardaestados.ui.screens.PrivacyPolicyScreen
import com.sjocol.guardaestados.ui.screens.TermsOfServiceScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
    object Mensaje : Screen("mensaje")
    object PrivacyPolicy : Screen("privacy_policy")
    object TermsOfService : Screen("terms_of_service")
}

@Composable
fun AppNavGraph(navController: NavHostController, appState: AppState) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) { HomeScreen(navController, appState) }
        composable(Screen.Settings.route) { SettingsScreen(navController, appState) }
        composable(Screen.Mensaje.route) { MensajeScreen() }
        composable(Screen.PrivacyPolicy.route) { PrivacyPolicyScreen(navController) }
        composable(Screen.TermsOfService.route) { TermsOfServiceScreen(navController) }
    }
} 