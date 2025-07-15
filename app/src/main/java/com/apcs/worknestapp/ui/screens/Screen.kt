package com.apcs.worknestapp.ui.screens

import android.util.Log
import kotlin.collections.find
import kotlin.text.startsWith
import kotlin.text.substringBefore

sealed class Screen(
    val route: String,
    val title: String,
) {
    object Login : Screen(route = "login", title = "Login")
    object SignUp : Screen(route = "signup", title = "Sign Up")
    object Home : Screen(route = "home", title = "Home")
    object NotificationScreen : Screen(route = "notification", title = "Notifications")
    object Profile : Screen(route = "profile", title = "Profile")

    companion object {
        val all: List<Screen> by lazy {
            listOf(
                Login,
                SignUp,
                Home,
                Profile,
                NotificationScreen,
            )
        }

        fun fromRoute(route: String?): Screen? {
            return all.find {
                val base = it.route.substringBefore("/{")
                route?.startsWith(base) == true
            }
        }
    }
}
