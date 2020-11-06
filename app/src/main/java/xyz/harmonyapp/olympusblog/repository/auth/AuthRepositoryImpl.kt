package xyz.harmonyapp.olympusblog.repository.auth

import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import xyz.harmonyapp.olympusblog.api.auth.AuthService
import xyz.harmonyapp.olympusblog.api.auth.dto.LoginDTO
import xyz.harmonyapp.olympusblog.api.auth.dto.RegisterDTO
import xyz.harmonyapp.olympusblog.di.auth.AuthScope
import xyz.harmonyapp.olympusblog.models.AccountProperties
import xyz.harmonyapp.olympusblog.models.AuthToken
import xyz.harmonyapp.olympusblog.persistence.AccountPropertiesDao
import xyz.harmonyapp.olympusblog.persistence.AuthTokenDao
import xyz.harmonyapp.olympusblog.repository.buildError
import xyz.harmonyapp.olympusblog.repository.safeApiCall
import xyz.harmonyapp.olympusblog.repository.safeCacheCall
import xyz.harmonyapp.olympusblog.session.SessionManager
import xyz.harmonyapp.olympusblog.ui.auth.state.AuthViewState
import xyz.harmonyapp.olympusblog.ui.auth.state.LoginFields
import xyz.harmonyapp.olympusblog.ui.auth.state.RegistrationFields
import xyz.harmonyapp.olympusblog.utils.*
import xyz.harmonyapp.olympusblog.utils.ErrorHandling.Companion.ERROR_SAVE_ACCOUNT_PROPERTIES
import xyz.harmonyapp.olympusblog.utils.ErrorHandling.Companion.ERROR_SAVE_AUTH_TOKEN
import xyz.harmonyapp.olympusblog.utils.SuccessHandling.Companion.RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE
import javax.inject.Inject
import kotlin.random.Random

