package xyz.harmonyapp.olympusblog.repository.main.profile

import kotlinx.coroutines.flow.Flow
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.models.Author
import xyz.harmonyapp.olympusblog.ui.main.profile.state.ProfileViewState
import xyz.harmonyapp.olympusblog.utils.DataState
import xyz.harmonyapp.olympusblog.utils.StateEvent

@MainScope
interface ProfileRepository {

    fun searchProfiles(
        query: String,
        stateEvent: StateEvent
    ): Flow<DataState<ProfileViewState>>

    fun toggleFollow(
        author: Author,
        stateEvent: StateEvent
    ): Flow<DataState<ProfileViewState>>

    fun getAuthorStories(
        author: Author,
        stateEvent: StateEvent
    ): Flow<DataState<ProfileViewState>>

    fun getAuthorFavorites(
        author: Author,
        stateEvent: StateEvent
    ): Flow<DataState<ProfileViewState>>
}