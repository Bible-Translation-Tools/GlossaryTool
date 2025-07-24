package org.bibletranslationtools.glossary.ui.state

interface AppStateStore {
    val resourceStateHolder: ResourceStateHolder
    val tabStateHolder: TabStateHolder
}

class AppStateStoreImpl(
    override val resourceStateHolder: ResourceStateHolder,
    override val tabStateHolder: TabStateHolder
) : AppStateStore