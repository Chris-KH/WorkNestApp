package com.apcs.worknestapp.ui.screens

sealed class Screen(
    val route: String,
    val title: String,
) {
    object Login : Screen(route = "login", title = "Login")
    object SignUp : Screen(route = "signup", title = "Sign Up")
    object Home : Screen(route = "home", title = "Home")
    object Note : Screen(route = "note", title = "Notes")
    object Contact : Screen(route = "contact", "Contacts")
    object Notification : Screen(route = "notification", title = "Notifications")
    object Profile : Screen(route = "profile", title = "Profile")
    object EditProfile : Screen(route = "edit-profile", title = "Edit Profile")
    object EditProfileDetail : Screen(route = "edit-profile-detail/{field}", title = "Edit Details")
    object Setting : Screen(route = "setting", title = "Settings")
    object SettingDetail : Screen(route = "setting-detail/{field}", title = "Setting Details")
    object SettingAccount : Screen(route = "setting/account/{field}", title = "Setting Account")
    object NoteDetail: Screen(route = "note-detail/{noteId}", title = "Note Detail")
    companion object {
        val all: List<Screen> by lazy {
            listOf(
                Login,
                SignUp,
                Home,
                Note,
                Contact,
                Notification,
                Profile,
                EditProfile,
                EditProfileDetail,
                Setting,
                SettingDetail,
                SettingAccount,
                NoteDetail
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
