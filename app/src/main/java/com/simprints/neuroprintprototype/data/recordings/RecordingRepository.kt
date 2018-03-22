package com.simprints.neuroprintprototype.data.recordings

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.simprints.neuroprintprototype.data.await

class RecordingRepository(firestore: FirebaseFirestore,
                          collectionPath: String = RECORDINGS_COLLECTION) {

    companion object {
        private const val RECORDINGS_COLLECTION = "NeuroPrintPrototype/NeuroPrintPrototype/recordings"
    }

    private val recordingsCollection = firestore.collection(collectionPath)

    suspend fun save(recording: Recording) {
        Log.d("NeuroPrintPrototype", "Saving recording...")
        recordingsCollection
            .add(recording)
            .await()
            .let {
                Log.d("NeuroPrintPrototype", "Saved recording ${it.id}.")
            }
    }

}
