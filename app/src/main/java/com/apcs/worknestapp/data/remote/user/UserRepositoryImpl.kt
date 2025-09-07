package com.apcs.worknestapp.data.remote.user

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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

    private val _friendships = MutableStateFlow(emptyList<Friendship>())
    override val friendships: StateFlow<List<Friendship>> = _friendships

    private val _foundUsers = MutableStateFlow(emptyMap<String, User>())
    override val foundUsers: StateFlow<Map<String, User>> = _foundUsers

    private var friendshipListener: ListenerRegistration? = null

    init {
        auth.addAuthStateListener {
            val user = it.currentUser
            if (user == null) {
                clearCache()
            } else {
                removeListener()
                registerFriendshipListener()
            }
        }
    }

    override fun removeListener() {
        friendshipListener?.remove()
        friendshipListener = null
    }

    override fun registerFriendshipListener() {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        friendshipListener?.remove()

        val friendshipRef = firestore.collection("friendships")
            .whereArrayContains("users", authUser.uid)
        friendshipListener = friendshipRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("UserRepository", "Listen friendship snapshot failed", error)
                return@addSnapshotListener
            } else if (snapshot != null) {
                val friendshipList = snapshot.documents.mapNotNull {
                    it.toObject(Friendship::class.java)
                }
                _friendships.value = friendshipList
            }
        }
    }

    override suspend fun getUser(docId: String): User {
        auth.currentUser ?: throw Exception("User not logged in")
        _foundUsers.value[docId]?.let { return it }

        return refreshUser(docId)
    }

    override suspend fun refreshUser(docId: String): User {
        auth.currentUser ?: throw Exception("User not logged in")

        val userRef = firestore.collection("users")
        val snapshot = userRef.document(docId).get().await()
        val user = snapshot.toObject(User::class.java) ?: throw Exception("User not found")
        _foundUsers.update { it + (docId to user) }

        return user
    }

    override suspend fun findUsers(searchValue: String): List<User> {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        val userRef = firestore.collection("users")
        val snapshot = userRef.where(
            Filter.or(
                Filter.and(
                    Filter.greaterThanOrEqualTo("email", searchValue),
                    Filter.lessThan("email", searchValue + "\uF8FF")
                ),
                Filter.and(
                    Filter.greaterThanOrEqualTo("name", searchValue),
                    Filter.lessThan("name", searchValue + "\uF8FF"),
                )
            )
        ).orderBy("name").get().await()

        return snapshot.documents.mapNotNull {
            val user = it.toObject(User::class.java)
            if (user?.docId == authUser.uid) null
            else user
        }
    }

    override suspend fun loadFriends() {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        val friendshipSnapshot = firestore
            .collection("friendships")
            .whereArrayContains("users", authUser.uid)
            .get()
            .await()

        val friendshipList = friendshipSnapshot.documents.mapNotNull {
            it.toObject(Friendship::class.java)
        }

        _friendships.value = friendshipList

        val friendUserIds = friendshipList
            .filter { it.status == "accepted" }
            .flatMap { it.users ?: emptyList() }
            .filter { it != authUser.uid }
            .distinct()

        if (friendUserIds.isEmpty()) {
            _friends.value = emptyList()
            return
        }

        val friendsSnapshot = firestore
            .collection("users")
            .whereIn(FieldPath.documentId(), friendUserIds)
            .get()
            .await()

        val friendUsers = friendsSnapshot.documents.mapNotNull {
            it.toObject(User::class.java)
        }

        _friends.value = friendUsers
    }

    override suspend fun loadFriendship() {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")

        val friendshipRef = firestore.collection("friendships")
        val snapshot = friendshipRef.whereArrayContains("users", authUser.uid).get().await()

        _friendships.value = snapshot.documents.mapNotNull {
            it.toObject(Friendship::class.java)
        }
    }

    override suspend fun sendFriendRequest(receiverId: String) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        val pairId = listOf(authUser.uid, receiverId).sorted().joinToString("_")

        val friendshipRef = firestore.collection("friendships").document(pairId)
        val senderRef = firestore.collection("users").document(authUser.uid)
        val receiverRef = firestore.collection("users").document(receiverId)

        val friendship = Friendship(
            docId = pairId,
            users = listOf(authUser.uid, receiverId).sorted(),
            sender = senderRef,
            receiver = receiverRef,
            status = "pending",
        )

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(friendshipRef)
            if (snapshot.exists()) throw Exception("Friend request already exits")
            else transaction.set(friendshipRef, friendship)
        }.await()
        _friendships.update { it + friendship }
    }

    override suspend fun acceptFriendRequest(docId: String) {
        auth.currentUser ?: throw Exception("User not logged in")

        firestore.collection("friendships").document(docId).update("status", "accepted").await()

        _friendships.update { list ->
            list.map { if (it.docId == docId) it.copy(status = "accepted") else it }
        }
    }

    override suspend fun deleteFriendship(docId: String) {
        val authUser = auth.currentUser ?: throw Exception("User not logged in")
        val friendship = _friendships.value.find { it.docId == docId }
            ?: throw Exception("Friendship does not exist")
        val otherUserId = friendship.users?.firstOrNull { it != authUser.uid }
            ?: throw Exception("Missing a user in a friendship")

        firestore.collection("friendships").document(docId).delete().await()

        _friendships.update { list ->
            list.filterNot { it.docId == docId }
        }
        _friends.update { list ->
            list.filterNot { it.docId == otherUserId }
        }
    }

    override fun clearCache() {
        removeListener()
        _friends.value = emptyList()
        _friendships.value = emptyList()
        _foundUsers.value = emptyMap()
    }
}
