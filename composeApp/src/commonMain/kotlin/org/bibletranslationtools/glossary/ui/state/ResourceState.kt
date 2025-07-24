package org.bibletranslationtools.glossary.ui.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.bibletranslationtools.glossary.data.Resource

data class ResourceState(
    val resource: Resource? = null
)

interface ResourceStateHolder {
    val resourceState: StateFlow<ResourceState>
    fun updateResource(resource: Resource)
}

class ResourceStateHolderImpl : ResourceStateHolder {
    private val _resourceState = MutableStateFlow(ResourceState())
    override val resourceState: StateFlow<ResourceState> = _resourceState

    override fun updateResource(resource: Resource) {
        _resourceState.update { current ->
            current.copy(resource = resource)
        }
    }
}
