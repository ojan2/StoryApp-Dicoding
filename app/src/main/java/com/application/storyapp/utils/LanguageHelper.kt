package com.application.storyapp.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import java.util.Locale

object LanguageHelper {

    private const val PREF_LANGUAGE = "app_language"
    private const val DEFAULT_LANGUAGE = "en" // Bahasa default

    // Simpan bahasa terpilih di SharedPreferences
    fun setAppLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_LANGUAGE, languageCode).apply()
    }

    // Dapatkan bahasa yang disimpan
    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        return prefs.getString(PREF_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }

    // Update konfigurasi bahasa aplikasi
    fun applyLanguage(context: Context): Context {
        val language = getSavedLanguage(context)
        return updateResources(context, language)
    }

    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources: Resources = context.resources
        val config: Configuration = resources.configuration

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale)
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
            resources.updateConfiguration(config, resources.displayMetrics)
            context
        }
    }
}