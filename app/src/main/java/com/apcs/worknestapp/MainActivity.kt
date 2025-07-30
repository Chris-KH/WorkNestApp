package com.apcs.worknestapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.apcs.worknestapp.data.local.theme.ThemeMode
import com.apcs.worknestapp.data.remote.auth.AuthViewModel
import com.apcs.worknestapp.ui.components.ApplySystemBarTheme
import com.apcs.worknestapp.ui.components.LoadingScreen
import com.apcs.worknestapp.ui.screens.Screen
import com.apcs.worknestapp.ui.theme.WorkNestAppTheme
import com.apcs.worknestapp.data.local.theme.ThemeViewModel
import com.cloudinary.android.MediaManager
import com.google.firebase.FirebaseApp
import dagger.hilt.android.AndroidEntryPoint

val LocalAuthViewModel = staticCompositionLocalOf<AuthViewModel> {
    error("AuthViewModel not provided")
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)

        MediaManager.init(
            this, hashMapOf(
                "cloud_name" to "dgniomynr",
                "secure" to true
            )
        )

        super.onCreate(savedInstanceState)
        installSplashScreen()

        authViewModel.checkAuth()

        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val themeState = themeViewModel.theme.collectAsState()
            val isDark = when(themeState.value) {
                ThemeMode.LIGHT -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    false
                }

                ThemeMode.DARK -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    true
                }

                ThemeMode.SYSTEM -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    isSystemInDarkTheme()
                }
            }

            val isCheckingAuth by authViewModel.isCheckingAuth.collectAsState()
            val authUser by authViewModel.user.collectAsState()

            CompositionLocalProvider(LocalAuthViewModel provides authViewModel) {
                WorkNestAppTheme(dynamicColor = false, darkTheme = isDark) {
                    ApplySystemBarTheme(isDark)
                    if (isCheckingAuth) LoadingScreen()
                    else MainLayout(startDestination = if (authUser != null) Screen.Home.route else Screen.Login.route)
                }
            }
        }
    }
}
