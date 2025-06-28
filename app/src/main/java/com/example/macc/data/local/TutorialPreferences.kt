package com.example.macc.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "tutorial_prefs"

val Context.tutorialDataStore by preferencesDataStore(name = DATASTORE_NAME)

object TutorialPreferences {

    fun tooltipShownFlow(context: Context, key: String): Flow<Boolean> {
        val preferenceKey = booleanPreferencesKey(key)
        return context.tutorialDataStore.data.map { prefs ->
            prefs[preferenceKey] ?: false
        }
    }

    suspend fun setTooltipShown(context: Context, key: String) {
        val preferenceKey = booleanPreferencesKey(key)
        context.tutorialDataStore.edit { prefs ->
            prefs[preferenceKey] = true
        }
    }
}