@AuthScope
class AuthRepositoryImpl
@Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val authService: AuthService,
    val sessionManager: SessionManager,
    val sharedPreferences: SharedPreferences,
    val sharedPrefsEditor: SharedPreferences.Editor
) : AuthRepository {

    private val TAG: String = "AppDebug"
    private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    override fun attemptLogin(
        stateEvent: StateEvent,
        email: String,
        password: String
    ): Flow<DataState<AuthViewState>> = flow {

        val loginFieldErrors = LoginFields(email, password).isValidForLogin()
        if (loginFieldErrors == LoginFields.LoginError.none()) {
            val apiResult = safeApiCall(IO) {
                authService.login(LoginDTO(email, password))
            }
            emit(
                object : ApiResponseHandler<AuthViewState, AccountProperties>(
                    response = apiResult,
                    stateEvent = stateEvent
                ) {
                    override suspend fun handleSuccess(resultObj: AccountProperties): DataState<AuthViewState> {
                        accountPropertiesDao.insertOrIgnore(
                            resultObj
                        )

                        // will return -1 if failure
                        val authToken = AuthToken(
                            resultObj.id,
                            generateRandomToken()
                        )
                        val result = authTokenDao.insert(authToken)
                        if (result < 0) {
                            return DataState.error(
                                response = Response(
                                    ERROR_SAVE_AUTH_TOKEN,
                                    UIComponentType.Dialog(),
                                    MessageType.Error()
                                ),
                                stateEvent = stateEvent
                            )
                        }
                        saveAuthenticatedUserToPrefs(email)

                        return DataState.data(
                            data = AuthViewState(
                                authToken = authToken
                            ),
                            stateEvent = stateEvent,
                            response = null
                        )
                    }

                }.getResult()
            )
        } else {
            Log.d(TAG, "emitting error: ${loginFieldErrors}")
            emit(
                buildError<AuthViewState>(
                    loginFieldErrors,
                    UIComponentType.Dialog(),
                    stateEvent
                )
            )
        }
    }

    override fun attemptRegistration(
        stateEvent: StateEvent,
        email: String,
        username: String,
        password: String,
    ): Flow<DataState<AuthViewState>> = flow {
        val registrationFieldErrors =
            RegistrationFields(email, username, password).isValidForRegistration()
        if (registrationFieldErrors == RegistrationFields.RegistrationError.none()) {

            val apiResult = safeApiCall(IO) {
                authService.register(
                    RegisterDTO(
                        email,
                        username,
                        password,
                    )
                )
            }
            emit(
                object : ApiResponseHandler<AuthViewState, AccountProperties>(
                    response = apiResult,
                    stateEvent = stateEvent
                ) {
                    override suspend fun handleSuccess(resultObj: AccountProperties): DataState<AuthViewState> {
                        val result1 = accountPropertiesDao.insertAndReplace(
                            resultObj
                        )
                        // will return -1 if failure
                        if (result1 < 0) {
                            return DataState.error(
                                response = Response(
                                    ERROR_SAVE_ACCOUNT_PROPERTIES,
                                    UIComponentType.Dialog(),
                                    MessageType.Error()
                                ),
                                stateEvent = stateEvent
                            )
                        }

                        // will return -1 if failure
                        val authToken = AuthToken(
                            resultObj.id,
                            generateRandomToken()
                        )
                        val result2 = authTokenDao.insert(authToken)
                        if (result2 < 0) {
                            return DataState.error(
                                response = Response(
                                    ERROR_SAVE_AUTH_TOKEN,
                                    UIComponentType.Dialog(),
                                    MessageType.Error()
                                ),
                                stateEvent = stateEvent
                            )
                        }
                        saveAuthenticatedUserToPrefs(email)
                        return DataState.data(
                            data = AuthViewState(
                                authToken = authToken
                            ),
                            stateEvent = stateEvent,
                            response = null
                        )
                    }
                }.getResult()
            )

        } else {
            emit(
                buildError<AuthViewState>(
                    registrationFieldErrors,
                    UIComponentType.Dialog(),
                    stateEvent
                )
            )
        }

    }


    override fun checkPreviousAuthUser(
        stateEvent: StateEvent
    ): Flow<DataState<AuthViewState>> = flow {
        Log.d(TAG, "checkPreviousAuthUser: ")
        val previousAuthUserEmail: String? =
            sharedPreferences.getString(PreferenceKeys.PREVIOUS_AUTH_USER, null)

        if (previousAuthUserEmail.isNullOrBlank()) {
            Log.d(TAG, "checkPreviousAuthUser: No previously authenticated user found.")
            emit(returnNoTokenFound(stateEvent))
        } else {
            val apiResult = safeCacheCall(IO) {
                accountPropertiesDao.searchByEmail(previousAuthUserEmail)
            }
            emit(
                object : CacheResponseHandler<AuthViewState, AccountProperties>(
                    response = apiResult,
                    stateEvent = stateEvent
                ) {
                    override suspend fun handleSuccess(resultObj: AccountProperties): DataState<AuthViewState> {

                        if (resultObj.id > -1) {
                            authTokenDao.searchById(resultObj.id).let { authToken ->
                                if (authToken != null) {
                                    if (authToken.token != null) {
                                        return DataState.data(
                                            data = AuthViewState(
                                                authToken = authToken
                                            ),
                                            response = null,
                                            stateEvent = stateEvent
                                        )
                                    }
                                }
                            }
                        }
                        Log.d(TAG, "createCacheRequestAndReturn: AuthToken not found...")
                        return DataState.error(
                            response = Response(
                                RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE,
                                UIComponentType.Dialog(),
                                MessageType.Error()
                            ),
                            stateEvent = stateEvent
                        )
                    }
                }.getResult()
            )
        }
    }

    override fun saveAuthenticatedUserToPrefs(email: String) {
        sharedPrefsEditor.putString(PreferenceKeys.PREVIOUS_AUTH_USER, email)
        sharedPrefsEditor.apply()
    }

    override fun returnNoTokenFound(
        stateEvent: StateEvent
    ): DataState<AuthViewState> {

        return DataState.error(
            response = Response(
                RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE,
                UIComponentType.None(),
                MessageType.Error()
            ),
            stateEvent = stateEvent
        )
    }


    private fun generateRandomToken(): String {
        return (1..16)
            .map { Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }

}

