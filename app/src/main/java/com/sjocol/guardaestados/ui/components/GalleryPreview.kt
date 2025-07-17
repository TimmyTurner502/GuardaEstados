package com.sjocol.guardaestados.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sjocol.guardaestados.R
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.provider.MediaStore
import java.io.File
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateListOf
import android.os.Build
import android.util.Size
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.res.stringResource
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import com.sjocol.guardaestados.AppState
import androidx.compose.ui.zIndex
import androidx.compose.animation.animateColorAsState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarHost
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import android.media.MediaMetadataRetriever

sealed class GalleryItem(val id: String, open var isLocked: Boolean = false) {
    data class Image(val url: String,
                     override var isLocked: Boolean = false) : GalleryItem(url, isLocked)
    data class Video(val url: String, override var isLocked: Boolean = false) : GalleryItem(url, isLocked)
}

enum class FilterType {
    ALL, IMAGES, VIDEOS
}

// NUEVO: Cache de thumbnails para evitar recálculos
private val thumbnailCache = mutableMapOf<String, Bitmap?>()

// NUEVO: Función optimizada para thumbnails que soporta content://
suspend fun getVideoThumbnailAsync(context: android.content.Context, path: String): Bitmap? = withContext(Dispatchers.IO) {
    // Verificar cache primero
    thumbnailCache[path]?.let { return@withContext it }
    val thumbnail = try {
        if (path.startsWith("content://")) {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, android.net.Uri.parse(path))
            val bmp = retriever.getFrameAtTime(0)
            retriever.release()
            bmp
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ThumbnailUtils.createVideoThumbnail(File(path), Size(320, 320), null)
        } else {
            @Suppress("DEPRECATION")
            ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND)
        }
    } catch (e: Exception) {
        null
    }
    thumbnailCache[path] = thumbnail
    thumbnail
}

