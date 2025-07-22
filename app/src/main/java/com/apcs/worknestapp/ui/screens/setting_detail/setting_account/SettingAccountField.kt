package com.apcs.worknestapp.ui.screens.setting_detail.setting_account

enum class SettingAccountField(
    val fieldName: String,
) {
    NAME("Name"),
    EMAIL("Email"),
    PHONE("Phone"),
    ADDRESS("Address");

    companion object {
        fun fromRoute(fieldName: String?): SettingAccountField? =
            SettingAccountField.entries.find { it.fieldName == fieldName }
    }
}
