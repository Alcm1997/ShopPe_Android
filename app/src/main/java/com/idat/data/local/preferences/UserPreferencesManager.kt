package com.idat.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesManager @Inject constructor(
    private val context: Context
) {
    companion object {
        val THEME_KEY = booleanPreferencesKey("is_dark_theme")
        val VIEW_MODE_KEY = stringPreferencesKey("view_mode") // "grid" o "list"
        val USER_NAME_KEY = stringPreferencesKey("user_name")
        val USER_PHOTO_KEY = stringPreferencesKey("user_photo")
        
        // Nuevas llaves para Recuérdame
        val REMEMBER_ME_KEY = booleanPreferencesKey("remember_me")
        val SAVED_EMAIL_KEY = stringPreferencesKey("saved_email")
        val SAVED_PASSWORD_KEY = stringPreferencesKey("saved_password")
    }

    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { it[THEME_KEY] ?: false }
    val viewMode: Flow<String> = context.dataStore.data.map { it[VIEW_MODE_KEY] ?: "grid" }
    
    val rememberMe: Flow<Boolean> = context.dataStore.data.map { it[REMEMBER_ME_KEY] ?: false }
    val savedEmail: Flow<String> = context.dataStore.data.map { it[SAVED_EMAIL_KEY] ?: "" }
    val savedPassword: Flow<String> = context.dataStore.data.map { it[SAVED_PASSWORD_KEY] ?: "" }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { it[THEME_KEY] = isDark }
    }

    suspend fun setViewMode(mode: String) {
        context.dataStore.edit { it[VIEW_MODE_KEY] = mode }
    }

    suspend fun setRememberMe(remember: Boolean, email: String = "", password: String = "") {
        context.dataStore.edit { preferences ->
            preferences[REMEMBER_ME_KEY] = remember
            if (remember) {
                preferences[SAVED_EMAIL_KEY] = email
                preferences[SAVED_PASSWORD_KEY] = password
            } else {
                preferences[SAVED_EMAIL_KEY] = ""
                preferences[SAVED_PASSWORD_KEY] = ""
            }
        }
    }
}
