package com.apcs.worknestapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.apcs.worknestapp.auth.AuthViewModel
import com.apcs.worknestapp.ui.components.LoadingScreen
import com.apcs.worknestapp.ui.screens.Screen
import com.apcs.worknestapp.ui.theme.WorkNestAppTheme
import com.google.firebase.FirebaseApp
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

val LocalAuthViewModel = staticCompositionLocalOf<AuthViewModel> {
    error("AuthViewModel not provided")
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)
        super.onCreate(savedInstanceState)
        installSplashScreen()

        authViewModel.checkAuth()

        enableEdgeToEdge()
        setContent {
            val isCheckingAuth by authViewModel.isCheckingAuth.collectAsState()
            val authUser by authViewModel.user.collectAsState()

            CompositionLocalProvider(LocalAuthViewModel provides authViewModel) {
                WorkNestAppTheme(dynamicColor = false) {
                    if (isCheckingAuth) LoadingScreen()
                    else MainLayout(startDestination = if (authUser != null) Screen.Home.route else Screen.Login.route)
                }
            }
        }
    }
}
