package com.sjocol.guardaestados

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sjocol.guardaestados.ui.theme.ThemeType
import com.sjocol.guardaestados.ui.theme.ThemeMode
import java.util.Locale
import android.content.Context
import android.content.SharedPreferences
import com.sjocol.guardaestados.ui.components.WhatsAppInstance

class AppState(private val context: Context? = null) {
    var themeType by mutableStateOf(loadThemeType())
    var themeMode by mutableStateOf(loadThemeMode())
    var previewBarTranslucent by mutableStateOf(true)
    var idiomaKey by mutableStateOf(0) // NUEVO: key para recomposición global
    var locale: Locale
        get() = _locale
        set(value) {
            _locale = value
            saveLocale(value)
            idiomaKey++ // Forzar recomposición global
        }
    private var _locale by mutableStateOf(loadLocale())
    var downloadFolder by mutableStateOf(loadDownloadFolder())
    var selectedInstance by mutableStateOf<WhatsAppInstance?>(loadSelectedInstance())
    
    // Lista de archivos bloqueados
    private var _lockedFiles by mutableStateOf(loadLockedFiles())
    val lockedFiles: Set<String> get() = _lockedFiles
    
    // Control de anuncio por recompensa visto en la sesión
    var rewardedAdWatchedThisSession by mutableStateOf(false)
    
    fun toggleFileLock(filePath: String) {
        val newLockedFiles = _lockedFiles.toMutableSet()
        if (filePath in newLockedFiles) {
            newLockedFiles.remove(filePath)
        } else {
            newLockedFiles.add(filePath)
        }
        _lockedFiles = newLockedFiles
        saveLockedFiles(newLockedFiles)
    }
    
    fun isFileLocked(filePath: String): Boolean {
        return filePath in _lockedFiles
    }
    
    fun updateDownloadFolder(path: String) {
        downloadFolder = path
        prefs()?.edit()?.putString("download_folder", path)?.apply()
    }
    fun updateThemeMode(mode: ThemeMode) {
        themeMode = mode
        prefs()?.edit()?.putString("theme_mode", mode.name)?.apply()
    }
    fun updateSelectedInstance(instance: WhatsAppInstance?) {
        selectedInstance = instance
        prefs()?.edit()?.putString("selected_instance_name", instance?.name)
            ?.putString("selected_instance_path", instance?.path)
            ?.apply()
    }
    fun updateThemeType(type: ThemeType) {
        themeType = type
        prefs()?.edit()?.putString("theme_type", type.name)?.apply()
    }
    fun resetLocaleToSystem() {
        _locale = Locale.getDefault()
        prefs()?.edit()?.remove("locale_lang")?.remove("locale_country")?.apply()
        idiomaKey++
    }
    private fun prefs(): SharedPreferences? = context?.getSharedPreferences("guardaestados_prefs", Context.MODE_PRIVATE)
    private fun saveLocale(locale: Locale) {
        prefs()?.edit()?.putString("locale_lang", locale.language)?.putString("locale_country", locale.country)?.apply()
    }
    private fun loadLocale(): Locale {
        val lang = prefs()?.getString("locale_lang", null)
        val country = prefs()?.getString("locale_country", null)
        return if (lang != null) Locale(lang, country ?: "") else Locale.getDefault()
    }
    private fun loadDownloadFolder(): String {
        return prefs()?.getString("download_folder", null)
            ?: "${android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS).absolutePath}/StatusWtsUp"
    }
    private fun loadSelectedInstance(): WhatsAppInstance? {
        val name = prefs()?.getString("selected_instance_name", null)
        val path = prefs()?.getString("selected_instance_path", null)
        return if (name != null && path != null) WhatsAppInstance(name, path) else null
    }
    private fun loadThemeMode(): ThemeMode {
        val value = prefs()?.getString("theme_mode", null)
        return value?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM
    }
    private fun loadThemeType(): ThemeType {
        val value = prefs()?.getString("theme_type", null)
        return value?.let { runCatching { ThemeType.valueOf(it) }.getOrNull() } ?: ThemeType.DEFAULT
    }
    
    private fun loadLockedFiles(): Set<String> {
        return prefs()?.getStringSet("locked_files", emptySet()) ?: emptySet()
    }
    
    private fun saveLockedFiles(files: Set<String>) {
        prefs()?.edit()?.putStringSet("locked_files", files)?.apply()
    }
} 