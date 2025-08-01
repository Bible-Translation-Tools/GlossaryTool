package org.bibletranslationtools.glossary.ui.event

import kotlinx.coroutines.channels.Channel
import org.bibletranslationtools.glossary.data.Glossary
import org.bibletranslationtools.glossary.data.RefOption
import org.bibletranslationtools.glossary.data.Resource

sealed class AppEvent {
    data class OpenRef(val ref: RefOption) : AppEvent()
    data class SelectGlossary(val glossary: Glossary) : AppEvent()
    data class SelectResource(val resource: Resource) : AppEvent()
    data object Idle : AppEvent()
}

/**
 * Single-subscriber event bus
 */
object EventBus {
    val events = Channel<AppEvent>(10)
}