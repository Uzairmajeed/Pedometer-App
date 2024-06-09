package com.facebook.pedometer_app

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class ListDataStore(private val context: Context) {

    private val Context.dataStore by preferencesDataStore("step_history_prefs")

    companion object {
        @Volatile
        private var INSTANCE: ListDataStore? = null

        fun getInstance(context: Context): ListDataStore {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ListDataStore(context).also { INSTANCE = it }
            }
        }
    }

    fun saveStepHistory(stepHistory: List<StepHistoryItem>) {
        val gson = Gson()
        val json = gson.toJson(stepHistory)
        val dataStoreKey = stringPreferencesKey("stepHistoryList")
        runBlocking {
            context.dataStore.edit { preferences ->
                preferences[dataStoreKey] = json
            }
        }
    }

    fun loadStepHistory(): MutableList<StepHistoryItem> {
        val dataStoreKey = stringPreferencesKey("stepHistoryList")
        val gson = Gson()
        val json: String? = runBlocking {
            val preferences = context.dataStore.data.first()
            preferences[dataStoreKey]
        }
        return if (json != null) {
            val type = object : TypeToken<MutableList<StepHistoryItem>>() {}.type
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }
    }
}
