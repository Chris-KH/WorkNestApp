package com.apcs.worknestapp.data.remote.user

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepo: UserRepository,
) : ViewModel() {

    suspend fun findUsers(searchValue: String): List<User> {
        return try {
            userRepo.findUsers(searchValue)
        } catch(e: Exception) {
            Log.e("UserViewModel", "Find users failed", e)
            emptyList()
        }
    }
}
