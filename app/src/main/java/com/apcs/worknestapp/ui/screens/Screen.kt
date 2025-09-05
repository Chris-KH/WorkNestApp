package com.apcs.worknestapp.ui.screens

sealed class Screen(
    val route: String,
    val title: String,
) {
    object Login : Screen(route = "login", title = "Login")
    object SignUp : Screen(route = "signup", title = "Sign Up")
    object Home : Screen(route = "home", title = "Home")
    object Note : Screen(route = "note", title = "Notes")
    object Contact : Screen(route = "contact", title = "Contacts")
    object AddContact : Screen(route = "add-contact", title = "Add Contacts")
    object Notification : Screen(route = "notification", title = "Notification")
    object MyProfile : Screen(route = "my-profile", title = "Profile")
    object UserProfile : Screen(route = "user-profile/{userId}", title = "User Profile")
    object EditProfile : Screen(route = "edit-profile", title = "Edit Profile")
    object EditProfileDetail : Screen(route = "edit-profile-detail/{field}", title = "Edit Details")
    object Setting : Screen(route = "setting", title = "Settings")
    object SettingDetail : Screen(route = "setting-detail/{field}", title = "Setting Details")
    object SettingAccount : Screen(route = "setting/account/{field}", title = "Setting Account")
    object NoteDetail : Screen(route = "note-detail/{noteId}", title = "Note Detail")
    object Chat : Screen(route = "chat/{conservationId}", title = "Chat")
    object BoardNoteDetail : Screen("board_note_detail", "Board Note Detail")
}
