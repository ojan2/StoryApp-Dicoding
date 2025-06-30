package com.application.storyapp.data.data_store

import android.annotation.SuppressLint
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferences private constructor(context: Context) {

    private val dataStore: DataStore<Preferences> = context.dataStore

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: UserPreferences? = null

        @SuppressLint("StaticFieldLeak")
        fun getInstance(context: Context): UserPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    suspend fun saveAuthToken(token: String, userId: String, userName: String) {
        dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USER_ID_KEY] = userId
            prefs[USER_NAME_KEY] = userName
        }
    }

    fun getAuthToken(): Flow<String?> = dataStore.data.map { it[TOKEN_KEY] }

    fun isLoggedIn(): Flow<Boolean> = dataStore.data.map { it[TOKEN_KEY] != null }

    suspend fun clearAuthData() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
