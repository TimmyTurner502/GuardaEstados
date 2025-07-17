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
     */
    fun listMediaFilesInFolder(path: String): List<File> {
        val dir = File(path)
        val files = if (dir.exists() && dir.isDirectory) {
            dir.listFiles()?.filter {
                it.isFile && (it.name.endsWith(".jpg", true) || it.name.endsWith(".jpeg", true) || it.name.endsWith(".png", true) || it.name.endsWith(".mp4", true))
            }?.sortedByDescending { it.lastModified() } ?: emptyList()
        } else emptyList()
        Log.d("FileUtils", "Archivos encontrados en $path: ${files.map { it.name }}")
        return files
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

    // NUEVO: Búsqueda profunda y automática de la carpeta .Statuses para cada instancia
    fun findStatusesRecursively(baseDir: File): File? {
        if (!baseDir.exists() || !baseDir.isDirectory) return null
        if (baseDir.name == ".Statuses") return baseDir
        baseDir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                val found = findStatusesRecursively(file)
                if (found != null) return found
            }
        }
        return null
    }

    // Eliminar la función duplicada de getStatusFilesForInstanceDeep que está más abajo en el archivo (línea 164 aprox.)

    // NUEVO: Búsqueda profunda global de la carpeta .Statuses en todo el almacenamiento externo
    fun findStatusesGlobally(): File? {
        val roots = listOf(
            Environment.getExternalStorageDirectory(),
            File("/storage/emulated/0/"),
            File("/sdcard/"),
            File("/storage/"),
        )
        for (root in roots) {
            val found = findStatusesRecursively(root)
            if (found != null && found.exists() && found.isDirectory) {
                val files = found.listFiles()?.filter { it.isFile && (it.name.endsWith(".jpg") || it.name.endsWith(".mp4")) }
                if (!files.isNullOrEmpty()) return found
            }
        }
        return null
    }

    fun getStatusFilesGlobal(): List<File> {
        val statusesDir = findStatusesGlobally()
        return if (statusesDir != null && statusesDir.exists() && statusesDir.isDirectory) {
            statusesDir.listFiles()?.filter { it.isFile && (it.name.endsWith(".jpg") || it.name.endsWith(".mp4")) }?.sortedByDescending { it.lastModified() } ?: emptyList()
        } else emptyList()
    }
} 