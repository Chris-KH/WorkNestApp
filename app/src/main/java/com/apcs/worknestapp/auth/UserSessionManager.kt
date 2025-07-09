package com.apcs.worknestapp.auth

import javax.inject.Inject

class UserSessionManager @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend fun signOutAndClearAll() {
        authRepository.signOut()
    }
}
