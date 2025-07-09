package com.apcs.worknestapp.domain.logic

import android.util.Patterns
import kotlin.text.contains
import kotlin.text.isNotBlank
import kotlin.text.isNotEmpty
import kotlin.text.trim

object Validator {
    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isUserName(name: String): Boolean {
        return name.isNotBlank() && name.trim().isNotEmpty()
    }

    fun isStrongPassword(password: String): Boolean {
        if (password.contains(" ")) return false
        if (password.length < 12) return false
        if (!password.contains(Regex("[A-Z]"))) return false
        if (!password.contains(Regex("[a-z]"))) return false
        if (!password.contains(Regex("[0-9]"))) return false
        if (!password.contains(Regex("[^a-zA-Z0-9]"))) return false
        return true
    }
}
