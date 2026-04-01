package org.bibletranslationtools.glossary.ui.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.bibletranslationtools.glossary.data.api.User

data class UserState(
    val user: User? = null
)

interface UserStateHolder {
    val state: StateFlow<UserState>
    fun setUser(user: User?)
}

class UserStateHolderImpl : UserStateHolder {
    private val _state = MutableStateFlow(UserState())
    override val state: StateFlow<UserState> = _state

    override fun setUser(user: User?) {
        _state.update { current ->
            current.copy(user = user)
        }
    }
}