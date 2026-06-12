package com.verdy.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "verdy_prefs")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME_DYNAMIC_COLOR = booleanPreferencesKey("theme_dynamic_color")
        val NOTIFICATION_HOUR = stringPreferencesKey("notification_hour")
        val LAST_EXPORT_DATE = stringPreferencesKey("last_export_date")
        val LAST_BACKUP_DATE = stringPreferencesKey("last_backup_date")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val useDynamicColor: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.THEME_DYNAMIC_COLOR] ?: true }

    val notificationHour: Flow<String> = context.dataStore.data
        .map { it[Keys.NOTIFICATION_HOUR] ?: "08:00" }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.ONBOARDING_COMPLETED] ?: false }

    val lastExportDate: Flow<String?> = context.dataStore.data
        .map { it[Keys.LAST_EXPORT_DATE] }

    val lastBackupDate: Flow<String?> = context.dataStore.data
        .map { it[Keys.LAST_BACKUP_DATE] }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[Keys.THEME_DYNAMIC_COLOR] = enabled }
    }

    suspend fun setNotificationHour(hour: String) {
        context.dataStore.edit { it[Keys.NOTIFICATION_HOUR] = hour }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setLastExportDate(date: String) {
        context.dataStore.edit { it[Keys.LAST_EXPORT_DATE] = date }
    }

    suspend fun setLastBackupDate(date: String) {
        context.dataStore.edit { it[Keys.LAST_BACKUP_DATE] = date }
    }
}
