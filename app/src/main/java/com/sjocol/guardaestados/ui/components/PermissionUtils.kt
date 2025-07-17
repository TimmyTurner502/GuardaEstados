package com.sjocol.guardaestados.ui.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import android.os.Environment

object PermissionUtils {
    fun hasStoragePermission(activity: android.app.Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+: MANAGE_EXTERNAL_STORAGE
            val granted = Environment.isExternalStorageManager()
            Log.d("PermissionUtils", "MANAGE_EXTERNAL_STORAGE: $granted")
            granted
        } else {
            val granted = activity.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED
            Log.d("PermissionUtils", "READ_EXTERNAL_STORAGE: $granted")
            granted
        }
    }
    fun requestStoragePermission(activity: android.app.Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = android.net.Uri.parse("package:" + activity.packageName)
                activity.startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                activity.startActivity(intent)
            }
        } else {
            activity.requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 100)
        }
    }
} 