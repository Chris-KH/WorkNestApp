package com.apcs.worknestapp.ui.screens.setting


enum class SettingField(val fieldName: String) {
    ACCOUNT("Account"),
    THEME("Theme"),
    LANGUAGE("Language"),
    NOTIFICATION("Notification"),
    ABOUT("About");

    companion object {
        fun fromRoute(fieldName: String?): SettingField? =
            SettingField.entries.find { it.fieldName == fieldName }
    }
}
