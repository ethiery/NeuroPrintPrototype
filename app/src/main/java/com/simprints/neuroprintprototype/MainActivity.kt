package com.simprints.neuroprintprototype

import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ArrayAdapter
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.sensorManager
import org.jetbrains.anko.toast
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.experimental.suspendCoroutine


@SuppressLint("LogNotTimber")
class MainActivity : AppCompatActivity(), SensorEventListener {
    companion object {

        private const val RECORDINGS_COLLECTION = "recordings"

        private val USERS = listOf(
            "Etienne",
            "Tristram"
        )

    }

    private val app: App by lazy { application as App }

    private val firestore: FirebaseFirestore by lazy { app.firestore }
    private val rootReference: DocumentReference by lazy {
        firestore.collection("NeuroPrintPrototype").document("NeuroPrintPrototype")
    }
    private val recordingsCollection: CollectionReference by lazy {
        rootReference.collection(RECORDINGS_COLLECTION)
    }

    private lateinit var linearAccelerationMeter: Sensor
    private val recording = AtomicBoolean(false)
    private val currentRecording = mutableListOf<Pair<Long, FloatArray>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startButton.setOnClickListener { start() }
        stopButton.setOnClickListener { stop() }
        initializeSpinner(USERS)

        linearAccelerationMeter = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    }

    private fun initializeSpinner(users: List<String>) {
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, users)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun start() {
        if (recording.getAndSet(true)) {
            toast("Already recording")
        } else {
            val success = sensorManager.registerListener(this, linearAccelerationMeter, SensorManager.SENSOR_DELAY_FASTEST)
            if (success) {
                toast("Started recording")
            } else {
                toast("Could not start recording")
            }
        }
    }

    private fun stop() {
        if (recording.getAndSet(false)) {
            sensorManager.unregisterListener(this)
            saveCurrentRecording()
            clearCurrentRecording()
        } else {
            toast("Already stopped")
        }
    }

    private fun saveCurrentRecording() {
        val user = spinner.selectedItem.toString()
        val csvData = currentRecording.joinToString("\n") { (timestamp, values) ->
            "$timestamp,${values.joinToString(",")}"
        }
        val recording = Recording(user, csvData)
        launch {
            val ref = recordingsCollection.document()
            ref.set(recording).await()
            Log.d("NeuroPrintPrototype", "Saved recording ${ref.id}")
        }
    }

    private fun clearCurrentRecording() {
        currentRecording.clear()
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        Log.d("SimRemote", "onAccuracyChanged(${sensor.name}, $accuracy)")
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor == linearAccelerationMeter) {
            currentRecording.add(event.timestamp to event.values.copyOf())
        }
    }

    private suspend fun <T> Task<T>.await(): T = suspendCoroutine { continuation ->

        addOnCompleteListener { task ->
            continuation.resume(task.result)
        }
        addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
    }

}
