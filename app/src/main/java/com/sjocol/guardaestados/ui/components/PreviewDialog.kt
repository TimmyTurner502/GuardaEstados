package com.sjocol.guardaestados.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sjocol.guardaestados.R
import com.sjocol.guardaestados.AppState
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.BackHandler
import android.widget.Toast
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.zIndex

@Composable
fun PreviewScreen(
    items: List<GalleryItem>,
    initialIndex: Int,
    onDismiss: () -> Unit,
    appState: AppState
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(initialPage = initialIndex) { items.size }
    var currentIndex by remember { mutableStateOf(initialIndex) }
    
    // DEBUG: Toast para confirmar que se abre
    LaunchedEffect(Unit) {
        Toast.makeText(context, "VISTA PREVIA ABIERTA - Índice: $initialIndex", Toast.LENGTH_SHORT).show()
    }
    
    BackHandler {
        onDismiss()
    }
    
    // NUEVO: Box principal con fondo negro sólido y zIndex máximo - FUERZA BRUTA
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .zIndex(9999f) // Z-INDEX MÁXIMO POSIBLE
    ) {
        // DEBUG: Texto de confirmación visible - FUERZA BRUTA
        Text(
            text = "VISTA PREVIA ACTIVA - Índice: $currentIndex",
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .background(Color.Red, RoundedCornerShape(8.dp))
                .padding(8.dp)
                .zIndex(10000f) // Z-INDEX MÁXIMO POSIBLE
        )
        
        // Pager horizontal para navegar entre imágenes/videos
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            currentIndex = page
            
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when (val item = items[page]) {
                    is GalleryItem.Image -> {
                        AsyncImage(
                            model = item.id,
                            contentDescription = "Imagen",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    is GalleryItem.Video -> {
                        // Por ahora solo mostrar un placeholder para videos
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.DarkGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_play),
                                    contentDescription = "Video",
                                    tint = Color.White,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Video: ${item.id.split("/").lastOrNull() ?: "Video"}",
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Botón de cerrar en la esquina superior derecha
        IconButton(
            onClick = { onDismiss() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .zIndex(1002f)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = "Cerrar",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        
        // Información del archivo en la parte inferior
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .zIndex(1002f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "Archivo ${currentIndex + 1} de ${items.size}",
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = items[currentIndex].id.split("/").lastOrNull() ?: "Archivo",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
} 