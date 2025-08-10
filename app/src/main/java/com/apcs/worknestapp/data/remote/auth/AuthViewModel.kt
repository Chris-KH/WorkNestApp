package com.apcs.worknestapp.data.remote.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
) : ViewModel() {
    val profile = repository.profile
    val user = repository.user

    private val _isCheckingAuth = MutableStateFlow(true)
    val isCheckingAuth: StateFlow<Boolean> = _isCheckingAuth

    fun checkAuth() {
        _isCheckingAuth.value = true
        viewModelScope.launch {
            try {
                repository.checkAuth()
            } catch(e: Exception) {
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
        } catch(e: Exception) {
            Log.e("AuthViewModel", "Load user profile failed", e)
            false
        }
    }

    suspend fun signUpWithEmailPassword(email: String, password: String, name: String): String? {
        return try {
            repository.signUpWithEmailPassword(email = email, password = password, name = name)
            null
        } catch(e: Exception) {
            Log.e("AuthViewModel", "Signup failed", e)
            signOut()
            e.message ?: "Signup failed for some reason"
        }
    }

    suspend fun login(email: String, password: String): Boolean {
        return try {
            repository.login(email, password)
            true
        } catch(e: Exception) {
            Log.e("AuthViewModel", "Login failed", e)
            signOut()
            false
        }
    }

    suspend fun loginWithGoogle(idToken: String?): Boolean {
        return try {
            if (idToken == null) throw Exception("ID token null")
            repository.loginWithGoogle(idToken)
            true
        } catch(e: Exception) {
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
        } catch(e: Exception) {
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
        } catch(e: Exception) {
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
        } catch(e: Exception) {
            Log.e("AuthViewModel", "Update user address failed", e)
            false
        }
    }

    suspend fun updateUserAvatar(
        avatar: String?,
    ): Boolean {
        return try {
            if (avatar == null) throw Exception("User avatar cannot be null")
            repository.updateUserAvatar(avatar)
            true
        } catch(e: Exception) {
            Log.e("AuthViewModel", "Update user avatar failed", e)
            false
        }
    }

    suspend fun updateUserBio(
        bio: String?,
    ): Boolean {
        return try {
            if (bio == null) throw Exception("User bio cannot be null")
            repository.updateUserBio(bio)
            true
        } catch(e: Exception) {
            Log.e("AuthViewModel", "Update user bio failed", e)
            false
        }
    }

    suspend fun updateUserPronouns(
        pronouns: String?,
    ): Boolean {
        return try {
            if (pronouns == null) throw Exception("User pronouns cannot be null")
            repository.updateUserPronouns(pronouns)
            true
        } catch(e: Exception) {
            Log.e("AuthViewModel", "Update user pronouns failed", e)
            false
        }
    }

    suspend fun signOut(): Boolean {
        return try {
            repository.signOut()
            true
        } catch(e: Exception) {
            Log.e("AuthViewModel", "Logout failed", e)
            false
        }
    }
}
