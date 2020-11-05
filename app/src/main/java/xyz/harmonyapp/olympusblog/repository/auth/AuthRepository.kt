package xyz.harmonyapp.olympusblog.repository.auth

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Job
import xyz.harmonyapp.olympusblog.api.auth.AuthService
import xyz.harmonyapp.olympusblog.api.auth.dto.LoginDTO
import xyz.harmonyapp.olympusblog.api.auth.dto.RegisterDTO
import xyz.harmonyapp.olympusblog.di.auth.AuthScope
import xyz.harmonyapp.olympusblog.models.AccountProperties
import xyz.harmonyapp.olympusblog.models.AuthToken
import xyz.harmonyapp.olympusblog.persistence.AccountPropertiesDao
import xyz.harmonyapp.olympusblog.persistence.AuthTokenDao
import xyz.harmonyapp.olympusblog.repository.JobManager
import xyz.harmonyapp.olympusblog.repository.NetworkBoundResource
import xyz.harmonyapp.olympusblog.session.SessionManager
import xyz.harmonyapp.olympusblog.ui.DataState
import xyz.harmonyapp.olympusblog.ui.Response
import xyz.harmonyapp.olympusblog.ui.ResponseType
import xyz.harmonyapp.olympusblog.ui.auth.state.AuthViewState
import xyz.harmonyapp.olympusblog.ui.auth.state.LoginFields
import xyz.harmonyapp.olympusblog.ui.auth.state.RegistrationFields
import xyz.harmonyapp.olympusblog.utils.AbsentLiveData
import xyz.harmonyapp.olympusblog.utils.ApiSuccessResponse
import xyz.harmonyapp.olympusblog.utils.ErrorHandling.Companion.ERROR_SAVE_ACCOUNT_PROPERTIES
import xyz.harmonyapp.olympusblog.utils.ErrorHandling.Companion.ERROR_SAVE_AUTH_TOKEN
import xyz.harmonyapp.olympusblog.utils.GenericApiResponse
import xyz.harmonyapp.olympusblog.utils.PreferenceKeys
import xyz.harmonyapp.olympusblog.utils.SuccessHandling.Companion.RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE
import javax.inject.Inject
import kotlin.random.Random

