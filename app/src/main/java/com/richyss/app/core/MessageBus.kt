package com.richyss.app.core

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * One-shot user messages (snackbars) shared by every ViewModel.
 * Buffered so a message emitted before the host is composed is not lost.
 */
class MessageBus {
    private val channel = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val messages: SharedFlow<String> = channel.asSharedFlow()

    fun show(message: String) {
        channel.tryEmit(message)
    }
}
