package xyz.harmonyapp.olympusblog.repository.main.profile

import android.util.Log
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import xyz.harmonyapp.olympusblog.api.main.MainService
import xyz.harmonyapp.olympusblog.api.main.dto.ChangePasswordDTO
import xyz.harmonyapp.olympusblog.api.main.responses.CommentResponse
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.models.AccountProperties
import xyz.harmonyapp.olympusblog.models.AuthToken
import xyz.harmonyapp.olympusblog.models.Author
import xyz.harmonyapp.olympusblog.persistence.AccountPropertiesDao
import xyz.harmonyapp.olympusblog.repository.NetworkBoundResource
import xyz.harmonyapp.olympusblog.repository.safeApiCall
import xyz.harmonyapp.olympusblog.session.SessionManager
import xyz.harmonyapp.olympusblog.ui.main.account.state.AccountViewState
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleViewState
import xyz.harmonyapp.olympusblog.ui.main.profile.state.ProfileViewState
import xyz.harmonyapp.olympusblog.ui.main.profile.state.ProfileViewState.*
import xyz.harmonyapp.olympusblog.utils.*
import xyz.harmonyapp.olympusblog.utils.SuccessHandling.Companion.RESPONSE_ACCOUNT_UPDATE_SUCCESS
import xyz.harmonyapp.olympusblog.utils.SuccessHandling.Companion.RESPONSE_PASSWORD_UPDATE_SUCCESS
import javax.inject.Inject

@MainScope
class ProfileRepositoryImpl
@Inject
constructor(
    val mainService: MainService,
    val sessionManager: SessionManager
) : ProfileRepository {

    private val TAG: String = "AppDebug"

    override fun searchProfiles(
        query: String,
        stateEvent: StateEvent
    ) = flow {
        val apiResult = safeApiCall(IO) {
            mainService.searchProfiles(query)
        }
        emit(
            object : ApiResponseHandler<ProfileViewState, List<Author>>(
                response = apiResult,
                stateEvent = stateEvent
            ) {
                override suspend fun handleSuccess(
                    resultObj: List<Author>
                ): DataState<ProfileViewState> {

                    Log.d(TAG, "Profiles: $resultObj")

                    return DataState.data(
                        response = null,
                        data = ProfileViewState(
                            profileFields = ProfileFields(
                                profileList = resultObj
                            )
                        ),
                        stateEvent = stateEvent
                    )
                }
            }.getResult()
        )
    }

    override fun toggleFollow(
        author: Author,
        stateEvent: StateEvent
    ) = flow {
        val apiResult = safeApiCall(IO) {
            if (author.following) {
                mainService.unfollowUser(author.username)
            } else {
                mainService.followUser(author.username)
            }
        }
        emit(
            object : ApiResponseHandler<ProfileViewState, Author>(
                response = apiResult,
                stateEvent = stateEvent
            ) {
                override suspend fun handleSuccess(
                    resultObj: Author
                ): DataState<ProfileViewState> {

                    Log.d(TAG, "Profiles: $resultObj")

                    return DataState.data(
                        response = null,
                        data = ProfileViewState(
                            viewProfileFields = ViewProfileFields(
                                profile = resultObj
                            )
                        ),
                        stateEvent = stateEvent
                    )
                }
            }.getResult()
        )
    }
}
