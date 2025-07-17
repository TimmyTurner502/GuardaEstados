package com.sjocol.guardaestados.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.sjocol.guardaestados.R
import com.sjocol.guardaestados.AppState
import android.widget.Toast
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.zIndex

/**
 * Galería en pantalla completa con navegación y acciones
 * @param items Lista de archivos a mostrar
 * @param initialIndex Índice inicial del archivo a mostrar
 * @param onClose Llamado al cerrar la galería
 * @param appState Estado de la aplicación para funcionalidades
 */
@Composable
fun MediaPreviewer(
    items: List<GalleryItem>,
    initialIndex: Int = 0,
    onClose: () -> Unit,
    appState: AppState? = null
) {
    if (items.isEmpty()) return
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = initialIndex) { items.size }
    var showControls by remember { mutableStateOf(true) }
    var showRewardDialog by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<String?>(null) }
    var showRewardedAd by remember { mutableStateOf(false) }
    var showWhatsAppDialog by remember { mutableStateOf(false) }
    var whatsAppPackages by remember { mutableStateOf(listOf<Pair<String, String>>()) }

    // Ocultar controles automáticamente
    LaunchedEffect(showControls) {
        if (showControls) {
            kotlinx.coroutines.delay(3000)
            showControls = false
        }
    }
    // Manejar back
    BackHandler {
        onClose()
    }
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(Modifier.fillMaxSize()) {
            // Barra superior SIEMPRE al frente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
                    .zIndex(2f)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "${pagerState.currentPage + 1}/${items.size}",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                    )
                    Spacer(Modifier.weight(1f))
                }
            }

            // Pager principal
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize().zIndex(1f)
            ) { page ->
                val item = items[page]
                when (item) {
                    is GalleryItem.Image -> {
                        AsyncImage(
                            model = item.id,
                            contentDescription = "Imagen ${page + 1}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                    is GalleryItem.Video -> {
                        val exoPlayer = remember {
                            ExoPlayer.Builder(context).build().apply {
                                setMediaItem(MediaItem.fromUri(android.net.Uri.parse(item.id)))
                                prepare()
                                playWhenReady = false
                            }
                        }
                        DisposableEffect(Unit) {
                            onDispose { exoPlayer.release() }
                        }
                        AndroidView(
                            factory = {
                                PlayerView(it).apply {
                                    player = exoPlayer
                                    useController = true
                                    setShutterBackgroundColor(android.graphics.Color.BLACK)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Barra de botones de acción SIEMPRE al frente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .zIndex(2f)
            ) {
                Row(
                    Modifier
                        .align(Alignment.Center)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 32.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    IconButton(
                        onClick = {
                            pendingAction = "share"
                            showRewardDialog = true
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_share),
                            contentDescription = "Compartir",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            pendingAction = "download"
                            showRewardDialog = true
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_download),
                            contentDescription = "Descargar",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            pendingAction = "repost"
                            showRewardDialog = true
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_repost_circle),
                            contentDescription = "Repostear",
                            tint = Color.White,
                            modifier = Modifier
                                .size(28.dp)
                                .graphicsLayer(rotationZ = 90f)
                        )
                    }
                }
            }

            // Dots indicadores SIEMPRE al frente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .zIndex(2f)
            ) {
                Row(
                    Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(items.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(
                                    if (index == pagerState.currentPage) Color.White else Color.White.copy(alpha = 0.4f),
                                    RoundedCornerShape(5.dp)
                                )
                        )
                    }
                }
            }
        }
    }
    // Diálogo de confirmación para publicidad por recompensa
    if (showRewardDialog && pendingAction != null) {
        AlertDialog(
            onDismissRequest = { showRewardDialog = false; pendingAction = null },
            title = { Text(stringResource(R.string.rewarded_ad_batch_title)) },
            text = { Text(stringResource(R.string.rewarded_ad_batch_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showRewardDialog = false
                    showRewardedAd = true
                }) {
                    Text(stringResource(R.string.accept))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRewardDialog = false
                    pendingAction = null
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Diálogo para elegir WhatsApp después de la publicidad
    if (showWhatsAppDialog) {
        val currentItem = items.getOrNull(pagerState.currentPage)
        if (currentItem != null) {
            val whatsAppPackages = FileActions.getAvailableWhatsAppPackages(context)
            AlertDialog(
                onDismissRequest = { showWhatsAppDialog = false },
                title = { Text("Selecciona WhatsApp para compartir en Estados") },
                text = {
                    if (whatsAppPackages.isEmpty()) {
                        Text("No se encontró ninguna versión de WhatsApp instalada.")
                    } else {
                        Column {
                            whatsAppPackages.forEach { (pkg, label) ->
                                Button(
                                    onClick = {
                                        FileActions.repostFile(context, currentItem.id, pkg)
                                        showWhatsAppDialog = false
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(label)
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showWhatsAppDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }

    // Mostrar anuncio por recompensa
    if (showRewardedAd) {
        ShowRewardedAd(
            context = context,
            onRewardEarned = {
                val currentItem = items.getOrNull(pagerState.currentPage)
                when (pendingAction) {
                    // Para compartir, solo marcar como pendiente, no lanzar el intent aún
                    "share" -> { /* No hacer nada aquí, se maneja en onAdClosed */ }
                    "download" -> if (currentItem != null) FileActions.downloadFiles(context, listOf(currentItem.id), {
                        Toast.makeText(context, "Archivo descargado", Toast.LENGTH_SHORT).show()
                    }, appState?.downloadFolder ?: "")
                    "repost" -> {
                        showWhatsAppDialog = true
                    }
                }
                // No limpiar pendingAction aquí, se limpia en onAdClosed
                showRewardedAd = false
            },
            onAdClosed = {
                // Solo aquí lanzar el intent de compartir si pendingAction es share
                val currentItem = items.getOrNull(pagerState.currentPage)
                if (pendingAction == "share" && currentItem != null) {
                    FileActions.shareFiles(context, listOf(currentItem.id))
                }
                showRewardedAd = false
                pendingAction = null
            }
        )
    }
} 