@AuthScope
class AuthRepository
@Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val authService: AuthService,
    val sessionManager: SessionManager,
    val sharedPreferences: SharedPreferences,
    val sharedPrefsEditor: SharedPreferences.Editor
): JobManager("AuthRepository") {
    private val TAG: String = "AppDebug"

    private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    fun attemptLogin(email: String, password: String): LiveData<DataState<AuthViewState>> {

        val loginFieldErrors = LoginFields(email, password).isValidForLogin()
        if (loginFieldErrors != LoginFields.LoginError.none()) {
            return returnErrorResponse(loginFieldErrors, ResponseType.Dialog())
        }

        return object : NetworkBoundResource<AccountProperties, Any, AuthViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ) {

            // Ignore
            override fun loadFromCache(): LiveData<AuthViewState> {
                return AbsentLiveData.create()
            }

            // Ignore
            override suspend fun updateLocalDb(cacheObject: Any?) {

            }

            override suspend fun createCacheRequestAndReturn() {

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<AccountProperties>) {
                Log.d(TAG, "handleApiSuccessResponse: ${response}")

                accountPropertiesDao.insertOrIgnore(
                    AccountProperties(
                        response.body.id,
                        response.body.email,
                        response.body.username,
                        response.body.bio,
                        response.body.image,
                    )
                )

                // will return -1 if failure
                val result = authTokenDao.insert(
                    AuthToken(
                        response.body.id,
                        generateRandomToken()
                    )
                )
                if (result < 0) {
                    return onCompleteJob(
                        DataState.error(
                            Response(ERROR_SAVE_AUTH_TOKEN, ResponseType.Dialog())
                        )
                    )
                }

                saveAuthenticatedUserToPrefs(email)

                onCompleteJob(
                    DataState.data(
                        data = AuthViewState(
                            authToken = AuthToken(response.body.id, response.body.email)
                        )
                    )
                )
            }

            override fun createCall(): LiveData<GenericApiResponse<AccountProperties>> {
                return authService.login(LoginDTO(email, password))
            }

            override fun setJob(job: Job) {
                addJob("attemptLogin", job)
            }

        }.asLiveData()
    }

    fun attemptRegister(
        email: String,
        username: String,
        password: String
    ): LiveData<DataState<AuthViewState>> {

        val registrationFieldErrors =
            RegistrationFields(email, username, password).isValidForRegistration()

        if (registrationFieldErrors != RegistrationFields.RegistrationError.none()) {
            return returnErrorResponse(registrationFieldErrors, ResponseType.Dialog())
        }

        return object : NetworkBoundResource<AccountProperties, Any, AuthViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ) {

            // Ignore
            override fun loadFromCache(): LiveData<AuthViewState> {
                return AbsentLiveData.create()
            }

            // Ignore
            override suspend fun updateLocalDb(cacheObject: Any?) {

            }

            override suspend fun createCacheRequestAndReturn() {

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<AccountProperties>) {

                Log.d(TAG, "handleApiSuccessResponse: ${response}")

                val result1 = accountPropertiesDao.insertAndReplace(
                    AccountProperties(
                        response.body.id,
                        response.body.email,
                        response.body.username,
                        response.body.bio,
                        response.body.image,
                    )
                )

                // will return -1 if failure
                if (result1 < 0) {
                    onCompleteJob(
                        DataState.error(
                            Response(ERROR_SAVE_ACCOUNT_PROPERTIES, ResponseType.Dialog())
                        )
                    )
                    return
                }

                // will return -1 if failure
                val result2 = authTokenDao.insert(
                    AuthToken(
                        response.body.id,
                        generateRandomToken()
                    )
                )
                if (result2 < 0) {
                    onCompleteJob(
                        DataState.error(
                            Response(ERROR_SAVE_AUTH_TOKEN, ResponseType.Dialog())
                        )
                    )
                    return
                }

                saveAuthenticatedUserToPrefs(email)

                onCompleteJob(
                    DataState.data(
                        data = AuthViewState(
                            authToken = AuthToken(response.body.id, response.body.email)
                        )
                    )
                )
            }

            override fun createCall(): LiveData<GenericApiResponse<AccountProperties>> {
                return authService.register(RegisterDTO(email, username, password))
            }

            override fun setJob(job: Job) {
                addJob("attemptRegistration", job)
            }

        }.asLiveData()
    }

    fun checkPreviousAuthUser(): LiveData<DataState<AuthViewState>> {

        val previousAuthUserEmail: String? =
            sharedPreferences.getString(PreferenceKeys.PREVIOUS_AUTH_USER, null)

        if (previousAuthUserEmail.isNullOrBlank()) {
            Log.d(TAG, "checkPreviousAuthUser: No previously authenticated user found.")
            return returnNoTokenFound()
        } else {
            return object : NetworkBoundResource<Void, Any, AuthViewState>(
                sessionManager.isConnectedToTheInternet(),
                false,
                false,
                false
            ) {

                // Ignore
                override fun loadFromCache(): LiveData<AuthViewState> {
                    return AbsentLiveData.create()
                }

                // Ignore
                override suspend fun updateLocalDb(cacheObject: Any?) {

                }

                override suspend fun createCacheRequestAndReturn() {
                    accountPropertiesDao.searchByEmail(previousAuthUserEmail)
                        .let { accountProperties ->
                            Log.d(
                                TAG,
                                "createCacheRequestAndReturn: searching for token... account properties: ${accountProperties}"
                            )

                            accountProperties?.let {
                                if (accountProperties.id > -1) {
                                    authTokenDao.searchById(accountProperties.id).let { authToken ->
                                        if (authToken != null) {
                                            if (authToken.token != null) {
                                                onCompleteJob(
                                                    DataState.data(
                                                        AuthViewState(authToken = authToken)
                                                    )
                                                )
                                                return
                                            }
                                        }
                                    }
                                }
                            }

                            Log.d(TAG, "createCacheRequestAndReturn: AuthToken not found...")
                            onCompleteJob(
                                DataState.data(
                                    null,
                                    Response(
                                        RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE,
                                        ResponseType.None()
                                    )
                                )
                            )
                        }
                }

                // not used in this case
                override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<Void>) {
                }

                // not used in this case
                override fun createCall(): LiveData<GenericApiResponse<Void>> {
                    return AbsentLiveData.create()
                }

                override fun setJob(job: Job) {
                    addJob("checkPreviousAuthUser", job)
                }


            }.asLiveData()
        }
    }

    private fun saveAuthenticatedUserToPrefs(email: String) {
        sharedPrefsEditor.putString(PreferenceKeys.PREVIOUS_AUTH_USER, email)
        sharedPrefsEditor.apply()
    }

    private fun returnNoTokenFound(): LiveData<DataState<AuthViewState>> {
        return object : LiveData<DataState<AuthViewState>>() {
            override fun onActive() {
                super.onActive()
                value = DataState.data(
                    null,
                    Response(RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE, ResponseType.None())
                )
            }
        }
    }

    private fun returnErrorResponse(
        errorMessage: String,
        responseType: ResponseType
    ): LiveData<DataState<AuthViewState>> {
        Log.d(TAG, "returnErrorResponse: ${errorMessage}")

        return object : LiveData<DataState<AuthViewState>>() {
            override fun onActive() {
                super.onActive()
                value = DataState.error(
                    Response(
                        errorMessage,
                        responseType
                    )
                )
            }
        }
    }

    private fun generateRandomToken(): String {
        return (1..16)
            .map { Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("");
    }

}