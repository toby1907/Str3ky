package com.example.str3ky.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SettingsRepository(private val dataStore: DataStore<Preferences>) {
    val prefs: Flow<Preferences> = dataStore.data
    private val scope = CoroutineScope(Dispatchers.IO)
    init {
        scope.launch {
            dataStore.edit { pref ->
                // Set the default value to false only if it hasn't been set before
                pref[FIRST_ENTRY_KEY] = pref[FIRST_ENTRY_KEY] ?: false
            }
        }
    }
    companion object {
        val FIRST_ENTRY_KEY = booleanPreferencesKey("first_entry")
    }



    suspend fun checkFirstEntryStatus(): Boolean? {
        return dataStore.data.firstOrNull()?.get(FIRST_ENTRY_KEY)
    }
     fun updateEntryStatus() {
        scope.launch{
            dataStore.edit { pref ->
                pref[FIRST_ENTRY_KEY] = true
            }
        }
    }


}