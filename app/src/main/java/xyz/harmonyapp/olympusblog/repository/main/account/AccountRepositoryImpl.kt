package xyz.harmonyapp.olympusblog.repository.main.account

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import xyz.harmonyapp.olympusblog.api.main.MainService
import xyz.harmonyapp.olympusblog.api.main.dto.ChangePasswordDTO
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.models.AccountProperties
import xyz.harmonyapp.olympusblog.models.AuthToken
import xyz.harmonyapp.olympusblog.persistence.AccountPropertiesDao
import xyz.harmonyapp.olympusblog.repository.NetworkBoundResource
import xyz.harmonyapp.olympusblog.repository.safeApiCall
import xyz.harmonyapp.olympusblog.session.SessionManager
import xyz.harmonyapp.olympusblog.ui.main.account.state.AccountViewState
import xyz.harmonyapp.olympusblog.utils.*
import xyz.harmonyapp.olympusblog.utils.SuccessHandling.Companion.RESPONSE_ACCOUNT_UPDATE_SUCCESS
import xyz.harmonyapp.olympusblog.utils.SuccessHandling.Companion.RESPONSE_PASSWORD_UPDATE_SUCCESS
import javax.inject.Inject

@MainScope
class AccountRepositoryImpl
@Inject
constructor(
    val mainService: MainService,
    val accountPropertiesDao: AccountPropertiesDao,
    val sessionManager: SessionManager
) : AccountRepository {

    private val TAG: String = "AppDebug"

    override fun getAccountProperties(
        authToken: AuthToken,
        stateEvent: StateEvent
    ): Flow<DataState<AccountViewState>> {
        return object :
            NetworkBoundResource<AccountProperties, AccountProperties, AccountViewState>(
                dispatcher = IO,
                stateEvent = stateEvent,
                apiCall = {
                    mainService.getAccountProperties()
                },
                cacheCall = {
                    accountPropertiesDao.searchById(authToken.account_id!!)
                }

            ) {
            override suspend fun updateCache(networkObject: AccountProperties) {
                accountPropertiesDao.updateAccountProperties(
                    networkObject.id,
                    networkObject.email,
                    networkObject.username,
                    networkObject.bio,
                    networkObject.image
                )
            }

            override fun handleCacheSuccess(
                resultObj: AccountProperties
            ): DataState<AccountViewState> {
                return DataState.data(
                    response = null,
                    data = AccountViewState(
                        accountProperties = resultObj
                    ),
                    stateEvent = stateEvent
                )
            }

        }.result
    }

    override fun saveAccountProperties(
        email: RequestBody,
        username: RequestBody,
        bio: RequestBody,
        image: MultipartBody.Part?,
        stateEvent: StateEvent
    ) = flow {
        val apiResult = safeApiCall(IO) {
            mainService.saveAccountProperties(
                email,
                username,
                bio,
                image
            )
        }
        emit(
            object : ApiResponseHandler<AccountViewState, AccountProperties>(
                response = apiResult,
                stateEvent = stateEvent
            ) {
                override suspend fun handleSuccess(
                    resultObj: AccountProperties
                ): DataState<AccountViewState> {

                    accountPropertiesDao.updateAccountProperties(
                        id = resultObj.id,
                        email = resultObj.email,
                        username = resultObj.username,
                        bio = resultObj.bio,
                        image = resultObj.image
                    )

                    return DataState.data(
                        data = null,
                        response = Response(
                            message = RESPONSE_ACCOUNT_UPDATE_SUCCESS,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Success()
                        ),
                        stateEvent = stateEvent
                    )
                }

            }.getResult()
        )
    }

    override fun updatePassword(
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String,
        stateEvent: StateEvent
    ) = flow {
        val apiResult = safeApiCall(IO) {
            mainService.updatePassword(
                ChangePasswordDTO(
                    currentPassword,
                    newPassword,
                    confirmNewPassword
                )
            )
        }
        emit(
            object : ApiResponseHandler<AccountViewState, AccountProperties>(
                response = apiResult,
                stateEvent = stateEvent
            ) {
                override suspend fun handleSuccess(
                    resultObj: AccountProperties
                ): DataState<AccountViewState> {

                    return DataState.data(
                        data = null,
                        response = Response(
                            message = RESPONSE_PASSWORD_UPDATE_SUCCESS,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Success()
                        ),
                        stateEvent = stateEvent
                    )
                }
            }.getResult()
        )
    }

    override fun logout(
        stateEvent: StateEvent
    ) = flow {
        val apiResult = safeApiCall(IO) {
            mainService.logout()
        }
        emit(
            object : ApiResponseHandler<AccountViewState, Boolean>(
                response = apiResult,
                stateEvent = stateEvent
            ) {
                override suspend fun handleSuccess(
                    resultObj: Boolean
                ): DataState<AccountViewState> {

                    return DataState.data(
                        data = null,
                        response = null,
                        stateEvent = stateEvent
                    )
                }
            }.getResult()
        )
    }

}
