package com.apcs.worknestapp.data.remote.user

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor() : UserRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _friends = MutableStateFlow(emptyList<User>())
    override val friends: StateFlow<List<User>> = _friends

    private val _foundUser = MutableStateFlow(emptyMap<String, User>())
    override val foundUser: StateFlow<Map<String, User>> = _foundUser

    override suspend fun getUser(docId: String): User {
        auth.currentUser ?: throw Exception("User not logged in")
        _foundUser.value[docId]?.let { return it }

        return refreshUser(docId)
    }

    override suspend fun refreshUser(docId: String): User {
        auth.currentUser ?: throw Exception("User not logged in")

        val userRef = firestore.collection("users")
        val snapshot = userRef.document(docId).get().await()
        val user = snapshot.toObject(User::class.java) ?: throw Exception("User not found")
        _foundUser.update { it + (docId to user) }

        return user
    }

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

    override fun clearCache() {
        _friends.value = emptyList()
        _foundUser.value = emptyMap()
    }
}
