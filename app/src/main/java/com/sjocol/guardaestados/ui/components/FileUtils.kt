package com.sjocol.guardaestados.ui.components

import android.content.Context
import android.os.Environment
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import android.content.res.Configuration
import android.os.Build
import com.sjocol.guardaestados.ui.components.PermissionUtils
import java.util.regex.Pattern
import android.util.Log

data class WhatsAppInstance(val name: String, val path: String)

object FileUtils {
    // Nombres claros para las instancias
    private val instanceLabels = listOf(
        "WhatsApp",
        "WhatsApp Business"
    )
    // Rutas relativas para cada tipo de instancia (solo permitidas)
    private val instanceRelativePaths = listOf(
        "Android/media/com.whatsapp/WhatsApp/Media/.Statuses",
        "Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses"
    )

    /**
     * Busca solo las instancias permitidas por Android 11+: WhatsApp y WhatsApp Business
     * en /storage/emulated/0/Android/media/.
     */
    fun getAvailableInstances(context: Context): List<WhatsAppInstance> {
        val userDir = File("/storage/emulated/0/")
        val found = mutableListOf<WhatsAppInstance>()
        for ((i, relPath) in instanceRelativePaths.withIndex()) {
            val fullPath = File(userDir, relPath)
            Log.d("FileUtils", "Buscando en: ${fullPath.absolutePath}")
            if (fullPath.exists() && fullPath.isDirectory) {
                val label = instanceLabels[i]
                found += WhatsAppInstance(label, fullPath.absolutePath)
            }
        }
        Log.d("FileUtils", "Instancias encontradas: ${found.map { it.name + ": " + it.path }}")
        return found
    }

    // NUEVO: Versión asíncrona para evitar bloqueos
    suspend fun getAvailableInstancesAsync(context: Context): List<WhatsAppInstance> = withContext(Dispatchers.IO) {
        getAvailableInstances(context)
    }

    /**
     * Lista todos los archivos de imagen y video en la carpeta dada.
     * VERSIÓN ROBUSTA Y DEPURABLE
     */
    fun listMediaFilesInFolder(path: String): List<File> {
        val dir = File(path)
        Log.d("FileUtils", "=== DEPURACIÓN: Buscando en $path ===")
        Log.d("FileUtils", "Carpeta existe: ${dir.exists()}")
        Log.d("FileUtils", "Es directorio: ${dir.isDirectory}")
        
        if (!dir.exists()) {
            Log.e("FileUtils", "ERROR: La carpeta NO existe: $path")
            return emptyList()
        }
        
        if (!dir.isDirectory()) {
            Log.e("FileUtils", "ERROR: No es un directorio: $path")
            return emptyList()
        }
        
        val allFiles = dir.listFiles()
        Log.d("FileUtils", "Total de archivos en carpeta: ${allFiles?.size ?: 0}")
        
        if (allFiles == null) {
            Log.e("FileUtils", "ERROR: No se puede listar archivos en: $path")
            return emptyList()
        }
        
        // Mostrar TODOS los archivos encontrados
        allFiles.forEach { file ->
            Log.d("FileUtils", "Archivo encontrado: ${file.name} (${file.length()} bytes)")
        }
        
        // Filtrar solo archivos de imagen y video
        val mediaFiles = allFiles.filter {
            it.isFile && (
                it.name.lowercase().endsWith(".jpg") || 
                it.name.lowercase().endsWith(".jpeg") || 
                it.name.lowercase().endsWith(".png") || 
                it.name.lowercase().endsWith(".mp4") ||
                it.name.lowercase().endsWith(".mov") ||
                it.name.lowercase().endsWith(".avi")
            )
        }.sortedByDescending { it.lastModified() }
        
        Log.d("FileUtils", "Archivos de media encontrados: ${mediaFiles.size}")
        mediaFiles.forEach { file ->
            Log.d("FileUtils", "Media file: ${file.name} (${file.length()} bytes, modificado: ${file.lastModified()})")
        }
        
        return mediaFiles
    }

    fun getStatusFilesForInstanceDeep(instancePath: String): List<File> {
        return listMediaFilesInFolder(instancePath)
    }

