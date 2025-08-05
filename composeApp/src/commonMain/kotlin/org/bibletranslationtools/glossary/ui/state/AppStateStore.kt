package org.bibletranslationtools.glossary.ui.state

interface AppStateStore {
    val resourceStateHolder: ResourceStateHolder
    val glossaryStateHolder: GlossaryStateHolder
}

class AppStateStoreImpl(
    override val resourceStateHolder: ResourceStateHolder,
    override val glossaryStateHolder: GlossaryStateHolder,
) : AppStateStore