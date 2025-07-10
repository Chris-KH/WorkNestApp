package com.apcs.worknestapp.data.remote.auth

import com.apcs.worknestapp.data.remote.auth.UserProfile
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val user: StateFlow<FirebaseUser?>
    val profile: StateFlow<UserProfile?>

    suspend fun signUpWithEmailPassword(email: String, password: String, name: String)
    suspend fun login(email: String, password: String)
    suspend fun updateUserName(name: String)
    suspend fun updateUserPhone(phone: String)
    suspend fun updateUserAddress(address: String)
    suspend fun checkAuth()
    suspend fun signOut()
}
