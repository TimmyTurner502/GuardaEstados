package com.sjocol.guardaestados.ui.components

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class WhatsAppInstance(val name: String, val path: String)

object FileUtils {
    
    // Rutas de instancias de WhatsApp
    private val instanceRelativePaths = listOf(
        "Android/media/com.whatsapp/WhatsApp/Media/.Statuses",
        "Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses"
    )
    
    private val instanceLabels = listOf(
        "WhatsApp",
        "WhatsApp Business"
    )

    /**
     * NUEVA IMPLEMENTACIÓN ROBUSTA
     * Busca instancias de WhatsApp disponibles
     */
    fun getAvailableInstances(context: Context): List<WhatsAppInstance> {
        val userDir = File("/storage/emulated/0/")
        val found = mutableListOf<WhatsAppInstance>()
        
        Log.d("FileUtils", "=== BUSCANDO INSTANCIAS DE WHATSAPP ===")
        
        for ((i, relPath) in instanceRelativePaths.withIndex()) {
            val fullPath = File(userDir, relPath)
            Log.d("FileUtils", "Verificando: ${fullPath.absolutePath}")
            
            if (fullPath.exists() && fullPath.isDirectory) {
                val label = instanceLabels[i]
                found += WhatsAppInstance(label, fullPath.absolutePath)
                Log.d("FileUtils", "✅ Instancia encontrada: $label en ${fullPath.absolutePath}")
            } else {
                Log.d("FileUtils", "❌ Instancia no encontrada: ${fullPath.absolutePath}")
            }
        }
        
        Log.d("FileUtils", "Total de instancias encontradas: ${found.size}")
        return found
    }

    /**
     * NUEVA IMPLEMENTACIÓN ROBUSTA
     * Lista archivos de media en una carpeta específica
     */
    fun listMediaFilesInFolder(path: String): List<File> {
        Log.d("FileUtils", "=== LISTANDO ARCHIVOS EN: $path ===")
        
        val dir = File(path)
        
        // Verificar que la carpeta existe
        if (!dir.exists()) {
            Log.e("FileUtils", "❌ La carpeta NO existe: $path")
            return emptyList()
        }
        
        if (!dir.isDirectory()) {
            Log.e("FileUtils", "❌ No es un directorio: $path")
            return emptyList()
        }
        
        // Verificar permisos de lectura
        if (!dir.canRead()) {
            Log.e("FileUtils", "❌ No se puede leer la carpeta: $path")
            return emptyList()
        }
        
        Log.d("FileUtils", "✅ Carpeta válida y legible: $path")
        
        // Listar archivos con manejo de errores
        val allFiles = try {
            dir.listFiles()
        } catch (e: SecurityException) {
            Log.e("FileUtils", "❌ Error de seguridad al listar archivos: ${e.message}")
            null
        } catch (e: Exception) {
            Log.e("FileUtils", "❌ Error inesperado al listar archivos: ${e.message}")
            null
        }
        
        if (allFiles == null) {
            Log.e("FileUtils", "❌ No se pudieron listar archivos en: $path")
            return emptyList()
        }
        
        Log.d("FileUtils", "📁 Total de archivos encontrados: ${allFiles.size}")
        
        // Mostrar todos los archivos encontrados
        allFiles.forEach { file ->
            Log.d("FileUtils", "📄 Archivo: ${file.name} (${file.length()} bytes, legible: ${file.canRead()})")
        }
        
        // Filtrar solo archivos de media válidos
        val mediaFiles = allFiles.filter { file ->
            val isValidFile = file.isFile && file.canRead() && file.length() > 0
            val isValidExtension = file.name.lowercase().let { name ->
                name.endsWith(".jpg") || 
                name.endsWith(".jpeg") || 
                name.endsWith(".png") || 
                name.endsWith(".mp4") ||
                name.endsWith(".mov") ||
                name.endsWith(".avi")
            }
            
            if (isValidFile && isValidExtension) {
                Log.d("FileUtils", "✅ Archivo de media válido: ${file.name}")
                true
            } else {
                Log.d("FileUtils", "❌ Archivo descartado: ${file.name} (archivo: $isValidFile, extensión: $isValidExtension)")
                false
            }
        }.sortedByDescending { it.lastModified() }
        
        Log.d("FileUtils", "🎬 Archivos de media válidos: ${mediaFiles.size}")
        mediaFiles.forEach { file ->
            Log.d("FileUtils", "🎬 Media: ${file.name} (${file.length()} bytes, modificado: ${file.lastModified()})")
        }
        
        return mediaFiles
    }

    /**
     * NUEVA IMPLEMENTACIÓN ROBUSTA
     * Obtiene archivos de estados para una instancia específica
     */
    fun getStatusFilesForInstanceDeep(instancePath: String): List<File> {
        Log.d("FileUtils", "=== OBTENIENDO ESTADOS PARA: $instancePath ===")
        
        val files = listMediaFilesInFolder(instancePath)
        
        Log.d("FileUtils", "📊 Resultado final: ${files.size} archivos de estados encontrados")
        
        return files
    }

