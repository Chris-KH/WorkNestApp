package com.apcs.worknestapp.data.remote.user

import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
    val friends: StateFlow<List<User>>

    suspend fun findUsers(searchValue: String): List<User>
}
