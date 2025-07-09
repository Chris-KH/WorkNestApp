package com.apcs.worknestapp.ui.screens

import kotlin.collections.find
import kotlin.text.startsWith
import kotlin.text.substringBefore

sealed class Screen(
    val route: String,
    val title: String,
    val showTopBar: Boolean = false,
    val showBottomBar: Boolean = false,
) {
    object Login : Screen(route = "login", title = "Login")
    object SignUp : Screen(route = "signup", title = "Sign Up")
    object Home : Screen(route = "home", title = "Home", showTopBar = true, showBottomBar = true)

    object Profile : Screen(route = "profile", title = "Profile", showTopBar = true)


    companion object {
        val all = listOf(
            Login,
            SignUp,
            Home,
            Profile,
        )

        fun fromRoute(route: String?): Screen? {
            return all.find {
                val base = it.route.substringBefore("/{")
                route?.startsWith(base) == true
            }
        }
    }
}