fun getVideoThumbnail(path: String): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Usar Size(320, 320) como ejemplo, puedes ajustar el tamaño
            ThumbnailUtils.createVideoThumbnail(File(path), Size(320, 320), null)
        } else {
            @Suppress("DEPRECATION")
            ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND)
        }
    } catch (e: Exception) {
        null
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun GalleryPreview(
    items: List<GalleryItem>,
    onItemClick: (GalleryItem) -> Unit = {},
    onSelectionChange: (List<GalleryItem>) -> Unit = {},
    showSelectionCount: Boolean = true,
    scrollToIndex: Int? = null,
    showSearchAndFilters: Boolean = true,
    showLockButton: Boolean = false,
    appState: AppState? = null,
    snackbarHostState: SnackbarHostState? = null,
    selectedItemsExternal: List<GalleryItem>? = null,
    onRefresh: (() -> Unit)? = null,
    isRefreshing: Boolean = false
) {
    val selectedItems = remember { mutableStateListOf<GalleryItem>() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current // NUEVO: Obtener context aquí
    
    // NUEVO: Estados optimizados para thumbnails
    var thumbnailStates by remember { mutableStateOf(mutableMapOf<String, Bitmap?>()) }
    
    // Sincronizar selección visual con la lista externa
    LaunchedEffect(selectedItemsExternal, items) {
        if (selectedItemsExternal != null) {
            selectedItems.clear()
            selectedItems.addAll(selectedItemsExternal)
        }
    }
    
    var selectionMode by remember { mutableStateOf(false) }
    // Sincronizar selectionMode con la selección externa y limpiar selectedItems si es necesario
    LaunchedEffect(selectedItemsExternal) {
        if (selectedItemsExternal != null && selectedItemsExternal.isEmpty()) {
            selectionMode = false
            selectedItems.clear()
        }
    }
    
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var selectedFilter by remember { mutableStateOf(FilterType.ALL) }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val itemSize = 90.dp
    val columns = (screenWidth / (itemSize + 16.dp)).toInt().coerceAtLeast(2)
    val gridState = rememberLazyGridState()

    // Pull to refresh
    val pullRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)

    // NUEVO: Filtrar items usando derivedStateOf para evitar recomposiciones innecesarias
    val filteredItems by remember(items, searchQuery.text, selectedFilter) {
        derivedStateOf {
            items.filter { item ->
                val matchesSearch = searchQuery.text.isEmpty() || 
                    item.id.lowercase().contains(searchQuery.text.lowercase())
                val matchesFilter = when (selectedFilter) {
                    FilterType.ALL -> true
                    FilterType.IMAGES -> item is GalleryItem.Image
                    FilterType.VIDEOS -> item is GalleryItem.Video
                }
                matchesSearch && matchesFilter
            }
        }
    }

    LaunchedEffect(scrollToIndex) {
        if (scrollToIndex != null && scrollToIndex in items.indices) {
            gridState.scrollToItem(scrollToIndex)
        }
    }

    // NUEVO: Cargar thumbnails de forma lazy
    LaunchedEffect(filteredItems) {
        val videoItems = filteredItems.filterIsInstance<GalleryItem.Video>()
        videoItems.forEach { videoItem ->
            if (!thumbnailStates.containsKey(videoItem.id)) {
                scope.launch {
                    val thumbnail = getVideoThumbnailAsync(context, videoItem.id)
                    thumbnailStates = thumbnailStates.toMutableMap().apply {
                        put(videoItem.id, thumbnail)
                    }
                }
            }
        }
    }

    // NUEVO: Estado para la vista previa
    var previewItem by remember { mutableStateOf<GalleryItem?>(null) }

    SwipeRefresh(
        state = pullRefreshState,
        onRefresh = { onRefresh?.invoke() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
        // Eliminar la Card de selección duplicada:
        // if (showSelectionCount && selectedItems.isNotEmpty()) { ... }
        // Dejar solo el grid y lógica de selección, sin barra de selección aquí.

        // Resultados de búsqueda
        if (filteredItems.isEmpty() && (searchQuery.text.isNotEmpty() || selectedFilter != FilterType.ALL)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = "Sin resultados",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No se encontraron archivos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (searchQuery.text.isNotEmpty()) {
                        Text(
                            text = "Intenta con otros términos de búsqueda",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Grid de archivos
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(columns),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = itemSize * 2),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredItems) { item ->
                val isSelected = selectedItems.contains(item)
                // Animación de rebote y color
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.08f else 1f,
                    animationSpec = tween(durationMillis = 180), label = "scale_anim"
                )
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else Color.Transparent,
                    animationSpec = tween(durationMillis = 180), label = "color_anim"
                )
                Card(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .background(bgColor)
                        .combinedClickable(
                            onClick = {
                                // NUEVO: Abrir vista previa modal si no hay selección múltiple
                                if (selectedItems.isEmpty()) {
                                    previewItem = item
                                } else {
                                    if (isSelected) {
                                        selectedItems.removeAt(selectedItems.indexOf(item))
                                        if (selectedItems.isEmpty()) {
                                            selectionMode = false
                                        }
                                    } else {
                                        selectedItems.add(item)
                                    }
                                    onSelectionChange(selectedItems)
                                }
                            },
                            onLongClick = {
                                if (selectedItems.isEmpty()) {
                                    selectionMode = true
                                    selectedItems.add(item)
                                    onSelectionChange(selectedItems)
                                }
                            }
                        ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        
                        if (item is GalleryItem.Video) {
                            val thumbnail = thumbnailStates[item.id]
                            if (thumbnail != null) {
                                Image(
                                    painter = BitmapPainter(thumbnail.asImageBitmap()),
                                    contentDescription = "Miniatura de video",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                // Placeholder mientras carga el thumbnail
                                Box(Modifier.fillMaxSize().background(Color.DarkGray))
                            }
                        } else {
                            AsyncImage(
                                model = item.id,
                                contentDescription = "Imagen",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        if (item is GalleryItem.Video) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(50))
                                    .size(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_play),
                                    contentDescription = "Play",
                                    tint = Color.White
                                )
                            }
                        }
                        // Mostrar candado solo si showLockButton es true
                        if (showLockButton && appState != null) {
                            val isLocked = appState.isFileLocked(item.id)
                            val rotation by animateFloatAsState(
                                targetValue = if (isLocked) 0f else 180f,
                                animationSpec = tween(durationMillis = 300),
                                label = "lock_rotation"
                            )
                            IconButton(
                                onClick = { 
                                    appState.toggleFileLock(item.id)
                                    item.isLocked = !item.isLocked
                                },
                                modifier = Modifier.align(Alignment.TopEnd).zIndex(2f)
                            ) {
                                Icon(
                                    painter = painterResource(id = if (isLocked) R.drawable.ic_lock else R.drawable.ic_lock_open),
                                    contentDescription = stringResource(if (isLocked) R.string.locked else R.string.unlocked),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.graphicsLayer(rotationY = rotation)
                                )
                            }
                        }
                        // Icono de selección siempre encima
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xCC000000))
                                    .zIndex(3f)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_check),
                                    contentDescription = "Seleccionado",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(28.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // NUEVO: indicador de pull to refresh
        // PullRefreshIndicator(
        //     refreshing = isRefreshing,
        //     state = pullRefreshState,
        //     modifier = Modifier.align(Alignment.CenterHorizontally)
        // )
        }
    }
    // Mostrar Snackbar si se pasa un host
    if (snackbarHostState != null) {
        Box(Modifier.fillMaxWidth()) {
            SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
    // NUEVO: Mostrar la vista previa modal si corresponde
    if (previewItem != null) {
        val initialIndex = items.indexOf(previewItem)
        MediaPreviewer(
            items = items,
            initialIndex = if (initialIndex >= 0) initialIndex else 0,
            onClose = { previewItem = null }
        )
    }
} 