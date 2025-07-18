package com.sjocol.guardaestados.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sjocol.guardaestados.navigation.Screen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Tab
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.sjocol.guardaestados.ui.components.GalleryItem
import com.sjocol.guardaestados.ui.components.GalleryPreview
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.sjocol.guardaestados.AppState
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import com.sjocol.guardaestados.ui.components.PermissionUtils
import com.sjocol.guardaestados.ui.components.WhatsAppInstance
import com.sjocol.guardaestados.ui.components.FileUtils
import java.io.File
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.runtime.SideEffect
import com.sjocol.guardaestados.ui.components.FileActions
import android.widget.Toast
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.background
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.DisposableEffect
import com.sjocol.guardaestados.R
import androidx.compose.ui.zIndex
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.TopEnd
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.mutableStateListOf
import com.sjocol.guardaestados.ui.components.BannerAdView
import com.sjocol.guardaestados.ui.components.ShowRewardedAd
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarHost
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import android.util.Log
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, appState: AppState) {
    androidx.compose.runtime.key(appState.locale) {
        val context = LocalContext.current
        val tabTitles = listOf(
            stringResource(R.string.tab_estados),
            stringResource(R.string.tab_guardados),
            stringResource(R.string.tab_mensaje)
        )
        androidx.compose.runtime.key(appState.locale, tabTitles) {
            val pagerState = rememberPagerState(initialPage = 0) { tabTitles.size }
            val coroutineScope = rememberCoroutineScope()
            val tabColors = listOf(
                Color(0xFF4CAF50)
            )

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(R.string.app_name), color = MaterialTheme.colorScheme.onPrimary) },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        actions = {
                            IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                                Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings), tint = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(WindowInsets.systemBars.asPaddingValues())
                ) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.tabs_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.Center),
                            textAlign = TextAlign.Center
                        )
                    }
                    TabRow(
                        selectedTabIndex = pagerState.currentPage,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        divider = {}
                    ) {
                        tabTitles.forEachIndexed { index, title ->
                            val selected = pagerState.currentPage == index
                            Tab(
                                selected = selected,
                                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                                modifier = Modifier
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.background else Color.Transparent,
                                        shape = MaterialTheme.shapes.small
                                    )
                            ) {
                                Text(
                                    title,
                                    color = when {
                                        selected -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.weight(1f)
                    ) { page ->
                        when (page) {
                            0 -> EstadosTab(navController, appState)
                            1 -> GuardadosTab(appState, showDeleteAll = true)
                            2 -> MensajeScreen()
                            else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(stringResource(R.string.error_tab), color = Color.Red)
                            }
                        }
                    }
                    // Banner de prueba AdMob
                    BannerAdView(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadosTab(navController: NavController, appState: AppState) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var statusItems by remember { mutableStateOf<List<GalleryItem>>(emptyList()) }
    var isRefreshing by remember { mutableStateOf(false) }
    var showPermissionUI by remember { mutableStateOf(false) }
    val selectedInstance = appState.selectedInstance

    // Detectar si hay permisos
    fun hasAllFilesPermission(): Boolean = PermissionUtils.hasStoragePermission(activity!!)

    // NUEVA IMPLEMENTACIÓN ROBUSTA CON DIAGNÓSTICO COMPLETO
    suspend fun loadStatusFilesForSelected() {
        Log.d("EstadosTab", "=== DIAGNÓSTICO COMPLETO DE ESTADOS ===")
        Log.d("EstadosTab", "Instancia seleccionada: ${selectedInstance?.name}")
        Log.d("EstadosTab", "Ruta de instancia: ${selectedInstance?.path}")
        
        if (selectedInstance == null) {
            Log.e("EstadosTab", "❌ No hay instancia seleccionada")
            return
        }
        
        // PASO 1: Verificar información de la carpeta
        val folderInfo = FileUtils.getFolderInfo(selectedInstance.path)
        Log.d("EstadosTab", "📁 Información de carpeta: $folderInfo")
        
        // PASO 2: Verificar permisos
        val hasPermissions = FileUtils.checkStoragePermissions(context)
        Log.d("EstadosTab", "🔐 Permisos de almacenamiento: $hasPermissions")
        
        // PASO 3: Obtener archivos con diagnóstico detallado
        val files = FileUtils.getStatusFilesForInstanceDeep(selectedInstance.path)
        
        Log.d("EstadosTab", "📊 Resultado de búsqueda:")
        Log.d("EstadosTab", "   - Archivos encontrados: ${files.size}")
        
        if (files.isNotEmpty()) {
            Log.d("EstadosTab", "   - Archivos válidos:")
            files.forEach { file ->
                val isAccessible = FileUtils.isFileAccessible(file.absolutePath)
                Log.d("EstadosTab", "     ✅ ${file.name} (${file.length()} bytes, accesible: $isAccessible)")
            }
        } else {
            Log.w("EstadosTab", "   ⚠️ No se encontraron archivos válidos")
            
            // PASO 4: Búsqueda alternativa si no hay archivos
            Log.d("EstadosTab", "🔍 Intentando búsqueda alternativa...")
            val alternativeFiles = FileUtils.findFilesRecursively(selectedInstance.path, maxDepth = 2)
            Log.d("EstadosTab", "   - Archivos alternativos encontrados: ${alternativeFiles.size}")
            alternativeFiles.forEach { file ->
                Log.d("EstadosTab", "     🔍 ${file.absolutePath}")
            }
        }
        
        // PASO 5: Crear GalleryItems
        statusItems = files.map { file ->
            if (file.name.lowercase().endsWith(".mp4")) {
                Log.d("EstadosTab", "🎬 Creando GalleryItem.Video: ${file.absolutePath}")
                GalleryItem.Video(file.absolutePath)
            } else {
                Log.d("EstadosTab", "🖼️ Creando GalleryItem.Image: ${file.absolutePath}")
                GalleryItem.Image(file.absolutePath)
            }
        }
        
        Log.d("EstadosTab", "✅ GalleryItems creados: ${statusItems.size}")
        
        // PASO 6: Estadísticas finales
        if (files.isNotEmpty()) {
            val stats = FileUtils.getFileStats(files)
            Log.d("EstadosTab", "📈 Estadísticas: $stats")
        }
    }

    // Verificar permisos cuando cambie la instancia seleccionada
    LaunchedEffect(selectedInstance) {
        isLoading = true
        if (!hasAllFilesPermission()) {
            showPermissionUI = true
            isLoading = false
            return@LaunchedEffect
        }
        loadStatusFilesForSelected()
        isLoading = false
    }

    Box(Modifier.fillMaxSize()) {
        if (showPermissionUI) {
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lock),
                    contentDescription = "Permiso requerido",
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Para mostrar los estados de WhatsApp, otorga permiso de acceso a todos los archivos.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        PermissionUtils.requestStoragePermission(activity!!)
                    },
                    modifier = Modifier.padding(horizontal = 32.dp).fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lock_open),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Otorgar permiso de archivos")
                }
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (isLoading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (selectedInstance == null) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Selecciona una cuenta de WhatsApp en configuración",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else if (statusItems.isEmpty()) {
                        // Estado vacío con pull-to-refresh
                        SwipeRefresh(
                            state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
                            onRefresh = {
                                isRefreshing = true
                                scope.launch {
                                    loadStatusFilesForSelected()
                                    isRefreshing = false
                                }
                            }
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_search),
                                        contentDescription = "Sin estados",
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No se encontraron estados en la instancia seleccionada",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "💡 Desliza hacia abajo para actualizar",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        GalleryPreview(
                            items = statusItems,
                            onSelectionChange = {},
                            scrollToIndex = 0,
                            showSearchAndFilters = false,
                            snackbarHostState = null,
                            selectedItemsExternal = emptyList(),
                            onRefresh = {
                                isRefreshing = true
                                scope.launch {
                                    loadStatusFilesForSelected()
                                    isRefreshing = false
                                }
                            },
                            isRefreshing = isRefreshing
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GuardadosTab(appState: AppState, showDeleteAll: Boolean = false) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    var savedItems by remember { mutableStateOf<List<GalleryItem>>(emptyList()) }
    var selectedSaved = remember { mutableStateListOf<GalleryItem>() }
    var lastSavedIndex by remember { mutableStateOf(0) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showBatchActions by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var showPermissionUI by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var showRewardedAd by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<String?>(null) }
    var pendingRepostItemsSaved by remember { mutableStateOf<List<String>>(emptyList()) }
    var whatsAppPackagesSaved by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var showWhatsAppDialogSaved by remember { mutableStateOf(false) }

    // Función para cargar archivos guardados
    fun loadSavedFiles() {
        val files = FileUtils.getSavedFiles(appState.downloadFolder)
        savedItems = files.map {
            if (it.name.endsWith(".mp4")) GalleryItem.Video(it.absolutePath)
            else GalleryItem.Image(it.absolutePath)
        }
    }
    
    // Eliminar función asíncrona y su uso
    LaunchedEffect(Unit) {
        loadSavedFiles()
        while (true) {
            kotlinx.coroutines.delay(5000)
            loadSavedFiles()
        }
    }

    // Detectar si hay permisos
    fun hasAllFilesPermission(): Boolean = PermissionUtils.hasStoragePermission(activity!!)

    // Reintentar carga al volver de la pantalla de permisos
    LaunchedEffect(Unit) {
    if (!hasAllFilesPermission()) {
        showPermissionUI = true
        return@LaunchedEffect
    }
    loadSavedFiles()
    }

    Box(Modifier.fillMaxSize()) {
        if (showPermissionUI) {
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lock),
                    contentDescription = "Permiso requerido",
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Para mostrar los archivos guardados, otorga permiso de acceso a todos los archivos.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        PermissionUtils.requestStoragePermission(activity!!)
                    },
                    modifier = Modifier.padding(horizontal = 32.dp).fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lock_open),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Otorgar permiso de archivos")
                }
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                // Barra de herramientas
                Row(
                    Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showDeleteAll && savedItems.isNotEmpty()) {
                        Button(
                            onClick = { showConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.delete_all_saved))
                        }
                    }
                    // Eliminar todos los IconButton a la derecha
                }

                // Barra contextual compacta
                if (selectedSaved.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Seleccionados: ${selectedSaved.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            IconButton(onClick = {
                                pendingAction = "share"
                                if (!appState.rewardedAdWatchedThisSession) {
                                    showRewardedAd = true
                                    pendingAction = "share"
                                } else {
                                    FileActions.shareFiles(context, selectedSaved.map { it.id })
                                    showBatchActions = false
                                }
                            }) {
                                Icon(painter = painterResource(id = R.drawable.ic_share), contentDescription = "Compartir")
                            }
                            IconButton(onClick = {
                                pendingAction = "download"
                                if (!appState.rewardedAdWatchedThisSession) {
                                    showRewardedAd = true
                                    pendingAction = "download"
                                } else {
                                    FileActions.downloadFiles(context, selectedSaved.map { it.id }, {
                                        Toast.makeText(context, "Archivos descargados", Toast.LENGTH_SHORT).show()
                                    }, appState.downloadFolder)
                                    showBatchActions = false
                                }
                            }) {
                                Icon(painter = painterResource(id = R.drawable.ic_download), contentDescription = "Descargar")
                            }
                            IconButton(onClick = {
                                pendingAction = "repost"
                                if (!appState.rewardedAdWatchedThisSession) {
                                    showRewardedAd = true
                                    pendingAction = "repost"
                                } else {
                                    showWhatsAppDialogSaved = true
                                }
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_repost_circle),
                                    contentDescription = "Repostear",
                                    modifier = Modifier.graphicsLayer(rotationZ = 90f).size(24.dp)
                                )
                            }
                        }
                    }
                }

                if (showConfirmDialog) {
                    AlertDialog(
                        onDismissRequest = { showConfirmDialog = false },
                        title = { Text(stringResource(R.string.delete_all_confirm_title)) },
                        text = { Text(stringResource(R.string.delete_all_confirm_text)) },
                        confirmButton = {
                            TextButton(onClick = {
                                // Filtrar archivos bloqueados antes de eliminar todos
                                val filesToDelete = savedItems.filter { !appState.isFileLocked(it.id) }
                                if (filesToDelete.isNotEmpty()) {
                                    FileActions.deleteFiles(context, filesToDelete.map { it.id }) {
                                        savedItems = savedItems.filter { it !in filesToDelete }
                                        showConfirmDialog = false
                                        Toast.makeText(context, "Archivos eliminados", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "No hay archivos para eliminar", Toast.LENGTH_SHORT).show()
                                    showConfirmDialog = false
                                }
                            }) {
                                Text(stringResource(R.string.delete), color = Color.Red)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showConfirmDialog = false }) {
                                Text(stringResource(R.string.cancel))
                            }
                        }
                    )
                }
                
                // Diálogo de confirmación para publicidad por recompensa
                if (showRewardedAd && !appState.rewardedAdWatchedThisSession) {
                    AlertDialog(
                        onDismissRequest = { showRewardedAd = false; pendingAction = null },
                        title = { Text(stringResource(R.string.rewarded_ad_batch_title)) },
                        text = { Text(stringResource(R.string.rewarded_ad_batch_message)) },
                        confirmButton = {
                            TextButton(onClick = {
                                appState.rewardedAdWatchedThisSession = true
                                when (pendingAction) {
                                    "share" -> {
                                        FileActions.shareFiles(context, selectedSaved.map { it.id })
                                        showBatchActions = false
                                    }
                                    "download" -> {
                                        FileActions.downloadFiles(context, selectedSaved.map { it.id }, {
                                            Toast.makeText(context, "Archivos descargados", Toast.LENGTH_SHORT).show()
                                        }, appState.downloadFolder)
                                        showBatchActions = false
                                    }
                                    "repost" -> {
                                        showWhatsAppDialogSaved = true
                                    }
                                }
                                pendingAction = null
                                showRewardedAd = false
                            }) {
                                Text(stringResource(R.string.accept))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showRewardedAd = false
                                pendingAction = null
                            }) {
                                Text(stringResource(R.string.cancel))
                            }
                        }
                    )
                }
                
                // Diálogo de selección de WhatsApp para repostear en guardados
                if (showWhatsAppDialogSaved && pendingRepostItemsSaved.isNotEmpty()) {
                    AlertDialog(
                        onDismissRequest = { showWhatsAppDialogSaved = false; pendingRepostItemsSaved = emptyList() },
                        title = {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("Selecciona WhatsApp para repostear", style = MaterialTheme.typography.titleLarge)
                            }
                        },
                        text = {
                            Column(Modifier.fillMaxWidth()) {
                                whatsAppPackagesSaved.forEach { (pkg, label) ->
                                    val iconRes = when {
                                        pkg.contains("w4b") -> R.drawable.ic_whatsapp_business
                                        pkg.contains("business") -> R.drawable.ic_whatsapp_business
                                        pkg.contains("clone") -> R.drawable.ic_whatsapp_dual
                                        else -> R.drawable.ic_whatsapp
                                    }
                                    Button(
                                        onClick = {
                                            pendingRepostItemsSaved.forEach { fileId ->
                                                FileActions.repostFile(context, fileId, pkg)
                                            }
                                            showWhatsAppDialogSaved = false
                                            pendingRepostItemsSaved = emptyList()
                                            showBatchActions = false
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        contentPadding = PaddingValues(12.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = iconRes),
                                            contentDescription = label,
                                            modifier = Modifier.size(28.dp).padding(end = 8.dp)
                                        )
                                        Text(label, fontSize = 18.sp)
                                    }
                                }
                            }
                        },
                        confirmButton = {},
                        dismissButton = {
                            TextButton(onClick = { showWhatsAppDialogSaved = false; pendingRepostItemsSaved = emptyList() }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }
                
                if (savedItems.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_search),
                                contentDescription = "Sin archivos guardados",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.no_saved_files),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // Usar el nuevo flujo de vista previa integrado en GalleryPreview
                    GalleryPreview(
                        items = savedItems,
                        onSelectionChange = { selectedSaved.clear(); selectedSaved.addAll(it) },
                        scrollToIndex = lastSavedIndex,
                        showSearchAndFilters = false,
                        showLockButton = true,
                        appState = appState,
                        snackbarHostState = snackbarHostState,
                        onRefresh = {
                            isRefreshing = true
                            scope.launch {
                                loadSavedFiles()
                                isRefreshing = false
                            }
                        },
                        isRefreshing = isRefreshing
                    )
                }
                // Mostrar el snackbar con efecto secundario
                if (snackbarMessage != null) {
                    LaunchedEffect(snackbarMessage) {
                        snackbarHostState.showSnackbar(snackbarMessage!!)
                        snackbarMessage = null
                    }
                }
            }
        }
    }
} 