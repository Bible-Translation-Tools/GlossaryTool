package org.bibletranslationtools.glossary.ui.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.bibletranslationtools.glossary.data.Resource

data class ResourceState(
    val resource: Resource? = null
)

interface ResourceStateHolder {
    val state: StateFlow<ResourceState>
    fun setResource(resource: Resource)
}

class ResourceStateHolderImpl : ResourceStateHolder {
    private val _state = MutableStateFlow(ResourceState())
    override val state: StateFlow<ResourceState> = _state

    override fun setResource(resource: Resource) {
        _state.update { current ->
            current.copy(resource = resource)
        }
    }
}
