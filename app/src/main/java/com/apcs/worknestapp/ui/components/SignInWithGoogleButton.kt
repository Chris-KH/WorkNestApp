package com.apcs.worknestapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.apcs.worknestapp.LocalAuthViewModel
import com.apcs.worknestapp.R
import com.apcs.worknestapp.data.remote.auth.GoogleAuthUiClient
import com.apcs.worknestapp.ui.screens.Screen
import kotlinx.coroutines.launch

@Composable
fun SignInWithGoogleButton(
    onSubmit: () -> Unit,
    onSuccess: suspend () -> Unit,
    onFailure: suspend () -> Unit,
    enabled: Boolean = true,
) {
    val authViewModel = LocalAuthViewModel.current
    val context = LocalContext.current
    val googleAuthUiClient = remember { GoogleAuthUiClient(context) }
    val coroutineScope = rememberCoroutineScope()

    val isDark = isSystemInDarkTheme()

    Button(
        onClick = {
            coroutineScope.launch {
                val option = googleAuthUiClient.createGoogleSignInWithGoogleOption()
                val idToken = googleAuthUiClient.getGoogleIdToken(option)
                if (idToken != null) {
                    onSubmit()
                    val isSuccess = authViewModel.loginWithGoogle(idToken)
                    if (isSuccess) onSuccess()
                    else onFailure()
                }
            }
        },
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
        ),
        contentPadding = PaddingValues(0.dp),
        shape = CircleShape,
        modifier = Modifier,
    ) {
        Image(
            painter = painterResource(
                if (isDark) R.drawable.google_sign_in_button_light
                else R.drawable.google_sign_in_button_dark,
            ),
            contentDescription = null,
            modifier = Modifier.height(40.dp)
        )
    }
}
