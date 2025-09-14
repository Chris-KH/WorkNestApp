package com.apcs.worknestapp.data.remote.message

import android.util.Log
import com.apcs.worknestapp.data.remote.user.User
import com.apcs.worknestapp.domain.usecase.AppDefault
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineExceptionHandler
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
import kotlin.collections.set

class MessageRepositoryImpl @Inject constructor() : MessageRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val errorHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("MessageRepository", "Coroutine crashed", throwable)
    }
    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + errorHandler)

    private val _conservations = MutableStateFlow(emptyList<Conservation>())
    override val conservations: StateFlow<List<Conservation>> = _conservations.asStateFlow()
    private var conservationsListener: ListenerRegistration? = null

    private val userCache = mutableMapOf<String, ConservationUserData>()
    private val userListeners = mutableMapOf<String, ListenerRegistration>()

    private val messageCache = mutableMapOf<String, List<Message>>()

    private val _currentConservation = MutableStateFlow<Conservation?>(null)
    override val currentConservation: StateFlow<Conservation?> = _currentConservation.asStateFlow()
    private var currentConservationsListener: ListenerRegistration? = null
    private var messageListener: ListenerRegistration? = null


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

    private fun unObserveUser(otherUserId: String) {
        userListeners[otherUserId]?.remove()
        userListeners.remove(otherUserId)
    }

    private suspend fun Conservation.withOtherUserData(
        otherUserId: String,
        isCache: Boolean = false,
    ): Conservation {
        if (isCache) {
            val cached = userCache[otherUserId]
            observeUser(otherUserId)
            if (cached != null) {
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
        if (_currentConservation.value?.docId == conservationId) {
            _currentConservation.update { conservation ->
                conservation?.copy(messages = messages)
            }
        }
    }

    override fun registerConservationListener() {
        val authUser = auth.currentUser ?: return

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

            repoScope.launch {
                val authUser = auth.currentUser ?: return@launch
                val conservationList = snapshot.documents.mapNotNull { doc ->
                    val cons = doc.toObject(Conservation::class.java) ?: return@mapNotNull null
                    val otherUserId = cons.userIds?.firstOrNull { it != authUser.uid }
                    if (otherUserId == null) return@mapNotNull null
                    Log.d("Test", cons.toString())
                    if (cons.deletedFor != null && cons.deletedFor.contains(authUser.uid)) {
                        messageCache.remove(doc.id)
                        unObserveUser(otherUserId)
                        userCache.remove(otherUserId)
                        return@mapNotNull null
                    }

                    async { cons.withOtherUserData(otherUserId, isCache = true) }
                }.awaitAll().map { it.copy(messages = messageCache[it.docId] ?: emptyList()) }
                if (!snapshot.metadata.isFromCache) _conservations.value = conservationList
            }
        }
    }

    override fun removeConservationListener() {
        conservationsListener?.remove()
        conservationsListener = null

        userListeners.forEach { it.value.remove() }
        userListeners.clear()
    }

    override fun registerCurrentConservationListener(conservationId: String) {
        val authUser = auth.currentUser
        if (authUser == null) {
            removeCurrentConservationListener()
            return
        }
        removeCurrentConservationListener()

        val conservationRef = firestore.collection("conservations").document(conservationId)

        currentConservationsListener = null

        messageListener = conservationRef
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                val currentConservationId = _currentConservation.value?.docId
                if (currentConservationId == null) return@addSnapshotListener

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
                    messages = messages.filterNot { msg ->
                        msg.deletedFor?.contains(authUser.uid) == true
                    }.sortedByDescending { it.createdAt })
            }
    }

    override fun removeCurrentConservationListener() {
        messageListener?.remove()
        messageListener = null
        currentConservationsListener?.remove()
        currentConservationsListener = null
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
        Log.d("Test2", messages.toString())

        _conservations.update { list ->
            (list + conservation.copy(
                userData = conservationUserData,
                messages = messages
            )).sortedByDescending { it.lastTime }
        }

        userCache[otherUserId] = conservationUserData
        messageCache[conservation.docId] = messages
    }

    override fun getConservation(docId: String?): Conservation? {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        if (docId == null) {
            removeCurrentConservationListener()
            _currentConservation.value = null
            return null
        } else {
            val conservation = _conservations.value.find { it.docId == docId }
                ?: throw Exception("This conservation does not exist")
            val otherUserId = conservation.userIds?.firstOrNull { it != authUser.uid }
            if (otherUserId != null) observeUser(otherUserId)
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
        observeUser(userId)
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
                val cons = doc.toObject(Conservation::class.java) ?: return@mapNotNull null
                val otherUserId = cons.userIds?.firstOrNull { it != authUser.uid }
                if (otherUserId == null) return@mapNotNull null

                if (cons.deletedFor != null && cons.deletedFor.contains(authUser.uid)) {
                    messageCache.remove(doc.id)
                    unObserveUser(otherUserId)
                    userCache.remove(otherUserId)
                    return@mapNotNull null
                }
                async { cons.withOtherUserData(otherUserId) }
            }.awaitAll()
        }.map { it.copy(messages = messageCache[it.docId] ?: emptyList()) }

        _conservations.value = conservationList
    }

    override suspend fun deleteConservation(docId: String) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        val conservationRef = firestore.collection("conservations").document(docId)
        val messagesRef =
            firestore.collection("conservations").document(docId).collection("messages")

        val currentMessages = messageCache[docId]
        messageCache.remove(docId)

        firestore.runTransaction { transaction ->
            currentMessages?.forEach { message ->
                val messageId = message.docId
                if (messageId != null) transaction.update(
                    messagesRef.document(messageId),
                    "deletedFor",
                    FieldValue.arrayUnion(authUser.uid)
                )
            }
            transaction.update(conservationRef, "deletedFor", FieldValue.arrayUnion(authUser.uid))
        }.await()

        _conservations.update { list -> list.filterNot { it.docId == docId } }
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
        }.await()
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
                        "receiverSeen" to false,
                        "deletedFor" to FieldValue.arrayRemove(authUser.uid)
                    )
                )

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


    override suspend fun deleteMessage(
        conservationId: String,
        messageId: String,
        isForMe: Boolean,
    ) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        val conservationRef = firestore.collection("conservations").document(conservationId)
        val messageRef = conservationRef.collection("messages").document(messageId)

        val deleteInfo = mapOf(
            "sender" to firestore.collection("users").document(authUser.uid),
            "lastTime" to Timestamp.now(),
            "lastContent" to "!!! Some messages are deleted",
            "senderSeen" to true,
            "receiverSeen" to false,
        )

        if (isForMe) {
            firestore.runTransaction { transaction ->
                transaction.update(messageRef, "deletedFor", FieldValue.arrayUnion(authUser.uid))
                transaction.update(conservationRef, deleteInfo)
            }
        } else {
            firestore.runTransaction { transaction ->
                val conservationSnapshot = transaction.get(conservationRef)
                if (!conservationSnapshot.exists()) {
                    throw Exception("Conservation not found")
                }
                val conservation = conservationSnapshot.toObject(Conservation::class.java)
                    ?: throw Exception("Invalid conservation data")
                val otherUserId = conservation.userIds?.firstOrNull { it != authUser.uid }
                    ?: throw Exception("Missing user in conservation")
                transaction.update(
                    messageRef,
                    "deletedFor",
                    FieldValue.arrayUnion(otherUserId, authUser.uid)
                )
                transaction.update(conservationRef, deleteInfo)
            }
        }
    }

    override fun clearCache() {
        repoScope.coroutineContext.cancelChildren()
        removeConservationListener()
        removeCurrentConservationListener()
        userCache.clear()
        messageCache.clear()
        _conservations.value = emptyList()
        _currentConservation.value = null
    }
}
