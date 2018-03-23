package com.simprints.neuroprintprototype.activities.recording

import android.util.Log
import com.simprints.neuroprintprototype.data.recordings.RecordingRepository
import com.simprints.neuroprintprototype.data.users.User
import com.simprints.neuroprintprototype.data.users.UserRepository
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

class RecordingPresenter(private val view: RecordingContract.View,
                         private val recordingRepository: RecordingRepository,
                         private val userRepository: UserRepository,
                         private val sensorRecorder: SensorRecorder): RecordingContract.Presenter {

    companion object {

        private const val RECORDING_LENGTH_MILLIS = 5000L

        private const val NUM_PROGRESS_UPDATES = 100

    }

    init {
        launch {
            try {
                val users = userRepository.getUsers()
                launch(UI) {
                    view.updateUserList(users)
                }.join()
            } catch (exception: Exception) {
                Log.e("NeuroPrintPrototype", "error while getting user list", exception)
            }
        }
    }

    override fun onStart() {
    }

    override fun startRecording(user: User) {
        launch {
            launch(UI) { view.startRecordingUI() }
            sensorRecorder.startRecording()
            for (progress in (0 until NUM_PROGRESS_UPDATES)) {
                launch(UI) { view.updateRecordingUI(progress, NUM_PROGRESS_UPDATES)}
                delay(RECORDING_LENGTH_MILLIS / NUM_PROGRESS_UPDATES)
            }
            val recording = sensorRecorder.finishRecording()
            recordingRepository.save(recording.copy(user = user.name))
            launch(UI) { view.stopRecordingUI() }
        }
    }

    override fun addUser(user: User) {
        launch {
            userRepository.saveUser(user)
        }
    }

    override fun onStop() {
        sensorRecorder.cancelRecording()
        view.stopRecordingUI()
    }
}
