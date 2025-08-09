package com.apcs.worknestapp.data.remote.message

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor() : MessageRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _messageMap = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    override val messageMap: StateFlow<Map<String, List<Message>>> = _messageMap
}