    /**
     * Obtiene archivos guardados
     */
    fun getSavedFiles(customFolder: String? = null): List<File> {
        val downloadsDir = if (customFolder != null) {
            File(customFolder)
        } else {
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "StatusWtsUp")
        }
        
        Log.d("FileUtils", "=== OBTENIENDO ARCHIVOS GUARDADOS EN: ${downloadsDir.absolutePath} ===")
        
        return listMediaFilesInFolder(downloadsDir.absolutePath)
    }

    /**
     * Obtener ruta completa desde URI de árbol (para SAF)
     */
    fun getFullPathFromTreeUri(context: Context, treeUri: android.net.Uri): String? {
        // Solo soporta almacenamiento externo primario
        val docId = android.provider.DocumentsContract.getTreeDocumentId(treeUri)
        val parts = docId.split(":")
        if (parts.size == 2 && parts[0] == "primary") {
            return android.os.Environment.getExternalStorageDirectory().absolutePath + "/" + parts[1]
        }
        return null
    }

    /**
     * NUEVA IMPLEMENTACIÓN ROBUSTA: Verificar permisos de almacenamiento
     */
    fun checkStoragePermissions(context: Context): Boolean {
        Log.d("FileUtils", "=== VERIFICANDO PERMISOS DE ALMACENAMIENTO ===")
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+: Verificar MANAGE_EXTERNAL_STORAGE
            val hasManagePermission = try {
                android.os.Environment.isExternalStorageManager()
            } catch (e: Exception) {
                Log.e("FileUtils", "❌ Error verificando permisos Android 11+: ${e.message}")
                false
            }
            
            Log.d("FileUtils", "🔐 Permisos Android 11+: $hasManagePermission")
            hasManagePermission
        } else {
            // Android < 11: Verificar permisos tradicionales
            val hasLegacyPermission = PermissionUtils.hasStoragePermission(context as android.app.Activity)
            Log.d("FileUtils", "🔐 Permisos Android < 11: $hasLegacyPermission")
            hasLegacyPermission
        }
    }

    /**
     * NUEVA FUNCIÓN: Obtener información detallada de una carpeta
     */
    fun getFolderInfo(path: String): Map<String, Any> {
        val dir = File(path)
        return mapOf(
            "exists" to dir.exists(),
            "isDirectory" to dir.isDirectory(),
            "canRead" to dir.canRead(),
            "canWrite" to dir.canWrite(),
            "totalSpace" to dir.totalSpace,
            "freeSpace" to dir.freeSpace,
            "absolutePath" to dir.absolutePath
        )
    }

    /**
     * NUEVA FUNCIÓN: Buscar archivos recursivamente (con límites de seguridad)
     */
    fun findFilesRecursively(basePath: String, maxDepth: Int = 3): List<File> {
        val results = mutableListOf<File>()
        val baseDir = File(basePath)
        
        if (!baseDir.exists() || !baseDir.isDirectory) {
            return results
        }
        
        fun searchRecursively(dir: File, currentDepth: Int) {
            if (currentDepth > maxDepth) return
            
            try {
                val files = dir.listFiles() ?: return
                
                for (file in files) {
                    if (file.isFile && file.canRead()) {
                        val name = file.name.lowercase()
                        if (name.endsWith(".jpg") || name.endsWith(".jpeg") || 
                            name.endsWith(".png") || name.endsWith(".mp4")) {
                            results.add(file)
                        }
                    } else if (file.isDirectory && file.canRead() && currentDepth < maxDepth) {
                        searchRecursively(file, currentDepth + 1)
                    }
                }
            } catch (e: Exception) {
                Log.e("FileUtils", "Error en búsqueda recursiva: ${e.message}")
            }
        }
        
        searchRecursively(baseDir, 0)
        return results
    }

    /**
     * NUEVA FUNCIÓN: Verificar si un archivo es accesible
     */
    fun isFileAccessible(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            file.exists() && file.canRead() && file.length() > 0
        } catch (e: Exception) {
            Log.e("FileUtils", "Error verificando acceso a archivo: ${e.message}")
            false
        }
    }

    /**
     * NUEVA FUNCIÓN: Obtener estadísticas de archivos
     */
    fun getFileStats(files: List<File>): Map<String, Any> {
        val totalSize = files.sumOf { it.length() }
        val imageCount = files.count { it.name.lowercase().endsWith(".jpg") || 
                                     it.name.lowercase().endsWith(".jpeg") || 
                                     it.name.lowercase().endsWith(".png") }
        val videoCount = files.count { it.name.lowercase().endsWith(".mp4") || 
                                     it.name.lowercase().endsWith(".mov") || 
                                     it.name.lowercase().endsWith(".avi") }
        
        return mapOf(
            "totalFiles" to files.size,
            "totalSize" to totalSize,
            "imageCount" to imageCount,
            "videoCount" to videoCount,
            "averageSize" to if (files.isNotEmpty()) totalSize / files.size else 0
        )
    }
} 