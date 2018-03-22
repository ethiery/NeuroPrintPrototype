package com.simprints.neuroprintprototype.activities.recording

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.simprints.neuroprintprototype.data.recordings.Recording
import java.util.concurrent.atomic.AtomicBoolean

class SensorRecorder(private val sensorManager: SensorManager,
                     sensorType: Int) {

    private val sensor = sensorManager.getDefaultSensor(sensorType)

    private val isRecording = AtomicBoolean(false)
    private val isFinishing = AtomicBoolean(false)
    private val currentRecording = mutableListOf<Pair<Long, FloatArray>>()

    private val sensorEventListener = object: SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            if (sensor == this@SensorRecorder.sensor) {
                Log.d("SimRemote", "onAccuracyChanged(${sensor.name}, $accuracy)")
            }
        }

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor == this@SensorRecorder.sensor) {
                currentRecording.add(event.timestamp to event.values.copyOf())
            }
        }
    }

    fun startRecording() {
        check(!isRecording.getAndSet(true))
        val success = sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_FASTEST)
        if (success) {
            Log.d("SimRemote", "Started recording sensor ${sensor.name}.")
        } else {
            Log.d("SimRemote", "Could not start recording sensor ${sensor.name}.")
            throw IllegalStateException()
        }
    }

    fun finishRecording(): Recording {
        check(isRecording.get())
        check(!isFinishing.getAndSet(true))
        sensorManager.unregisterListener(sensorEventListener)
        Log.d("SimRemote", "Finished recording sensor ${sensor.name}. " +
            "${getNumSamples()} samples over ${getDurationMillis()} ms")
        val csvData = currentRecording.joinToString("\n") { (timestamp, values) ->
            "$timestamp,${values.joinToString(",")}"
        }
        currentRecording.clear()
        isRecording.set(false)
        isFinishing.set(false)
        return Recording(csvData = csvData)
    }

    fun cancelRecording() {
        if (isRecording.getAndSet(false)) {
            sensorManager.unregisterListener(sensorEventListener)
            Log.d("SimRemote", "Cancelled recording sensor ${sensor.name}.")
        }
    }

    private fun getNumSamples(): Int =
        currentRecording.size

    private fun getDurationMillis(): Long =
        if (currentRecording.size == 0) {
            0L
        } else {
            currentRecording.last().first - currentRecording.first().first
        }

}
