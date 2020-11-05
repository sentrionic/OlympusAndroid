package xyz.harmonyapp.olympusblog.repository.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import xyz.harmonyapp.olympusblog.api.main.MainService
import xyz.harmonyapp.olympusblog.api.main.dto.ChangePasswordDTO
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.models.AccountProperties
import xyz.harmonyapp.olympusblog.models.AuthToken
import xyz.harmonyapp.olympusblog.persistence.AccountPropertiesDao
import xyz.harmonyapp.olympusblog.repository.JobManager
import xyz.harmonyapp.olympusblog.repository.NetworkBoundResource
import xyz.harmonyapp.olympusblog.session.SessionManager
import xyz.harmonyapp.olympusblog.ui.DataState
import xyz.harmonyapp.olympusblog.ui.Response
import xyz.harmonyapp.olympusblog.ui.ResponseType
import xyz.harmonyapp.olympusblog.ui.main.account.state.AccountViewState
import xyz.harmonyapp.olympusblog.utils.AbsentLiveData
import xyz.harmonyapp.olympusblog.utils.ApiSuccessResponse
import xyz.harmonyapp.olympusblog.utils.GenericApiResponse
import xyz.harmonyapp.olympusblog.utils.SuccessHandling.Companion.RESPONSE_PASSWORD_UPDATE_SUCCESS
import javax.inject.Inject

@MainScope
class AccountRepository
@Inject
constructor(
    val mainService: MainService,
    val accountPropertiesDao: AccountPropertiesDao,
    val sessionManager: SessionManager
) : JobManager("AccountRepository") {
    private val TAG: String = "AppDebug"

    fun getAccountProperties(authToken: AuthToken): LiveData<DataState<AccountViewState>> {
        return object :
            NetworkBoundResource<AccountProperties, AccountProperties, AccountViewState>(
                sessionManager.isConnectedToTheInternet(),
                true,
                false,
                true,
            ) {

            // if network is down, view the cache and return
            override suspend fun createCacheRequestAndReturn() {
                withContext(Dispatchers.Main) {

                    // finishing by viewing db cache
                    result.addSource(loadFromCache()) { viewState ->
                        onCompleteJob(DataState.data(viewState, null))
                    }
                }
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<AccountProperties>) {
                updateLocalDb(response.body)
                createCacheRequestAndReturn()
            }

            override fun loadFromCache(): LiveData<AccountViewState> {
                return accountPropertiesDao.searchById(authToken.account_id!!)
                    .switchMap {
                        object : LiveData<AccountViewState>() {
                            override fun onActive() {
                                super.onActive()
                                value = AccountViewState(it)
                            }
                        }
                    }
            }

            override fun createCall(): LiveData<GenericApiResponse<AccountProperties>> {
                return mainService.getAccountProperties()
            }

            override suspend fun updateLocalDb(accountProp: AccountProperties?) {
                accountProp?.let {
                    accountPropertiesDao.updateAccountProperties(
                        accountProp.id,
                        accountProp.email,
                        accountProp.username,
                        accountProp.bio,
                        accountProp.image
                    )
                }
            }

            override fun setJob(job: Job) {
                addJob("getAccountProperties", job)
            }


        }.asLiveData()
    }

    fun saveAccountProperties(
        accountProperties: AccountProperties,
        image: MultipartBody.Part?
    ): LiveData<DataState<AccountViewState>> {
        return object :
            NetworkBoundResource<AccountProperties, AccountProperties, AccountViewState>(
                sessionManager.isConnectedToTheInternet(),
                true,
                true,
                false
            ) {

            // not applicable
            override suspend fun createCacheRequestAndReturn() {

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<AccountProperties>) {
                updateLocalDb(response.body)

                withContext(Dispatchers.Main) {
                    // finish with success response
                    onCompleteJob(
                        DataState.data(
                            data = null,
                            response = Response("Update Success", ResponseType.Toast())
                        )
                    )
                }
            }

            // not used in this case
            override fun loadFromCache(): LiveData<AccountViewState> {
                return AbsentLiveData.create()
            }

            override fun createCall(): LiveData<GenericApiResponse<AccountProperties>> {
                val email =
                    RequestBody.create(MediaType.parse("text/plain"), accountProperties.email)
                val username =
                    RequestBody.create(MediaType.parse("text/plain"), accountProperties.username)
                val bio = RequestBody.create(MediaType.parse("text/plain"), accountProperties.bio)
                return mainService.saveAccountProperties(
                    email,
                    username,
                    bio,
                    image
                )
            }

            override suspend fun updateLocalDb(accountProperties: AccountProperties?) {
                accountProperties?.let {
                    accountPropertiesDao.updateAccountProperties(
                        accountProperties.id,
                        accountProperties.email,
                        accountProperties.username,
                        accountProperties.bio,
                        accountProperties.image,
                    )
                }
            }

            override fun setJob(job: Job) {
                addJob("saveAccountProperties", job)
            }

        }.asLiveData()
    }

    fun updatePassword(
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ): LiveData<DataState<AccountViewState>> {
        return object : NetworkBoundResource<AccountProperties, Any, AccountViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ) {

            // not applicable
            override suspend fun createCacheRequestAndReturn() {

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<AccountProperties>) {
                withContext(Dispatchers.Main) {
                    // finish with success response
                    onCompleteJob(
                        DataState.data(
                            null,
                            Response(RESPONSE_PASSWORD_UPDATE_SUCCESS, ResponseType.Toast())
                        )
                    )
                }
            }

            // not used in this case
            override fun loadFromCache(): LiveData<AccountViewState> {
                return AbsentLiveData.create()
            }

            override fun createCall(): LiveData<GenericApiResponse<AccountProperties>> {
                return mainService.updatePassword(
                    ChangePasswordDTO(
                        currentPassword,
                        newPassword,
                        confirmNewPassword
                    )
                )
            }

            // not used in this case
            override suspend fun updateLocalDb(cacheObject: Any?) {
            }

            override fun setJob(job: Job) {
                addJob("updatePassword", job)
            }

        }.asLiveData()
    }

}