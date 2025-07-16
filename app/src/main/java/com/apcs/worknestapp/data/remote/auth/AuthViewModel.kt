package com.apcs.worknestapp.data.remote.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val googleAuthUiClient: GoogleAuthUiClient,
    private val sessionManager: UserSessionManager,
) : ViewModel() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    val profile = repository.profile
    val user = repository.user

    private val _isCheckingAuth = MutableStateFlow(true)
    val isCheckingAuth: StateFlow<Boolean> = _isCheckingAuth

//    init {
//        firebaseAuth.addAuthStateListener { auth ->
//            if (auth.currentUser == null) {
//                viewModelScope.launch {
//                    signOut()
//                }
//            }
//        }
//    }

    fun checkAuth() {
        viewModelScope.launch {
            _isCheckingAuth.value = true
            try {
                repository.checkAuth()
                Log.d("AuthViewModel", "Profile: ${repository.profile.value}")
            } catch (e: Exception) {
                signOut()
                Log.e("AuthViewModel", "Check failed", e)
            } finally {
                _isCheckingAuth.value = false
            }
        }
    }

    suspend fun loadUserProfile(): Boolean {
        return try {
            repository.loadUserProfile()
            true
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Load user profile failed", e)
            false
        }
    }

    suspend fun signUpWithEmailPassword(email: String, password: String, name: String): String? {
        return try {
            repository.signUpWithEmailPassword(email = email, password = password, name = name)
            Log.d("AuthViewModel", "Signup success")
            null
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Signup failed", e)
            signOut()
            e.message ?: "Signup failed for some reason"
        }
    }

    suspend fun login(email: String, password: String): Boolean {
        return try {
            repository.login(email, password)
            Log.d("AuthViewModel", "Login success")
            true
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Login failed", e)
            signOut()
            false
        }
    }

    suspend fun loginWithGoogle(idToken: String?): Boolean {
        return try {
            if (idToken == null) throw Exception("ID token null")

            repository.loginWithGoogle(idToken);
            true
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Google login failed", e)
            false
        }
    }

    suspend fun updateUserName(
        name: String?,
    ): Boolean {
        return try {
            if (name == null) throw Exception("User name cannot be null")
            repository.updateUserName(name)
            true
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Update user name failed", e)
            false
        }
    }

    suspend fun updateUserPhone(
        phone: String?,
    ): Boolean {
        return try {
            if (phone == null) throw Exception("User phone cannot be null")
            repository.updateUserPhone(phone)
            true
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Update user phone failed", e)
            false
        }
    }

    suspend fun updateUserAddress(
        address: String?,
    ): Boolean {
        return try {
            if (address == null) throw Exception("User address cannot be null")
            repository.updateUserAddress(address)
            true
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Update user address failed", e)
            false
        }
    }

    suspend fun signOut(): Boolean {
        return try {
            googleAuthUiClient.clearCredential()
            sessionManager.signOutAndClearAll()
            true
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Logout failed", e)
            false
        }
    }
}
