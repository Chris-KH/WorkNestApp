package com.apcs.worknestapp.data.remote.message

import android.util.Log
import com.apcs.worknestapp.domain.usecase.AppDefault
import com.google.firebase.Timestamp
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

    private val messageCache = mutableMapOf<String, List<Message>>()
    private val messageListeners = mutableMapOf<String, ListenerRegistration>()

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
                snapshot?.let { userDoc ->
                    val userData = ConservationUserData(
                        docId = userDoc.id,
                        name = userDoc.getString("name") ?: AppDefault.USER_NAME,
                        avatar = userDoc.getString("avatar") ?: AppDefault.AVATAR,
                        online = userDoc.getBoolean("online") ?: false,
                    )
                    userCache[otherUserId] = userData
                    _conservations.update { list ->
                        list.map { cons ->
                            if (cons.userIds?.contains(otherUserId) == true)
                                cons.copy(userData = userData)
                            else cons
                        }
                    }

                    if (_currentConservation.value?.userData?.docId == userData.docId) {
                        _currentConservation.update { it?.copy(userData = userData) }
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

    override fun getConservation(docId: String?) {
        auth.currentUser ?: throw Exception("User not logged in")
        if (docId == null) {
            messageListeners[_currentConservation.value?.docId]?.remove()
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
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        val conservationRef = firestore.collection("conservations").document(docId)

        firestore.runTransaction { transaction ->
            val conservationSnapshot = transaction.get(conservationRef)
            if (!conservationSnapshot.exists()) throw Exception("Conservation not found")

            val conservation = conservationSnapshot.toObject(Conservation::class.java)
                ?: throw Exception("Invalid conservation data")
            val isSender = conservation.sender?.id == authUser.uid
            if (isSender) transaction.update(conservationRef, mapOf("senderSeen" to state))
            else transaction.update(conservationRef, mapOf("receiverSeen" to state))

            _conservations.update { list ->
                list.map {
                    if (it.docId == docId) {
                        if (isSender) it.copy(senderSeen = state)
                        else it.copy(receiverSeen = state)
                    } else it
                }
            }
        }
    }

    override suspend fun loadNewMessages(conservationId: String) {
        auth.currentUser ?: throw Exception("User not logged in")
        val conservation = _conservations.value.find { it.docId == conservationId }
            ?: throw Exception("Conservation not found")
        val currentMessages = messageCache[conservationId] ?: emptyList()
        val latestCreatedAt =
            currentMessages.maxByOrNull { it.createdAt ?: Timestamp(0, 0) }?.createdAt

        val snapshot = firestore.collection("conservations").document(conservationId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .let { if (latestCreatedAt != null) it.startAfter(latestCreatedAt) else it }
            .get()
            .await()

        val newMessages = snapshot.documents.mapNotNull {
            it.toObject(Message::class.java)
        }.reversed()

        val merged = newMessages + currentMessages

        messageCache[conservationId] = merged
        _conservations.update { list ->
            list.map { if (it.docId == conservationId) it.copy(messages = merged) else it }
        }
        _currentConservation.value = conservation.copy(messages = merged)
    }

    override suspend fun sendMessage(conservationId: String, message: Message) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        val conservation = _conservations.value.find { it.docId == conservationId }
            ?: throw Exception("Conservation not found")

        val messageRef = firestore.collection("conservations").document(conservationId)
            .collection("messages").document()
        val createdAt = Timestamp.now()
        val newMessage = message.copy(
            docId = messageRef.id,
            sender = firestore.collection("users").document(authUser.uid),
            createdAt = createdAt,
            isSending = true,
            isSentSuccess = null,
        )

        val optimisticList = (messageCache[conservationId] ?: emptyList()) + newMessage
        messageCache[conservationId] = optimisticList
        _conservations.update { list ->
            list.map { if (it.docId == conservationId) it.copy(messages = optimisticList) else it }
        }
        _currentConservation.value = conservation.copy(messages = optimisticList)

        try {
            firestore.runTransaction { transaction ->
                transaction.set(
                    messageRef,
                    newMessage.copy(isSending = null, isSentSuccess = null)
                )
                val sentMessage = newMessage.copy(isSending = false, isSentSuccess = true)
                val replacedList = (messageCache[conservationId] ?: emptyList()).map {
                    if (it.docId == sentMessage.docId) sentMessage else it
                }
                messageCache[conservationId] = replacedList

                _conservations.update { list ->
                    list.map {
                        if (it.docId == conservationId)
                            it.copy(
                                sender = firestore.collection("users").document(authUser.uid),
                                messages = replacedList,
                                lastTime = createdAt,
                                lastContent = when(sentMessage.type) {
                                    MessageType.TEXT.name -> sentMessage.content ?: ""
                                    MessageType.IMAGE.name -> "{Someone} has sent a image"
                                    MessageType.VIDEO.name -> "{Someone} has sent a video"
                                    MessageType.VOICE.name -> "{Someone} has sent a voice"
                                    else -> ""
                                },
                                senderSeen = true,
                                receiverSeen = false
                            )
                        else it
                    }
                }
                _currentConservation.value = conservation.copy(messages = replacedList)
            }.await()
        } catch(e: Exception) {
            val failMessage = newMessage.copy(isSending = false, isSentSuccess = false)
            val replacedList = (messageCache[conservationId] ?: emptyList()).map {
                if (it.docId == failMessage.docId) failMessage else it
            }
            messageCache[conservationId] = replacedList

            _conservations.update { list ->
                list.map { if (it.docId == conservationId) it.copy(messages = replacedList) else it }
            }
            _currentConservation.value = conservation.copy(messages = replacedList)
            throw e
        }
    }

    override suspend fun deleteMessage(conservationId: String, messageId: String) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        val conservationRef = firestore.collection("conservations").document(conservationId)
        val messageRef = conservationRef.collection("messages").document(messageId)

        firestore.runTransaction { transaction ->
            val messageSnapshot = transaction.get(messageRef)
            if (!messageSnapshot.exists()) throw Exception("Message does not exists")

            transaction.update(
                messageRef, mapOf(
                    "deleteBy" to firestore.collection("users").document(authUser.uid),
                    "content" to "Message is deleted",
                    "type" to MessageType.DELETED.name
                )
            )

            transaction.update(
                conservationRef, mapOf(
                    "sender" to firestore.collection("users").document(authUser.uid),
                    "lastContent" to "{Someone} has removed message",
                )
            )
        }.await()
    }

    override fun clearCache() {
        removeListener()
        userCache.clear()
        userListeners.forEach { it.value.remove() }
        userListeners.clear()
        messageCache.clear()
        _conservations.value = emptyList()

    }
}
