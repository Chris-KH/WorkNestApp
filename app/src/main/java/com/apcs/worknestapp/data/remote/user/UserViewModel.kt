package com.apcs.worknestapp.data.remote.user

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepo: UserRepository,
) : ViewModel() {
    val friends = userRepo.friends
    val friendships = userRepo.friendships

    suspend fun getUser(docId: String): User? {
        return try {
            userRepo.getUser(docId)
        } catch(e: Exception) {
            Log.e("UserViewModel", "Get user failed", e)
            null
        }
    }

    suspend fun refreshUser(docId: String): User? {
        return try {
            userRepo.refreshUser(docId)
        } catch(e: Exception) {
            Log.e("UserViewModel", "Refresh user failed", e)
            null
        }
    }

    suspend fun findUsers(searchValue: String): List<User> {
        return try {
            userRepo.findUsers(searchValue)
        } catch(e: Exception) {
            Log.e("UserViewModel", "Find users failed", e)
            emptyList()
        }
    }

    suspend fun sendFriendRequest(receiverId: String): Boolean {
        return try {
            userRepo.sendFriendRequest(receiverId)
            true
        } catch(e: Exception) {
            Log.e("UserViewModel", "Send friend request failed", e)
            false
        }
    }

    suspend fun acceptFriendRequest(docId: String): Boolean {
        return try {
            userRepo.acceptFriendRequest(docId)
            true
        } catch(e: Exception) {
            Log.e("UserViewModel", "Accept friend request failed", e)
            false
        }
    }

    suspend fun deleteFriendship(docId: String): Boolean {
        return try {
            userRepo.deleteFriendship(docId)
            true
        } catch(e: Exception) {
            Log.e("UserViewModel", "Delete friendship failed", e)
            false
        }
    }
}
