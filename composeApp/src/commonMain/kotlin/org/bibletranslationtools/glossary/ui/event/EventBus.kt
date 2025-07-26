package org.bibletranslationtools.glossary.ui.event

import kotlinx.coroutines.channels.Channel
import org.bibletranslationtools.glossary.data.RefOption

sealed class AppEvent {
    data class OpenRef(val ref: RefOption) : AppEvent()
    data object Idle : AppEvent()
}

object EventBus {
    val events = Channel<AppEvent>(Channel.CONFLATED)
}