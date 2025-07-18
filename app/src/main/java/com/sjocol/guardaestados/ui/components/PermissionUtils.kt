package com.sjocol.guardaestados.ui.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.util.Log

object PermissionUtils {
    
    private const val STORAGE_PERMISSION_REQUEST_CODE = 1001
    private const val MANAGE_STORAGE_REQUEST_CODE = 1002

    fun hasStoragePermission(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+: Verificar permisos de administración
            hasManageExternalStoragePermission(activity)
        } else {
            // Android < 11: Verificar permisos tradicionales
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestStoragePermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+: Solicitar permiso de administración
            requestManageExternalStoragePermission(activity)
        } else {
            // Android < 11: Solicitar permisos tradicionales
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                STORAGE_PERMISSION_REQUEST_CODE
            )
        }
    }

    /**
     * NUEVA FUNCIÓN: Verificar permiso de administración de almacenamiento (Android 11+)
     */
    fun hasManageExternalStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                // Verificar si tenemos el permiso MANAGE_EXTERNAL_STORAGE
                val hasPermission = android.os.Environment.isExternalStorageManager()
                Log.d("PermissionUtils", "🔐 Permiso MANAGE_EXTERNAL_STORAGE: $hasPermission")
                
                // Verificar también si podemos acceder a archivos de WhatsApp
                val testPaths = listOf(
                    "/storage/emulated/0/Android/media/com.whatsapp/WhatsApp/Media/.Statuses",
                    "/storage/emulated/0/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses"
                )
                
                val canAccessFiles = testPaths.any { path ->
                    val testFile = java.io.File(path)
                    val exists = testFile.exists()
                    val canRead = testFile.canRead()
                    Log.d("PermissionUtils", "🔍 Probando acceso a $path: existe=$exists, legible=$canRead")
                    exists && canRead
                }
                
                Log.d("PermissionUtils", "📁 Puede acceder a archivos: $canAccessFiles")
                hasPermission && canAccessFiles
            } catch (e: Exception) {
                Log.e("PermissionUtils", "❌ Error verificando permisos: ${e.message}")
                false
            }
        } else {
            true // En Android < 11 no necesitamos este permiso
        }
    }

    /**
     * NUEVA FUNCIÓN: Solicitar permiso de administración de almacenamiento (Android 11+)
     */
    fun requestManageExternalStoragePermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Log.d("PermissionUtils", "🔐 Solicitando permiso MANAGE_EXTERNAL_STORAGE")
                
                // Abrir configuración de permisos de la app
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${activity.packageName}")
                }
                
                // Si no se puede abrir la configuración específica, abrir la configuración general
                if (intent.resolveActivity(activity.packageManager) == null) {
                    val generalIntent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    if (generalIntent.resolveActivity(activity.packageManager) != null) {
                        activity.startActivityForResult(generalIntent, MANAGE_STORAGE_REQUEST_CODE)
                    } else {
                        // Fallback: abrir configuración de la app
                        val appSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${activity.packageName}")
                        }
                        activity.startActivityForResult(appSettingsIntent, MANAGE_STORAGE_REQUEST_CODE)
                    }
                } else {
                    activity.startActivityForResult(intent, MANAGE_STORAGE_REQUEST_CODE)
                }
            } catch (e: Exception) {
                Log.e("PermissionUtils", "❌ Error solicitando permisos: ${e.message}")
            }
        }
    }

    /**
     * NUEVA FUNCIÓN: Verificar si necesitamos solicitar permisos
     */
    fun needsPermissionRequest(context: Context): Boolean {
        return !hasStoragePermission(context as Activity)
    }

    /**
     * NUEVA FUNCIÓN: Obtener mensaje de explicación de permisos
     */
    fun getPermissionExplanationMessage(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            "Esta app necesita acceso a todos los archivos para leer los estados de WhatsApp. " +
            "Por favor, ve a Configuración > Aplicaciones > GuardaEstados > Permisos y activa 'Permitir gestión de todos los archivos'."
        } else {
            "Esta app necesita acceso al almacenamiento para leer los estados de WhatsApp."
        }
    }

    /**
     * NUEVA FUNCIÓN: Verificar permisos después de solicitud
     */
    fun onPermissionRequestResult(requestCode: Int, grantResults: IntArray, activity: Activity): Boolean {
        return when (requestCode) {
            STORAGE_PERMISSION_REQUEST_CODE -> {
                val granted = grantResults.isNotEmpty() && 
                             grantResults[0] == PackageManager.PERMISSION_GRANTED
                Log.d("PermissionUtils", "📋 Resultado permiso tradicional: $granted")
                granted
            }
            MANAGE_STORAGE_REQUEST_CODE -> {
                val granted = hasManageExternalStoragePermission(activity)
                Log.d("PermissionUtils", "📋 Resultado permiso administración: $granted")
                granted
            }
            else -> false
        }
    }
} 