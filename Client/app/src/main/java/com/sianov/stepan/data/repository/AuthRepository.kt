package com.sianov.stepan.data.repository

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.sianov.stepan.data.model.User
import com.sianov.stepan.data.remote.ApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authDataStore by preferencesDataStore("auth")

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService
) {
    private val isLoggedInKey = booleanPreferencesKey("is_logged_in")
    private val tokenKey = stringPreferencesKey("auth_token")
    private val userEmailKey = stringPreferencesKey("user_email")
    private val userNameKey = stringPreferencesKey("user_name")

    val isLoggedIn: Flow<Boolean> = context.authDataStore.data.map { it[isLoggedInKey] ?: false }
    val authToken: Flow<String?> = context.authDataStore.data.map { it[tokenKey] }
    
    // Текущее имя пользователя для формирования ключей
    val currentUsername: Flow<String?> = context.authDataStore.data.map { it[userNameKey] }

    // Динамические ключи на основе пользователя
    private fun getFavoritesKey(username: String) = stringSetPreferencesKey("favorites_$username")
    private fun getRemindersKey(username: String) = stringSetPreferencesKey("reminders_$username")
    private fun getVisitedKey(username: String) = stringSetPreferencesKey("visited_$username")

    // Получаем списки, привязанные к текущему пользователю
    val favorites: Flow<Set<String>> = currentUsername.flatMapLatest { username ->
        if (username == null) flowOf(emptySet())
        else context.authDataStore.data.map { it[getFavoritesKey(username)] ?: emptySet() }
    }

    val reminders: Flow<Set<String>> = currentUsername.flatMapLatest { username ->
        if (username == null) flowOf(emptySet())
        else context.authDataStore.data.map { it[getRemindersKey(username)] ?: emptySet() }
    }

    val visited: Flow<Set<String>> = currentUsername.flatMapLatest { username ->
        if (username == null) flowOf(emptySet())
        else context.authDataStore.data.map { it[getVisitedKey(username)] ?: emptySet() }
    }

    val user: Flow<User?> = context.authDataStore.data.map { preferences ->
        val email = preferences[userEmailKey]
        val name = preferences[userNameKey]
        if (email != null && name != null) {
            User(email, name)
        } else null
    }

    suspend fun login(username: String, email: String, password: String): Boolean {
        return try {
            val response = apiService.login(mapOf(
                "username" to username,
                "password" to password
            ))
            saveAuth(response.token, response.user.username, response.user.email)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun register(username: String, email: String, password: String): Boolean {
        return try {
            val response = apiService.register(mapOf(
                "username" to username,
                "email" to email,
                "password" to password
            ))
            saveAuth(response.token, response.user.username, response.user.email)
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun saveAuth(token: String, name: String, email: String) {
        context.authDataStore.edit { preferences ->
            preferences[isLoggedInKey] = true
            preferences[tokenKey] = token
            preferences[userEmailKey] = email
            preferences[userNameKey] = name
        }
    }

    suspend fun logout() {
        context.authDataStore.edit { preferences ->
            preferences[isLoggedInKey] = false
            preferences.remove(tokenKey)
            preferences.remove(userEmailKey)
            preferences.remove(userNameKey)
        }
    }

    suspend fun toggleFavorite(performanceUrl: String) {
        val username = getUserNameSync() ?: return
        context.authDataStore.edit { prefs ->
            val key = getFavoritesKey(username)
            val current = prefs[key] ?: emptySet()
            prefs[key] = if (current.contains(performanceUrl)) current - performanceUrl else current + performanceUrl
        }
    }

    suspend fun toggleReminder(performanceUrl: String) {
        val username = getUserNameSync() ?: return
        context.authDataStore.edit { prefs ->
            val key = getRemindersKey(username)
            val current = prefs[key] ?: emptySet()
            prefs[key] = if (current.contains(performanceUrl)) current - performanceUrl else current + performanceUrl
        }
    }

    suspend fun toggleVisited(performanceUrl: String) {
        val username = getUserNameSync() ?: return
        context.authDataStore.edit { prefs ->
            val key = getVisitedKey(username)
            val current = prefs[key] ?: emptySet()
            prefs[key] = if (current.contains(performanceUrl)) current - performanceUrl else current + performanceUrl
        }
    }

    private suspend fun getUserNameSync(): String? {
        return try {
            context.authDataStore.data.first()[userNameKey]
        } catch (e: Exception) { null }
    }
}