    fun getSavedFiles(customFolder: String? = null): List<File> {
        val downloadsDir = if (customFolder != null) File(customFolder) else File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "StatusWtsUp")
        return listMediaFilesInFolder(downloadsDir.absolutePath)
    }

    fun getFullPathFromTreeUri(context: Context, treeUri: android.net.Uri): String? {
        // Solo soporta almacenamiento externo primario
        val docId = android.provider.DocumentsContract.getTreeDocumentId(treeUri)
        val parts = docId.split(":")
        if (parts.size == 2 && parts[0] == "primary") {
            return android.os.Environment.getExternalStorageDirectory().absolutePath + "/" + parts[1]
        }
        return null
    }

    // NUEVO: Detectar si es tablet
    fun isTablet(context: Context): Boolean {
        return (context.resources.configuration.screenLayout and 
            Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
    }

    // NUEVO: Obtener versión de Android
    fun getAndroidVersion(): Int {
        return Build.VERSION.SDK_INT
    }

    // NUEVO: Obtener posibles rutas de estados según el dispositivo
    fun getPossibleStatusPaths(context: Context): List<String> {
        val paths = mutableListOf<String>()
        val isTablet = isTablet(context)
        val version = getAndroidVersion()
        // Ruta estándar (Android 11+)
        paths.add("/storage/emulated/0/Android/media/com.whatsapp/WhatsApp/Media/.Statuses")
        // Ruta antigua (Android < 11)
        paths.add("/storage/emulated/0/WhatsApp/Media/.Statuses")
        // WhatsApp Business
        paths.add("/storage/emulated/0/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses")
        paths.add("/storage/emulated/0/WhatsApp Business/Media/.Statuses")
        // Rutas alternativas para tablets o dispositivos vinculados
        if (isTablet) {
            paths.add("/storage/emulated/0/Documents/WhatsApp/Media/.Statuses")
            paths.add("/storage/emulated/0/Download/WhatsApp/Media/.Statuses")
        }
        // NUEVO: Buscar en caché de WhatsApp (algunas tablets y multi-dispositivo)
        paths.add("/storage/emulated/0/Android/data/com.whatsapp/cache/Media/.Statuses")
        paths.add("/storage/emulated/0/Android/data/com.whatsapp/cache/.Statuses")
        paths.add("/storage/emulated/0/Android/data/com.whatsapp.w4b/cache/Media/.Statuses")
        paths.add("/storage/emulated/0/Android/data/com.whatsapp.w4b/cache/.Statuses")
        // Otras rutas de caché posibles
        paths.add("/storage/emulated/0/WhatsApp/cache/.Statuses")
        paths.add("/storage/emulated/0/WhatsApp/Cache/.Statuses")
        return paths
    }

    // NUEVO: Buscar archivos de estado probando todas las rutas posibles
    fun getStatusFilesSmart(context: Context): List<File> {
        val paths = getPossibleStatusPaths(context)
        for (path in paths) {
            val dir = File(path)
            if (dir.exists() && dir.isDirectory) {
                val files = dir.listFiles()?.filter { it.isFile && (it.name.endsWith(".jpg") || it.name.endsWith(".mp4")) } ?: emptyList()
                if (files.isNotEmpty()) return files.sortedByDescending { it.lastModified() }
            }
        }
        return emptyList()
    }

    // NUEVO: Pedir permiso de almacenamiento si no se tiene
    fun ensureStoragePermission(activity: android.app.Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            if (!PermissionUtils.hasStoragePermission(activity)) {
                PermissionUtils.requestStoragePermission(activity)
            }
        } else {
            // Android 11+: Mostrar mensaje para usar SAF
            // Aquí puedes lanzar un diálogo o navegación a la UI de SAF
        }
    }

    // FUNCIONES ELIMINADAS: Las funciones findStatusesRecursively, findStatusesGlobally y getStatusFilesGlobal
    // fueron eliminadas porque causaban bucles infinitos y colgaban la aplicación.
    // En su lugar, se usa getStatusFilesForInstanceDeep que es más segura y eficiente.
} 