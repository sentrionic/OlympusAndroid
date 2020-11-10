package xyz.harmonyapp.olympusblog.ui.main.profile.state

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.models.Author
import xyz.harmonyapp.olympusblog.repository.main.profile.ProfileRepositoryImpl
import xyz.harmonyapp.olympusblog.session.SessionManager
import xyz.harmonyapp.olympusblog.ui.BaseViewModel
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleStateEvent
import xyz.harmonyapp.olympusblog.ui.main.article.viewmodel.getDummyAuthor
import xyz.harmonyapp.olympusblog.ui.main.profile.state.ProfileStateEvent.ProfileSearchEvent
import xyz.harmonyapp.olympusblog.ui.main.profile.state.ProfileStateEvent.ToggleFollowEvent
import xyz.harmonyapp.olympusblog.utils.*
import xyz.harmonyapp.olympusblog.utils.ErrorHandling.Companion.INVALID_STATE_EVENT
import javax.inject.Inject

@MainScope
class ProfileViewModel
@Inject
constructor(
    private val sessionManager: SessionManager,
    private val profileRepository: ProfileRepositoryImpl,
) : BaseViewModel<ProfileViewState>() {

    override fun handleNewData(data: ProfileViewState) {
        data.profileFields.let { profileFields ->

            profileFields.profileList?.let { profileList ->
                setProfileListData(profileList)
            }
        }

        data.viewProfileFields.let { viewProfileFields ->
            viewProfileFields.profile?.let { profile ->
                setProfile(profile)
            }
        }

    }

    override fun setStateEvent(stateEvent: StateEvent) {
        if (!isJobAlreadyActive(stateEvent)) {
            val job: Flow<DataState<ProfileViewState>> = when (stateEvent) {

                is ProfileSearchEvent -> {
                    profileRepository.searchProfiles(query = getQuery(), stateEvent = stateEvent)
                }

                is ToggleFollowEvent -> {
                    profileRepository.toggleFollow(author = getAuthor(), stateEvent = stateEvent)
                }

                else -> {
                    flow {
                        emit(
                            DataState.error<ProfileViewState>(
                                response = Response(
                                    message = INVALID_STATE_EVENT,
                                    uiComponentType = UIComponentType.None(),
                                    messageType = MessageType.Error()
                                ),
                                stateEvent = stateEvent
                            )
                        )
                    }
                }
            }
            launchJob(stateEvent, job)
        }
    }

    fun setProfile(author: Author) {
        val update = getCurrentViewStateOrNew()
        update.viewProfileFields.profile = author
        setViewState(update)
    }

    fun getAuthor(): Author {
        return getCurrentViewStateOrNew().viewProfileFields.profile ?: getDummyAuthor()
    }

    fun setQuery(query: String) {
        val update = getCurrentViewStateOrNew()
        update.profileFields.searchQuery = query
        setViewState(update)
    }

    fun getQuery(): String {
        return getCurrentViewStateOrNew().profileFields.searchQuery ?: ""
    }

    private fun setProfileListData(profileList: List<Author>) {
        val update = getCurrentViewStateOrNew()
        update.profileFields.profileList = profileList
        setViewState(update)
    }

    fun loadProfiles() {
        if (!isJobAlreadyActive(ProfileSearchEvent())) {
            setStateEvent(ProfileSearchEvent())
        }
    }

    fun getCurrentUserId(): Int {
        return sessionManager.cachedToken.value?.account_id?: -1
    }

    override fun initNewViewState(): ProfileViewState {
        return ProfileViewState()
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}