package com.apcs.worknestapp.data.remote.auth

import javax.inject.Inject

class UserSessionManager @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend fun signOutAndClearAll() {
        authRepository.signOut()
    }
}
