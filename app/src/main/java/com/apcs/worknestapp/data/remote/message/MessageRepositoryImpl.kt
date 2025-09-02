package com.apcs.worknestapp.data.remote.message

import android.util.Log
import com.apcs.worknestapp.data.remote.user.User
import com.apcs.worknestapp.domain.usecase.AppDefault
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor() : MessageRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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


    private fun updateMessages(
        conservationId: String,
        messages: List<Message>,
    ) {
        messageCache[conservationId] = messages
        _conservations.update { list ->
            list.map { if (it.docId == conservationId) it.copy(messages = messages) else it }
        }
        if (_currentConservation.value?.docId == conservationId) {
            _currentConservation.value = _conservations.value.find {
                it.docId == conservationId
            }?.copy(messages = messages)
        }
    }

    override fun registerConservationListener() {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        val conservationSnapshot = firestore.collection("conservations")
            .whereArrayContains("userIds", authUser.uid)
            .orderBy("lastTime", Query.Direction.DESCENDING)

        if (conservationsListener != null) return
        conservationsListener = conservationSnapshot.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("MessageRepository", "Listen conservations snapshot failed", error)
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener
            if (snapshot.metadata.isFromCache) return@addSnapshotListener

            repoScope.launch {
                val authUser = auth.currentUser ?: return@launch
                val conservationList = snapshot.documents.mapNotNull { doc ->
                    val cons = doc.toObject(Conservation::class.java) ?: return@mapNotNull null
                    val otherUserId = cons.userIds?.firstOrNull { it != authUser.uid }
                        ?: return@mapNotNull null

                    async { cons.withOtherUserData(otherUserId, isCache = true) }
                }.awaitAll().map { it.copy(messages = messageCache[it.docId] ?: emptyList()) }
                Log.d("Test", conservationList.toString())
                _conservations.value = conservationList
            }
        }
    }

    override fun removeConservationListener() {
        conservationsListener?.remove()
        conservationsListener = null

        userListeners.forEach { it.value.remove() }
        userListeners.clear()
    }

    override fun registerMessageListener(conservationId: String) {
        auth.currentUser ?: throw Exception("User not logged in")
        if (messageListeners.containsKey(conservationId)) return

        val listener = firestore.collection("conservations").document(conservationId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("MessageRepository", "Listen messages snapshot failed", error)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                val messages = (messageCache[conservationId] ?: emptyList()).toMutableList()

                for(change in snapshot.documentChanges) {
                    val msg = change.document.toObject(Message::class.java)
                    when(change.type) {
                        DocumentChange.Type.ADDED -> {
                            if (messages.any { it.docId == msg.docId }) continue
                            messages.add(msg)
                        }

                        DocumentChange.Type.MODIFIED -> {
                            val index = messages.indexOfFirst { it.docId == msg.docId }
                            if (index != -1) messages[index] = msg
                        }

                        DocumentChange.Type.REMOVED -> {
                            val removedId = change.document.id
                            messages.removeIf { it.docId == removedId }
                        }
                    }
                }
                updateMessages(
                    conservationId = conservationId,
                    messages = messages.sortedByDescending { it.createdAt })
            }
        messageListeners[conservationId] = listener
    }

    override fun removeMessageListener(conservationId: String) {
        messageListeners[conservationId]?.remove()
        messageListeners.remove(conservationId)
    }

    override fun createConservation(conservation: Conservation, userMetadata: User) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        if (conservation.docId == null) throw Exception("Missing conservation id")
        if (conservation.userIds == null || !conservation.userIds.contains(authUser.uid))
            throw Exception("Missing user id for conservation")

        val otherUserId = conservation.userIds.firstOrNull { it != authUser.uid }
            ?: throw Exception("Missing user id for conservation")
        val docId = listOf(authUser.uid, otherUserId).sorted().joinToString("_")
        if (conservation.docId != docId) throw Exception("Invalid id for conservation")

        val conservationUserData = ConservationUserData(
            docId = userMetadata.docId,
            name = userMetadata.name,
            avatar = userMetadata.avatar,
            online = userMetadata.online,
        )
        val messages = messageCache[conservation.docId] ?: emptyList()

        _conservations.update { list ->
            (list + conservation.copy(
                userData = conservationUserData,
                messages = messages
            )).sortedByDescending { it.lastTime }
        }
        Log.d("Test", _conservations.value.toString())

        userCache[otherUserId] = conservationUserData
        messageCache[conservation.docId] = messages
    }

    override fun getConservation(docId: String?): Conservation? {
        auth.currentUser ?: throw Exception("User not logged in")
        if (docId == null) {
            messageListeners[_currentConservation.value?.docId]?.remove()
            _currentConservation.value = null
            return null
        } else {
            val conservation = _conservations.value.find { it.docId == docId }
                ?: throw Exception("This conservation does not exist")
            val result = conservation.copy(messages = messageCache[docId] ?: emptyList())

            _currentConservation.value = result
            return result
        }
    }

    override fun getConservationWith(userId: String): Conservation? {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        val conservation = _conservations.value.find {
            it.userIds?.containsAll(listOf(authUser.uid, userId)) == true
        } ?: throw Exception("This conservation does not exist")
        val result = conservation.copy(messages = messageCache[conservation.docId] ?: emptyList())

        _currentConservation.value = result
        return result
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
        }.map { it.copy(messages = messageCache[it.docId] ?: emptyList()) }

        _conservations.value = conservationList
    }

    override suspend fun deleteConservation(docId: String) {
        auth.currentUser ?: throw Exception("User not logged in")

        firestore.collection("conservations").document(docId).delete().await()

        _conservations.update { list -> list.filterNot { it.docId == docId } }
        if (_currentConservation.value?.docId == docId) {
            _currentConservation.value = null
        }

        userCache.remove(docId)
        userListeners[docId]?.remove()
        userListeners.remove(docId)
        messageCache.remove(docId)
        removeMessageListener(docId)
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

        updateMessages(
            conservationId = conservationId,
            messages = merged.sortedByDescending { it.createdAt })
    }

    override suspend fun sendMessage(conservationId: String, message: Message) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        val conservation = _conservations.value.find { it.docId == conservationId }
            ?: throw Exception("Conservation not found")

        val conservationRef = firestore.collection("conservations").document(conservationId)
        val messageRef = conservationRef.collection("messages").document()

        val newMessage = message.copy(
            docId = messageRef.id,
            sender = firestore.collection("users").document(authUser.uid),
            createdAt = Timestamp.now(),
            isSending = true,
            isSentSuccess = null,
        )

        val optimisticList = listOf(newMessage) + (messageCache[conservationId] ?: emptyList())
        updateMessages(conservationId = conservationId, messages = optimisticList)

        try {
            firestore.runTransaction { transaction ->
                val sentMessage = newMessage.copy(
                    isSending = false,
                    isSentSuccess = true,
                )
                val conservationSnapshot = transaction.get(conservationRef)
                if (conservationSnapshot.exists()) {
                    transaction.update(
                        conservationRef, mapOf(
                            "sender" to firestore.collection("users").document(authUser.uid),
                            "lastTime" to Timestamp.now(),
                            "lastContent" to when(sentMessage.type) {
                                MessageType.TEXT.name -> sentMessage.content ?: ""
                                MessageType.IMAGE.name -> "{Someone} has sent a image"
                                MessageType.VIDEO.name -> "{Someone} has sent a video"
                                MessageType.VOICE.name -> "{Someone} has sent a voice"
                                else -> ""
                            },
                            "senderSeen" to true,
                            "receiverSeen" to false
                        )
                    )
                } else {
                    transaction.set(
                        conservationRef, mapOf(
                            "userIds" to conservation.userIds,
                            "sender" to firestore.collection("users").document(authUser.uid),
                            "lastTime" to Timestamp.now(),
                            "lastContent" to when(sentMessage.type) {
                                MessageType.TEXT.name -> sentMessage.content ?: ""
                                MessageType.IMAGE.name -> "{Someone} has sent a image"
                                MessageType.VIDEO.name -> "{Someone} has sent a video"
                                MessageType.VOICE.name -> "{Someone} has sent a voice"
                                else -> ""
                            },
                            "senderSeen" to true,
                            "receiverSeen" to false
                        )
                    )
                }

                transaction.set(
                    messageRef,
                    newMessage.copy(createdAt = null, isSending = null, isSentSuccess = null),
                    SetOptions.merge()
                )

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
                                lastTime = sentMessage.createdAt,
                                lastContent = when(sentMessage.type) {
                                    MessageType.TEXT.name -> sentMessage.content ?: ""
                                    MessageType.IMAGE.name -> "{Someone} has sent a image"
                                    MessageType.VIDEO.name -> "{Someone} has sent a video"
                                    MessageType.VOICE.name -> "{Someone} has sent a voice"
                                    else -> ""
                                },
                                senderSeen = true,
                                receiverSeen = false,
                                isTemporary = false,
                            )
                        else it
                    }.sortedByDescending { it.lastTime }
                }
                _currentConservation.value =
                    conservation.copy(messages = replacedList, isTemporary = false)
            }.await()
        } catch(e: Exception) {
            val failMessage = newMessage.copy(isSending = false, isSentSuccess = false)
            val replacedList = (messageCache[conservationId] ?: emptyList()).map {
                if (it.docId == failMessage.docId) failMessage else it
            }
            messageCache[conservationId] = replacedList
            updateMessages(conservationId = conservationId, messages = replacedList)
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
        repoScope.coroutineContext.cancelChildren()
        removeConservationListener()
        userCache.clear()
        userListeners.forEach { it.value.remove() }
        userListeners.clear()
        messageCache.clear()
        messageListeners.forEach { it.value.remove() }
        messageListeners.clear()
        _conservations.value = emptyList()
        _currentConservation.value = null
    }
}
