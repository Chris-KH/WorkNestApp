package com.apcs.worknestapp.ui.screens.edit_profile_detail

enum class EditProfileField(val fieldName: String) {
    NAME("Name"),
    PRONOUNS("Pronouns"),
    BIO("Bio");

    companion object {
        fun fromRoute(fieldName: String?): EditProfileField? =
            entries.find { it.fieldName == fieldName }
    }
}
