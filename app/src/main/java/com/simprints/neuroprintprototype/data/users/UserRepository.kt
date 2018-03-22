package com.simprints.neuroprintprototype.data.users

import com.google.firebase.firestore.FirebaseFirestore
import com.simprints.neuroprintprototype.data.await

class UserRepository(firestore: FirebaseFirestore,
                     collectionPath: String = USERS_COLLECTION) {

    companion object {
        private const val USERS_COLLECTION = "NeuroPrintPrototype/NeuroPrintPrototype/users"

        private const val NAME_FIELD = "name"
    }

    private val usersCollection = firestore.collection(collectionPath)

    suspend fun getUsers(): List<User> =
        usersCollection
            .orderBy(NAME_FIELD)
            .get()
            .await()
            .toObjects(User::class.java)

    suspend fun saveUser(user: User) {
        usersCollection
            .add(user)
            .await()
    }

}
