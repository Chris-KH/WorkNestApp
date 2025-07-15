package com.apcs.worknestapp.data.remote.auth

import com.apcs.worknestapp.R
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat.getString
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialOption
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GoogleAuthUiClient @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val credentialManager = CredentialManager.create(context)
    private val WEB_CLIENT_ID = getString(context, R.string.default_web_client_id)


    fun createGoogleIdOption(nonce: String = ""): GetGoogleIdOption {
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(WEB_CLIENT_ID)
            .setAutoSelectEnabled(true)
            // nonce string to use when generating a Google ID token
            .setNonce(nonce)
            .build()

        return googleIdOption
    }

    fun createGoogleSignInWithGoogleOption(nonce: String = ""): GetSignInWithGoogleOption {
        val signInWithGoogleOption: GetSignInWithGoogleOption =
            GetSignInWithGoogleOption.Builder(
                serverClientId = WEB_CLIENT_ID
            ).setNonce(nonce).build()

        return signInWithGoogleOption
    }

    suspend fun getGoogleIdToken(
        googleIdOption: CredentialOption,
    ): String? {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdCredential = GoogleIdTokenCredential.createFrom(credential.data)
                googleIdCredential.idToken
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("GoogleAuthClient", "Google sign in failed", e)
            null
        }
    }

    suspend fun clearCredential() {
        val clearRequest = ClearCredentialStateRequest()
        credentialManager.clearCredentialState(clearRequest)
    }
}
