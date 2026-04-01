package org.bibletranslationtools.glossary.ui.state

interface AppStateStore {
    val resourceStateHolder: ResourceStateHolder
    val glossaryStateHolder: GlossaryStateHolder
    val userStateHolder: UserStateHolder
}

class AppStateStoreImpl(
    override val resourceStateHolder: ResourceStateHolder,
    override val glossaryStateHolder: GlossaryStateHolder,
    override val userStateHolder: UserStateHolder
) : AppStateStore