package com.memorymoments.app.utils

import android.content.Context
import android.content.res.Configuration
import androidx.datastore.preferences.core.edit
import com.memorymoments.app.data.local.PreferenceKeys
import com.memorymoments.app.data.local.appDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

data class LanguageItem(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val localeTag: String
)

object AppLanguageManager {
    val SUPPORTED_LANGUAGES = listOf(
        LanguageItem("en", "English", "English", "en-IN"),
        LanguageItem("hi", "हिन्दी", "Hindi", "hi-IN"),
        LanguageItem("as", "অসমীয়া", "Assamese", "as-IN"),
        LanguageItem("bn", "বাংলা", "Bengali", "bn-IN"),
        LanguageItem("mni", "মৈতৈলোন / Manipuri", "Meitei / Manipuri", "mni-IN"),
        LanguageItem("kha", "Khasi", "Khasi", "kha-IN"),
        LanguageItem("lus", "Mizo", "Mizo", "lus-IN")
    )

    const val DEFAULT_LANGUAGE_CODE = "en"

    fun getLanguageItem(code: String?): LanguageItem {
        val clean = code?.trim()?.lowercase() ?: DEFAULT_LANGUAGE_CODE
        return SUPPORTED_LANGUAGES.find { it.code == clean }
            ?: SUPPORTED_LANGUAGES.find { it.englishName.equals(clean, ignoreCase = true) }
            ?: SUPPORTED_LANGUAGES.first()
    }

    fun getLanguageFlow(context: Context): Flow<String> {
        return context.appDataStore.data.map { prefs ->
            prefs[PreferenceKeys.APP_LANGUAGE] ?: DEFAULT_LANGUAGE_CODE
        }
    }

    suspend fun saveLanguage(context: Context, languageCode: String) {
        val cleanCode = getLanguageItem(languageCode).code
        context.appDataStore.edit { prefs ->
            prefs[PreferenceKeys.APP_LANGUAGE] = cleanCode
        }
        applyLocale(context, cleanCode)
    }

    fun applyLocale(context: Context, languageCode: String): Context {
        val item = getLanguageItem(languageCode)
        val locale = Locale.forLanguageTag(item.localeTag)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }
}
