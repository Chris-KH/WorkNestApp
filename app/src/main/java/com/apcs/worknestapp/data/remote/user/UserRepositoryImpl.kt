package com.apcs.worknestapp.data.remote.user

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor() : UserRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _friends = MutableStateFlow(emptyList<User>())
    override val friends: StateFlow<List<User>> = _friends

    override suspend fun findUsers(searchValue: String): List<User> {
        auth.currentUser ?: throw Exception("User not logged in")

        val userRef = firestore.collection("users")
        val snapshot = userRef.whereGreaterThanOrEqualTo("email", searchValue)
            .whereLessThan("email", searchValue + "\uF8FF")
            .orderBy("email")
            .get()
            .await()

        return snapshot.documents.mapNotNull {
            it.toObject(User::class.java)
        }
    }
}
