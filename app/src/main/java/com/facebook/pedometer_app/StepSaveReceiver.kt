package com.facebook.pedometer_app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StepSaveReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val listDataStore = ListDataStore.getInstance(context)
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        CoroutineScope(Dispatchers.IO).launch {
            val stepCount = getStepCountFromSharedPreferences(context)
            if (stepCount > 0) {
                val stepHistory = listDataStore.loadStepHistory()
                stepHistory.add(StepHistoryItem(currentDate, currentTime, stepCount))
                listDataStore.saveStepHistory(stepHistory)
                resetStepCountInSharedPreferences(context)
            }
        }
    }

    private fun getStepCountFromSharedPreferences(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("StepCounterPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("stepCount", 0)
    }

    private fun resetStepCountInSharedPreferences(context: Context) {
        val sharedPreferences = context.getSharedPreferences("StepCounterPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putInt("stepCount", 0)
            apply()
        }
    }
}
