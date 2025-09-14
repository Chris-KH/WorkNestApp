package com.apcs.worknestapp.data.remote.auth

import android.util.Log
import com.apcs.worknestapp.data.remote.notification.Notification
import com.apcs.worknestapp.domain.logic.Validator
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val googleAuthUiClient: GoogleAuthUiClient,
    private val sessionManager: SessionManager,
) : AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _user = MutableStateFlow<FirebaseUser?>(null)
    override val user: StateFlow<FirebaseUser?> = _user

    private val _profile = MutableStateFlow<UserProfile?>(null)
    override val profile: StateFlow<UserProfile?> = _profile

    private var profileListener: ListenerRegistration? = null

    init {
        auth.addAuthStateListener {
            val user = it.currentUser
            if (user == null) removeProfileListener()
        }
    }

    private fun registerProfileListener() {
        val authUser = auth.currentUser
        if (authUser == null) {
            registerProfileListener()
            return
        }
        removeProfileListener()

        val profileRef = firestore
            .collection("users")
            .document(authUser.uid)

        profileListener = profileRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("AuthRepository", "Listen user profile failed", error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val updatedProfile = snapshot.toObject(UserProfile::class.java)
                updatedProfile?.let { _profile.value = updatedProfile }
            }
        }
    }

    private fun removeProfileListener() {
        profileListener?.remove()
        profileListener = null
    }

    private suspend fun getUserRemoteProfile(uid: String): UserProfile {
        val snapshot = firestore.collection("users")
            .document(uid)
            .get()
            .await()

        if (!snapshot.exists()) throw Exception("Cannot load profile data")

        return snapshot.toObject(UserProfile::class.java)
            ?: throw Exception("Invalid profile data")
    }

    override suspend fun loadUserProfile() {
        val authUser = auth.currentUser ?: throw Exception("No user logged in")

        _profile.value = getUserRemoteProfile(authUser.uid)
    }

    override suspend fun checkAuth() {
        val currentUser = auth.currentUser ?: throw Exception("Check auth fail")

        _profile.value = getUserRemoteProfile(currentUser.uid)
        _user.value = currentUser
        updateUserStatus(isOnline = true)
    }

    override suspend fun signUpWithEmailPassword(email: String, password: String, name: String) {
        if (
            !Validator.isValidEmail(email) ||
            !Validator.isStrongPassword(password) ||
            !Validator.isUserName(name)
        ) throw Exception("Wrong email and password format")

        val res = auth.createUserWithEmailAndPassword(email, password).await()
        val authUser = res.user
        if (authUser == null) throw Exception("Create user failed")

        val createdAt = authUser.metadata?.creationTimestamp?.let {
            Timestamp(
                seconds = it / 1000,
                nanoseconds = (it % 1000 * 1_000_000).toInt()
            )
        } ?: Timestamp.now()

        val newProfile = UserProfile(
            docId = authUser.uid,
            name = name,
            email = email,
            createdAt = createdAt
        )

        firestore.collection("users").document(authUser.uid).set(newProfile, SetOptions.merge())
            .await()

        _profile.value = newProfile
        _user.value = authUser
        updateUserStatus(isOnline = true)
        registerProfileListener()
    }

    override suspend fun login(email: String, password: String) {
        val res = auth.signInWithEmailAndPassword(email, password).await()
        val authUser = res.user

        if (authUser == null) throw Exception("Login failed for some reason")

        _profile.value = getUserRemoteProfile(authUser.uid)
        _user.value = authUser
        updateUserStatus(isOnline = true)
        registerProfileListener()
    }

    override suspend fun loginWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val authUser = result.user ?: throw Exception("Google login failed")

        try {
            val remoteProfile = getUserRemoteProfile(authUser.uid)
            _profile.value = remoteProfile
        } catch(e: Exception) {
            val createdAt = authUser.metadata?.creationTimestamp?.let {
                Timestamp(
                    seconds = it / 1000,
                    nanoseconds = (it % 1000 * 1_000_000).toInt()
                )
            } ?: Timestamp.now()

            val newProfile = UserProfile(
                docId = authUser.uid,
                name = authUser.displayName,
                avatar = if (authUser.photoUrl == null) null else authUser.photoUrl.toString(),
                email = authUser.email,
                phone = authUser.phoneNumber,
                createdAt = createdAt,
            )

            firestore.collection("users").document(authUser.uid)
                .set(newProfile, SetOptions.merge()).await()
            _profile.value = newProfile
        }

        _user.value = authUser
        updateUserStatus(isOnline = true)
        registerProfileListener()
    }

    override suspend fun updateUserName(name: String) {
        val uid = auth.currentUser?.uid ?: throw Exception("No user logged in")
        if (name.isBlank()) throw Exception("Name cannot be blank")

        firestore.collection("users")
            .document(uid)
            .update("name", name.trim())
            .await()

        _profile.update { it?.copy(name = name.trim()) }
    }

    override suspend fun updateUserAvatar(avatar: String) {
        val uid = auth.currentUser?.uid ?: throw Exception("No user logged in")
        if (avatar.isBlank()) throw Exception("Avatar cannot be blank")

        firestore.collection("users")
            .document(uid)
            .update("avatar", avatar.trim())
            .await()

        _profile.update { it?.copy(avatar = avatar.trim()) }
    }

    override suspend fun updateUserPhone(phone: String) {
        val uid = auth.currentUser?.uid ?: throw Exception("No user logged in")
        if (phone.isBlank()) throw Exception("Phone cannot be blank")

        firestore.collection("users")
            .document(uid)
            .update("phone", phone.trim())
            .await()

        _profile.update { it?.copy(phone = phone.trim()) }
    }

    override suspend fun updateUserAddress(address: String) {
        val uid = auth.currentUser?.uid ?: throw Exception("No user logged in")

        firestore.collection("users")
            .document(uid)
            .update("address", address)
            .await()

        _profile.update { it?.copy(address = address) }
    }

    override suspend fun updateUserBio(bio: String) {
        val uid = auth.currentUser?.uid ?: throw Exception("No user logged in")

        firestore.collection("users")
            .document(uid)
            .update("bio", bio)
            .await()

        _profile.update { it?.copy(bio = bio) }
    }

    override suspend fun updateUserPronouns(pronouns: String) {
        val uid = auth.currentUser?.uid ?: throw Exception("No user logged in")

        firestore.collection("users")
            .document(uid)
            .update("pronouns", pronouns)
            .await()

        _profile.update { it?.copy(pronouns = pronouns) }
    }

    override suspend fun updateUserStatus(isOnline: Boolean) {
        val uid = auth.currentUser?.uid ?: throw Exception("No user logged in")

        firestore.collection("users")
            .document(uid)
            .update("online", isOnline)
            .await()

        _profile.update { it?.copy(online = isOnline) }
    }

    override suspend fun signOut() {
        updateUserStatus(false)
        googleAuthUiClient.clearCredential()
        sessionManager.clearAllCache()
        auth.signOut()
        _profile.value = null
        _user.value = null
    }
}
