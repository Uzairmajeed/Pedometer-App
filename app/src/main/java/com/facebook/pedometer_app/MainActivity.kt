package com.facebook.pedometer_app

import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var stepCount = 0
    private lateinit var stepCountText: TextView
    private lateinit var resetButton: Button
    private lateinit var nextButton: Button
    private lateinit var stepHistoryListView: ListView
    private lateinit var stepHistoryAdapter: StepHistoryAdapter
    private val stepHistory = mutableListOf<StepHistoryItem>()
    private lateinit var listDataStore: ListDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        stepCountText = findViewById(R.id.stepCountText)
        resetButton = findViewById(R.id.resetButton)
        nextButton = findViewById(R.id.MoveToNext)
        stepHistoryListView = findViewById(R.id.stepHistoryListView)

        val serviceIntent = Intent(this, StepCounterService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)

        listDataStore = ListDataStore.getInstance(this)

        // Load step history from DataStore
        stepHistory.addAll(listDataStore.loadStepHistory())
        stepHistoryAdapter = StepHistoryAdapter(this, stepHistory)
        stepHistoryListView.adapter = stepHistoryAdapter

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepCounterSensor == null) {
            Toast.makeText(this, "No Step Counter Sensor found!", Toast.LENGTH_SHORT).show()
            return
        }

        resetButton.setOnClickListener {
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            if (stepCount > 0) {
                stepHistory.add(StepHistoryItem(currentDate, currentTime, stepCount))
                stepHistoryAdapter.notifyDataSetChanged()
                listDataStore.saveStepHistory(stepHistory) // Save step history to DataStore
            }
            stepCount = 0
            updateStepCountText()
        }

        checkPermissions()

        nextButton.setOnClickListener {
            val intent = Intent(this@MainActivity, Accelometer::class.java)
            startActivity(intent)
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION), 1)
        } else {
            startStepCounter()
        }
    }

    private fun startStepCounter() {
        sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_FASTEST)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            stepCount++
            updateStepCountText()
        }
    }

    private fun updateStepCountText() {
        stepCountText.text = "Steps Count: $stepCount"
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startStepCounter()
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        if (stepCounterSensor != null) {
            startStepCounter()
        }
    }
}
