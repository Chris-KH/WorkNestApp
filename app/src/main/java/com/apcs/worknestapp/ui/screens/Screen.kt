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
    object Note : Screen(route = "note", title = "Notes")
    object Notification : Screen(route = "notification", title = "Notifications")
    object Profile : Screen(route = "profile", title = "Profile")
    object EditProfile : Screen(route = "edit-profile", title = "Edit Profile")
    object Setting : Screen(route = "setting", title = "Settings")

    companion object {
        val all: List<Screen> by lazy {
            listOf(
                Login,
                SignUp,
                Home,
                Note,
                Notification,
                Profile,
                EditProfile,
                Setting,
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
