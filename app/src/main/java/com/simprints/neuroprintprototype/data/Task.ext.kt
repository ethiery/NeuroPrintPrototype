package com.simprints.neuroprintprototype.data

import com.google.android.gms.tasks.Task
import kotlin.coroutines.experimental.suspendCoroutine

internal suspend fun <T> Task<T>.await(): T = suspendCoroutine { continuation ->
    addOnCompleteListener { task ->
        continuation.resume(task.result)
    }
    addOnFailureListener { exception ->
        continuation.resumeWithException(exception)
    }
}

