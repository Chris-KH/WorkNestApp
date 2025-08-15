package com.apcs.worknestapp.data.remote.message

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor() : MessageRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _conservations = MutableStateFlow(emptyList<Conservation>())
    override val conservations: StateFlow<List<Conservation>> = _conservations.asStateFlow()

    private var conservationsListener: ListenerRegistration? = null

    init {
        auth.addAuthStateListener {
            val user = it.currentUser
            if (user == null) removeListener()
            else {
                removeListener()
                registerConservationListener()
            }
        }
    }

    override fun removeListener() {
        conservationsListener?.remove()
        conservationsListener = null
    }

    override fun registerConservationListener() {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        val conservationSnapshot = firestore
            .collection("conservations")
            .whereArrayContains("users", authUser.uid)
            .orderBy("lastTime", Query.Direction.DESCENDING)

        conservationsListener?.remove()
        conservationsListener = conservationSnapshot.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("MessageRepository", "Listen conservations snapshot failed", error)
                return@addSnapshotListener
            } else if (snapshot != null) {
                val conservationList = snapshot.documents.mapNotNull {
                    it.toObject(Conservation::class.java)
                }
                _conservations.value = conservationList
            }
        }
    }


    override suspend fun loadConservations() {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        val conservationSnapshot = firestore
            .collection("conservations")
            .whereArrayContains("users", authUser.uid)
            .orderBy("lastTime", Query.Direction.DESCENDING)
            .get()
            .await()

        val conservationList = conservationSnapshot.documents.mapNotNull {
            it.toObject(Conservation::class.java)
        }
        _conservations.value = conservationList
    }

    override fun clearCache() {
        _conservations.value = emptyList()
    }
}
