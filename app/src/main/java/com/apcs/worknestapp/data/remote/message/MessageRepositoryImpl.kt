package com.apcs.worknestapp.data.remote.message

import android.util.Log
import com.apcs.worknestapp.domain.usecase.AppDefault
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

    private var conservationsListener: ListenerRegistration? = null
    private val userCache = mutableMapOf<String, ConservationUserData>()
    private val userListeners = mutableMapOf<String, ListenerRegistration>()

    private val _conservations = MutableStateFlow(emptyList<Conservation>())
    override val conservations: StateFlow<List<Conservation>> = _conservations.asStateFlow()

    private val _currentConservation = MutableStateFlow<Conservation?>(null)
    override val currentConservation: StateFlow<Conservation?> = _currentConservation.asStateFlow()

    init {
        auth.addAuthStateListener {
            val user = it.currentUser
            if (user == null) clearCache()
        }
    }

    private fun observeUser(otherUserId: String) {
        if (userListeners.containsKey(otherUserId)) return

        val listener = firestore.collection("users")
            .document(otherUserId)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    val userData = ConservationUserData(
                        docId = it.id,
                        name = it.getString("name") ?: AppDefault.USER_NAME,
                        avatar = it.getString("avatar") ?: AppDefault.AVATAR,
                        online = it.getBoolean("online") ?: false,
                    )
                    userCache[otherUserId] = userData

                    _conservations.update { list ->
                        list.map { cons ->
                            if (cons.userIds?.contains(otherUserId) == true)
                                cons.copy(userData = userData)
                            else cons
                        }
                    }
                }
            }

        userListeners[otherUserId] = listener
    }

    private suspend fun Conservation.withOtherUserData(
        otherUserId: String,
        isCache: Boolean = false,
    ): Conservation {
        if (isCache) {
            val cached = userCache[otherUserId]
            if (cached != null) {
                observeUser(otherUserId)
                return copy(userData = cached)
            }
        }

        return try {
            val userDoc = firestore.collection("users")
                .document(otherUserId)
                .get()
                .await()

            val userData = ConservationUserData(
                docId = userDoc.id,
                name = userDoc.getString("name") ?: AppDefault.USER_NAME,
                avatar = userDoc.getString("avatar") ?: AppDefault.AVATAR,
                online = userDoc.getBoolean("online") ?: false,
            )

            userCache[otherUserId] = userData
            observeUser(otherUserId)

            copy(userData = userData)
        } catch(_: Exception) {
            copy(userData = ConservationUserData())
        }
    }

    override fun removeListener() {
        conservationsListener?.remove()
        conservationsListener = null

        userListeners.forEach { it.value.remove() }
        userListeners.clear()
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

                        async { cons.withOtherUserData(otherUserId, isCache = true) }
                    }.awaitAll()
                }

                withContext(Dispatchers.Main) {
                    _conservations.value = conservationList
                }
            }
        }
    }

    override fun getCacheConservation(docId: String?) {
        auth.currentUser ?: throw Exception("User not logged in")
        if (docId == null) {
            _currentConservation.value = null
        } else {
            val conservation = _conservations.value.find { it.docId == docId }
                ?: throw Exception("This conservation does not exist")

            _currentConservation.value = conservation
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
        userCache.clear()
        userListeners.forEach { it.value.remove() }
        userListeners.clear()
        _conservations.value = emptyList()

    }
}
