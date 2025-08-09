package com.apcs.worknestapp.data.remote.message

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    messageRepo: MessageRepository,
) : ViewModel() {
    val messageMap = messageRepo.messageMap

}
