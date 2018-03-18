package com.simprints.neuroprintprototype

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings



class App : Application() {

    val firestoreApp: FirebaseApp by lazy {
        FirebaseApp.initializeApp(this) ?: throw RuntimeException()

    }

    val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance(firestoreApp)
            .also {
                it.firestoreSettings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(false)
                    .build()
            }
    }



}
