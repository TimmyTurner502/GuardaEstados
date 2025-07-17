package com.sjocol.guardaestados.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import com.sjocol.guardaestados.R
import com.sjocol.guardaestados.ui.components.PermissionUtils

object FileActions {
    fun shareFile(context: Context, filePath: String) {
        try {
            if (filePath.startsWith("content://")) {
                val uri = Uri.parse(filePath)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = if (filePath.endsWith(".mp4")) "video/*" else "image/*"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Compartir archivo"))
                return
            }
            val file = File(filePath)
            if (!file.exists()) {
                Toast.makeText(context, "Archivo no encontrado", Toast.LENGTH_SHORT).show()
                return
            }
            val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = if (filePath.endsWith(".mp4")) "video/*" else "image/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Compartir archivo"))
        } catch (e: Exception) {
            Toast.makeText(context, "Error al compartir archivo", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareMultipleFiles(context: Context, filePaths: List<String>) {
        try {
            val uris = filePaths.mapNotNull {
                if (it.startsWith("content://")) Uri.parse(it)
                else {
                    val file = File(it)
                    if (file.exists()) FileProvider.getUriForFile(context, context.packageName + ".provider", file)
                    else null
                }
            }
            if (uris.isEmpty()) {
                Toast.makeText(context, "No hay archivos válidos para compartir", Toast.LENGTH_SHORT).show()
                return
            }
            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = if (filePaths.any { it.endsWith(".mp4") }) "video/*" else "image/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Compartir ${uris.size} archivos"))
        } catch (e: Exception) {
            Toast.makeText(context, "Error al compartir archivos", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteFile(context: Context, filePath: String, onDeleted: () -> Unit) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                Toast.makeText(context, "Archivo no encontrado", Toast.LENGTH_SHORT).show()
                return
            }
            if (file.delete()) {
                onDeleted()
            } else {
                Toast.makeText(context, "No se pudo eliminar el archivo", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error al eliminar archivo", Toast.LENGTH_SHORT).show()
        }
    }

    fun downloadFile(context: Context, filePath: String, onDownloaded: () -> Unit, customFolder: String? = null) {
        try {
            val isContentUri = filePath.startsWith("content://")
            val fileName = if (isContentUri) {
                // Obtener nombre del archivo desde el content resolver
                val uri = Uri.parse(filePath)
                var name = "descarga"
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && nameIndex >= 0) {
                        name = cursor.getString(nameIndex)
                    }
                }
                name
            } else File(filePath).name
            // Carpeta destino
            var downloadsDir: File? = null
            var usingDefault = false
            if (customFolder != null) {
                downloadsDir = File(customFolder)
                if (!downloadsDir.exists()) {
                    val created = downloadsDir.mkdirs()
                    if (!created) {
                        downloadsDir = null
                        usingDefault = true
                    }
                }
            }
            if (downloadsDir == null) {
                downloadsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "StatusWtsUp")
                if (!downloadsDir.exists()) {
                    val created = downloadsDir.mkdirs()
                    if (!created) {
                        Toast.makeText(context, "No se pudo crear la carpeta destino pública", Toast.LENGTH_SHORT).show()
                        return
                    }
                }
            }
            val destFile = File(downloadsDir, fileName)
            if (isContentUri) {
                val uri = Uri.parse(filePath)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                } ?: throw IOException("No se pudo abrir el archivo de origen")
            } else {
                val sourceFile = File(filePath)
                if (!sourceFile.exists()) {
                    Toast.makeText(context, "Archivo fuente no encontrado", Toast.LENGTH_SHORT).show()
                    return
                }
                sourceFile.copyTo(destFile, overwrite = true)
            }
            if (!destFile.exists()) {
                Toast.makeText(context, "Error: el archivo no se copió", Toast.LENGTH_SHORT).show()
                return
            }
            onDownloaded()
            val msg = if (usingDefault) "Archivo descargado en carpeta pública: ${destFile.absolutePath}" else "Archivo descargado en: ${destFile.absolutePath}"
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error al descargar archivo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun repostFile(context: Context, filePath: String, packageName: String? = null) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                Toast.makeText(context, context.getString(R.string.no_saved_files), Toast.LENGTH_SHORT).show()
                return
            }
            val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)
            val pm = context.packageManager
            val targetPackages = if (packageName != null) listOf(packageName) else listOf(
                "com.whatsapp", "com.whatsapp.clone", "com.whatsapp.w4b", "com.whatsapp.w4b.clone"
            )
            var opened = false
            for (pkg in targetPackages) {
                try {
                    pm.getPackageInfo(pkg, 0)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = if (filePath.endsWith(".mp4")) "video/*" else "image/*"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        setPackage(pkg)
                        putExtra("jid", "status@broadcast")
                    }
                    context.startActivity(intent)
                    opened = true
                    break
                } catch (e: Exception) {
                    // Continuar con el siguiente paquete
                }
            }
            if (!opened) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = if (filePath.endsWith(".mp4")) "video/*" else "image/*"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_files)))
            }
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.whatsapp_error), Toast.LENGTH_SHORT).show()
        }
    }

    fun sendWhatsAppMessageToNumber(context: Context, phoneNumber: String, message: String) {
        val resources = context.resources
        val invalidNumberMsg = resources.getString(R.string.invalid_phone_number)
        val whatsappErrorMsg = resources.getString(R.string.whatsapp_error)
        val noWhatsappMsg = resources.getString(R.string.no_whatsapp_found)
        val minLength = 8

        var cleanNumber = phoneNumber.filter { it.isDigit() }
        while (cleanNumber.startsWith("0")) {
            cleanNumber = cleanNumber.drop(1)
        }
        if (cleanNumber.length < minLength) {
            Toast.makeText(context, invalidNumberMsg, Toast.LENGTH_SHORT).show()
            return
        }
        if (message.isBlank()) {
            Toast.makeText(context, resources.getString(R.string.message), Toast.LENGTH_SHORT).show()
            return
        }
        val url = "https://wa.me/$cleanNumber?text=" + Uri.encode(message)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Si falla, copiar al portapapeles y mostrar mensaje
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
            clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("WhatsApp Link", url))
            Toast.makeText(context, noWhatsappMsg, Toast.LENGTH_LONG).show()
        }
    }

    fun sendWhatsAppMessageToNumberSpecific(context: Context, phoneNumber: String, message: String, packageName: String) {
        try {
            val resources = context.resources
            val invalidNumberMsg = resources.getString(R.string.invalid_phone_number)
            val whatsappErrorMsg = resources.getString(R.string.whatsapp_error)
            val noWhatsappMsg = resources.getString(R.string.no_whatsapp_found)
            val minLength = 8
            var cleanNumber = phoneNumber.filter { it.isDigit() }
            while (cleanNumber.startsWith("0")) {
                cleanNumber = cleanNumber.drop(1)
            }
            if (cleanNumber.length < minLength) {
                Toast.makeText(context, invalidNumberMsg, Toast.LENGTH_SHORT).show()
                return
            }
            if (message.isBlank()) {
                Toast.makeText(context, resources.getString(R.string.message), Toast.LENGTH_SHORT).show()
                return
            }
            val url = "https://wa.me/$cleanNumber?text=" + Uri.encode(message)
            val pm = context.packageManager
            try {
                pm.getPackageInfo(packageName, 0)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(url)
                    setPackage(packageName)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                // Si no está instalada, abrir en navegador
                val intentWeb = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                val canOpenWeb = intentWeb.resolveActivity(pm) != null
                if (canOpenWeb) {
                    context.startActivity(intentWeb)
                } else {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                    clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("WhatsApp Link", url))
                    Toast.makeText(context, noWhatsappMsg, Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, context.resources.getString(R.string.whatsapp_error), Toast.LENGTH_SHORT).show()
        }
    }

    fun shareFiles(context: Context, filePaths: List<String>) {
        shareMultipleFiles(context, filePaths)
    }

    fun deleteFiles(context: Context, filePaths: List<String>, onDeleted: () -> Unit) {
        try {
            var deletedAny = false
            var totalFiles = 0
            filePaths.forEach { path ->
                val file = File(path)
                totalFiles++
                if (file.exists() && file.delete()) {
                    deletedAny = true
                }
            }
            if (deletedAny) {
                onDeleted()
            } else {
                Toast.makeText(context, "No se pudieron eliminar los archivos", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error al eliminar archivos", Toast.LENGTH_SHORT).show()
        }
    }

    fun downloadFiles(context: Context, filePaths: List<String>, onDownloaded: () -> Unit, customFolder: String? = null) {
        try {
            var downloadedAny = false
            filePaths.forEach { path ->
                downloadFile(context, path, {
                    downloadedAny = true
                }, customFolder)
            }
            if (downloadedAny) {
                onDownloaded()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error al descargar archivos", Toast.LENGTH_SHORT).show()
        }
    }

    fun openWhatsAppDual(context: Context) {
        try {
            val pm = context.packageManager
            val whatsappDualPackages = listOf(
                "com.whatsapp.clone",
                "com.whatsapp.w4b.clone"
            )
            
            val availablePackages = whatsappDualPackages.filter { pkg ->
                try {
                    pm.getPackageInfo(pkg, 0)
                    true
                } catch (e: Exception) { false }
            }
            
            when {
                availablePackages.isEmpty() -> {
                    Toast.makeText(context, "No se encontró WhatsApp Dual instalado", Toast.LENGTH_SHORT).show()
                }
                availablePackages.size == 1 -> {
                    val intent = pm.getLaunchIntentForPackage(availablePackages.first())
                    if (intent != null) {
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, "No se pudo abrir WhatsApp Dual", Toast.LENGTH_SHORT).show()
                    }
                }
                else -> {
                    // Si hay múltiples, mostrar selector
                    val intents = availablePackages.map { pkg ->
                        pm.getLaunchIntentForPackage(pkg)
                    }.filterNotNull()
                    
                    if (intents.isNotEmpty()) {
                        val chooser = Intent.createChooser(intents.first(), "Abrir WhatsApp Dual")
                        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.drop(1).toTypedArray())
                        context.startActivity(chooser)
                    } else {
                        Toast.makeText(context, "No se pudo abrir WhatsApp Dual", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error al abrir WhatsApp Dual", Toast.LENGTH_SHORT).show()
        }
    }

    fun getAvailableWhatsAppPackages(context: Context): List<Pair<String, String>> {
        val pm = context.packageManager
        val result = mutableListOf<Pair<String, String>>()
        val seen = mutableSetOf<String>()
        
        // Método 1: Buscar todas las apps instaladas
        try {
            val packages = pm.getInstalledApplications(0)
            for (app in packages) {
                val pkg = app.packageName
                if (pkg.contains("whatsapp", ignoreCase = true) && pkg !in seen) {
                    val label = pm.getApplicationLabel(app).toString()
                    result.add(pkg to label)
                    seen.add(pkg)
                    android.util.Log.d("WhatsAppDetector", "Encontrado: $pkg - $label")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("WhatsAppDetector", "Error en método 1: ${e.message}")
        }
        
        // Método 2: Buscar por intent de compartir
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
            }
            val resolveInfos = pm.queryIntentActivities(intent, 0)
            for (info in resolveInfos) {
                val pkg = info.activityInfo.packageName
                if (pkg.contains("whatsapp", ignoreCase = true) && pkg !in seen) {
                    val label = info.loadLabel(pm).toString()
                    result.add(pkg to label)
                    seen.add(pkg)
                    android.util.Log.d("WhatsAppDetector", "Encontrado por intent: $pkg - $label")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("WhatsAppDetector", "Error en método 2: ${e.message}")
        }
        
        // Método 3: Lista hardcodeada de paquetes conocidos
        val knownPackages = listOf(
            "com.whatsapp" to "WhatsApp",
            "com.whatsapp.w4b" to "WhatsApp Business",
            "com.whatsapp.clone" to "WhatsApp Dual",
            "com.whatsapp.w4b.clone" to "WhatsApp Business Dual",
            "com.whatsapp.plus" to "WhatsApp Plus",
            "com.whatsapp.gb" to "GBWhatsApp",
            "com.whatsapp.yowhatsapp" to "YoWhatsApp"
        )
        
        for ((pkg, label) in knownPackages) {
            if (pkg !in seen) {
                try {
                    pm.getPackageInfo(pkg, 0)
                    result.add(pkg to label)
                    seen.add(pkg)
                    android.util.Log.d("WhatsAppDetector", "Encontrado conocido: $pkg - $label")
                } catch (e: Exception) {
                    // App no instalada, continuar
                }
            }
        }
        
        android.util.Log.d("WhatsAppDetector", "Total encontrado: ${result.size}")
        return result
    }
} 