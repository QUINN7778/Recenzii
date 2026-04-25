package com.sianov.stepan.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val darkThemeKey = booleanPreferencesKey("dark_theme")
    private val fontSizeKey = floatPreferencesKey("font_size")
    private val themeColorKey = intPreferencesKey("theme_color")
    private val seedColorKey = androidx.datastore.preferences.core.longPreferencesKey("seed_color")

    val isDarkTheme: Flow<Boolean?> = context.dataStore.data.map { it[darkThemeKey] }
    val fontSizeMultiplier: Flow<Float> = context.dataStore.data.map { it[fontSizeKey] ?: 1.0f }
    val themeColorIndex: Flow<Int> = context.dataStore.data.map { it[themeColorKey] ?: 0 }
    val seedColor: Flow<Long> = context.dataStore.data.map { it[seedColorKey] ?: 0xFFB71C1C }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { it[darkThemeKey] = isDark }
    }

    suspend fun setFontSize(size: Float) {
        context.dataStore.edit { it[fontSizeKey] = size }
    }

    suspend fun setThemeColor(index: Int) {
        context.dataStore.edit { it[themeColorKey] = index }
    }

    suspend fun setSeedColor(color: Long) {
        context.dataStore.edit { it[seedColorKey] = color }
    }
}
