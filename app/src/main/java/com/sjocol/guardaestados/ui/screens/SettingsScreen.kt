package com.sjocol.guardaestados.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import com.sjocol.guardaestados.ui.components.FileActions
import androidx.compose.ui.platform.LocalContext
import com.sjocol.guardaestados.ui.components.WhatsAppInstance
import java.util.Locale
import androidx.compose.ui.res.stringResource
import com.sjocol.guardaestados.R
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import android.content.Intent
import com.sjocol.guardaestados.ui.components.FileUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import android.net.Uri
import android.app.Activity
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import com.sjocol.guardaestados.navigation.Screen
import com.sjocol.guardaestados.ui.components.PermissionUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.sjocol.guardaestados.AppState
import com.sjocol.guardaestados.AppPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, appState: AppState) {
    val context = LocalContext.current
    val activity = context as? Activity
    val hasPermission = remember { mutableStateOf(activity?.let { PermissionUtils.hasStoragePermission(it) } ?: false) }

    // Verificar permisos cada vez que se entra
    LaunchedEffect(Unit) {
        hasPermission.value = activity?.let { PermissionUtils.hasStoragePermission(it) } ?: false
    }

    val instances = com.sjocol.guardaestados.ui.components.FileUtils.getAvailableInstances(context)
    var selectedInstance by remember { mutableStateOf(appState.selectedInstance) }
    var expandedInstance by remember { mutableStateOf(false) }
    val idiomas = listOf(
        stringResource(R.string.lang_auto) to null,
        stringResource(R.string.lang_es) to Locale("es"),
        stringResource(R.string.lang_en) to Locale("en")
    )
    var expandedIdioma by remember { mutableStateOf(false) }
    val idiomaActual = idiomas.find { it.second?.language == appState.locale.language && it.second?.country == appState.locale.country }?.first
        ?: if (appState.locale == Locale.getDefault()) stringResource(R.string.lang_auto) else idiomas[1].first
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(it, takeFlags)
            appState.updateDownloadFolder(FileUtils.getFullPathFromTreeUri(context, it) ?: appState.downloadFolder)
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Selector de instancia solo entre WhatsApp y WhatsApp Business
            item {
                Text(stringResource(R.string.select_whatsapp_account), style = MaterialTheme.typography.titleMedium)
                Box {
                    Button(
                        onClick = { expandedInstance = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(selectedInstance?.name ?: stringResource(R.string.select_account), color = MaterialTheme.colorScheme.onPrimary)
                    }
                    DropdownMenu(
                        expanded = expandedInstance, 
                        onDismissRequest = { expandedInstance = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (instances.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.no_whatsapp_instances_found)) },
                                onClick = { expandedInstance = false }
                            )
                        } else {
                            instances.forEach { instance ->
                                DropdownMenuItem(
                                    text = { Text(instance.name) },
                                    onClick = {
                                        selectedInstance = instance
                                        appState.updateSelectedInstance(instance)
                                        expandedInstance = false
                                    }
                                )
                            }
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            }
            // Selector de idioma
            item {
                Text(stringResource(R.string.language), style = MaterialTheme.typography.titleMedium)
                Box {
                    Button(
                        onClick = { expandedIdioma = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(idiomaActual, color = MaterialTheme.colorScheme.onPrimary)
                    }
                    DropdownMenu(
                        expanded = expandedIdioma, 
                        onDismissRequest = { expandedIdioma = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        idiomas.forEach { (nombre, locale) ->
                            DropdownMenuItem(
                                text = { Text(nombre) },
                                onClick = {
                                    if (locale != null) {
                                        appState.locale = locale
                                    } else {
                                        appState.resetLocaleToSystem()
                                    }
                                    expandedIdioma = false
                                }
                            )
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            }
            // Carpeta de descarga
            item {
                Text(stringResource(R.string.download_folder), style = MaterialTheme.typography.titleMedium)
                Button(
                    onClick = { launcher.launch(null) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(appState.downloadFolder, color = MaterialTheme.colorScheme.onPrimary)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            }
            // Selector de paleta de colores
            item {
                Text(stringResource(R.string.color_palette), style = MaterialTheme.typography.titleMedium)
                var expandedPaleta by remember { mutableStateOf(false) }
                // Declarar la lista de paletas explícitamente:
                val paletas: List<Pair<String, AppPalette>> = listOf(
                    stringResource(R.string.palette_default) to AppPalette.DEFAULT,
                    stringResource(R.string.palette_light) to AppPalette.LIGHT,
                    stringResource(R.string.palette_dark) to AppPalette.DARK
                )
                val paletaActual = paletas.find { it.second == appState.palette }?.first ?: paletas[0].first
                Box {
                    Button(
                        onClick = { expandedPaleta = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(paletaActual, color = MaterialTheme.colorScheme.onPrimary)
                    }
                    DropdownMenu(
                        expanded = expandedPaleta,
                        onDismissRequest = { expandedPaleta = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        paletas.forEach { (nombre, tipo) ->
                            DropdownMenuItem(
                                text = { Text(nombre) },
                                onClick = {
                                    appState.palette = tipo
                                    expandedPaleta = false
                                }
                            )
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            }
            // Enlaces a políticas
            item {
                Text(stringResource(R.string.legal_info), style = MaterialTheme.typography.titleMedium)
                Button(
                    onClick = { navController.navigate(Screen.PrivacyPolicy.route) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(stringResource(R.string.privacy_policy), color = MaterialTheme.colorScheme.onSecondary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { navController.navigate(Screen.TermsOfService.route) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(stringResource(R.string.terms_of_service), color = MaterialTheme.colorScheme.onSecondary)
                }
            }
        }
    }
} 