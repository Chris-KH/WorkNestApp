package com.apcs.worknestapp.data.remote.message

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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

    private suspend fun Conservation.withOtherUserData(otherUserId: String): Conservation {
        return try {
            val userDoc = firestore.collection("users")
                .document(otherUserId)
                .get()
                .await()

            copy(
                userData = ConservationUserData(
                    name = userDoc.getString("name"),
                    avatar = userDoc.getString("avatar"),
                    online = userDoc.getBoolean("online"),
                )
            )
        } catch(_: Exception) {
            copy(
                userData = ConservationUserData(
                    name = "Unknown",
                    avatar = null,
                    online = false,
                )
            )
        }
    }

    override fun removeListener() {
        conservationsListener?.remove()
        conservationsListener = null
    }

    override fun registerConservationListener() {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        val conservationSnapshot = firestore.collection("conservations")
            .whereArrayContains("userIds", authUser.uid)
            .orderBy("lastTime", Query.Direction.DESCENDING)

        conservationsListener?.remove()
        conservationsListener = conservationSnapshot.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("MessageRepository", "Listen conservations snapshot failed", error)
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener

            CoroutineScope(Dispatchers.IO).launch {
                val conservationList = coroutineScope {
                    val authUser = auth.currentUser ?: return@coroutineScope emptyList()
                    snapshot.documents.mapNotNull { doc ->
                        val cons = doc.toObject(Conservation::class.java)
                        val otherUserId = cons?.userIds?.firstOrNull { it != authUser.uid }
                            ?: return@mapNotNull null

                        async { cons.withOtherUserData(otherUserId) }
                    }.awaitAll()
                }

                withContext(Dispatchers.Main) {
                    _conservations.value = conservationList
                }
            }
        }
    }

    override suspend fun loadConservations() {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        val conservationSnapshot = firestore
            .collection("conservations")
            .whereArrayContains("userIds", authUser.uid)
            .orderBy("lastTime", Query.Direction.DESCENDING)
            .get()
            .await()

        val conservationList = coroutineScope {
            conservationSnapshot.documents.mapNotNull { doc ->
                val cons = doc.toObject(Conservation::class.java)
                val otherUserId = cons?.userIds?.firstOrNull { it != authUser.uid }
                    ?: return@mapNotNull null

                async { cons.withOtherUserData(otherUserId) }
            }.awaitAll()
        }

        _conservations.value = conservationList
    }

    override suspend fun updateConservationSeen(docId: String, state: Boolean) {
        auth.currentUser ?: throw Exception("User not logged in")
        if (_conservations.value.find { it.docId == docId } == null)
            throw Exception("Conservation not found")

        firestore.collection("conservations").document(docId).update("seen", state).await()
        _conservations.update { list ->
            list.map { if (it.docId == docId) it.copy(seen = state) else it }
        }
    }

    override fun clearCache() {
        removeListener()
        _conservations.value = emptyList()
    }
}
