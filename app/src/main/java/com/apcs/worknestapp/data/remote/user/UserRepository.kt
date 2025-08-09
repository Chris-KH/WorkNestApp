package com.apcs.worknestapp.data.remote.user

import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
    val friends: StateFlow<List<User>>
    val foundUser: StateFlow<Map<String, User>>

    suspend fun getUser(docId: String): User
    suspend fun refreshUser(docId: String): User
    suspend fun findUsers(searchValue: String): List<User>
    suspend fun sendFriendRequest(receiverId: String)
    suspend fun acceptFriendRequest(docId: String)
    suspend fun deleteFriendship(docId: String)
    fun clearCache()
}
