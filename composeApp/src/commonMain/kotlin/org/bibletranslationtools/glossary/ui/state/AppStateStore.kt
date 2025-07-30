package org.bibletranslationtools.glossary.ui.state

interface AppStateStore {
    val resourceStateHolder: ResourceStateHolder
    val glossaryStateHolder: GlossaryStateHolder
    val tabStateHolder: TabStateHolder
}

class AppStateStoreImpl(
    override val resourceStateHolder: ResourceStateHolder,
    override val glossaryStateHolder: GlossaryStateHolder,
    override val tabStateHolder: TabStateHolder
) : AppStateStore