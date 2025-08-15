package com.apcs.worknestapp.data.remote.auth

import com.apcs.worknestapp.data.remote.message.MessageRepository
import com.apcs.worknestapp.data.remote.note.NoteRepository
import com.apcs.worknestapp.data.remote.notification.NotificationRepository
import com.apcs.worknestapp.data.remote.user.UserRepository
import javax.inject.Inject

class SessionManager @Inject constructor(
    private val userRepo: UserRepository,
    private val noteRepo: NoteRepository,
    private val notificationRepo: NotificationRepository,
    private val messageRepo: MessageRepository,
) {
    fun signOutAndClearAll() {
        userRepo.clearCache()
        noteRepo.clearCache()
        notificationRepo.clearCache()
        messageRepo.clearCache()
    }
}
