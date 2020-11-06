package xyz.harmonyapp.olympusblog.repository.auth

import kotlinx.coroutines.flow.Flow
import xyz.harmonyapp.olympusblog.di.auth.AuthScope
import xyz.harmonyapp.olympusblog.ui.auth.state.AuthViewState
import xyz.harmonyapp.olympusblog.utils.DataState
import xyz.harmonyapp.olympusblog.utils.StateEvent

@AuthScope
interface AuthRepository {

    fun attemptLogin(
        stateEvent: StateEvent,
        email: String,
        password: String
    ): Flow<DataState<AuthViewState>>

    fun attemptRegistration(
        stateEvent: StateEvent,
        email: String,
        username: String,
        password: String,
    ): Flow<DataState<AuthViewState>>

    fun checkPreviousAuthUser(
        stateEvent: StateEvent
    ): Flow<DataState<AuthViewState>>

    fun saveAuthenticatedUserToPrefs(email: String)

    fun returnNoTokenFound(
        stateEvent: StateEvent
    ): DataState<AuthViewState>

}