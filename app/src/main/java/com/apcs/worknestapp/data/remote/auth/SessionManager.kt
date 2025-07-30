package com.apcs.worknestapp.data.remote.auth

import com.apcs.worknestapp.data.remote.note.NoteRepository
import com.apcs.worknestapp.data.remote.notification.NotificationRepository
import javax.inject.Inject

class SessionManager @Inject constructor(
    private val noteRepo: NoteRepository,
    private val notificationRepo: NotificationRepository,
) {
    fun signOutAndClearAll() {
        noteRepo.clearCache()
        notificationRepo.clearCache()
    }
}
