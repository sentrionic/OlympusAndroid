package xyz.harmonyapp.olympusblog.ui.main.account

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.models.AccountProperties
import xyz.harmonyapp.olympusblog.repository.main.account.AccountRepositoryImpl
import xyz.harmonyapp.olympusblog.session.SessionManager
import xyz.harmonyapp.olympusblog.ui.BaseViewModel
import xyz.harmonyapp.olympusblog.ui.main.account.state.AccountStateEvent.*
import xyz.harmonyapp.olympusblog.ui.main.account.state.AccountViewState
import xyz.harmonyapp.olympusblog.utils.*
import xyz.harmonyapp.olympusblog.utils.ErrorHandling.Companion.INVALID_STATE_EVENT
import javax.inject.Inject

@MainScope
class AccountViewModel
@Inject
constructor(
    val sessionManager: SessionManager,
    private val accountRepository: AccountRepositoryImpl
) : BaseViewModel<AccountViewState>() {

    override fun handleNewData(data: AccountViewState) {
        data.accountProperties?.let { accountProperties ->
            setAccountPropertiesData(accountProperties)
        }
    }

    override fun setStateEvent(stateEvent: StateEvent) {
        sessionManager.cachedToken.value?.let { authToken ->
            val job: Flow<DataState<AccountViewState>> = when (stateEvent) {

                is GetAccountPropertiesEvent -> {
                    accountRepository.getAccountProperties(
                        stateEvent = stateEvent,
                        authToken = authToken
                    )
                }

                is UpdateAccountPropertiesEvent -> {
                    val email =
                        stateEvent.email.toRequestBody("text/plain".toMediaTypeOrNull())
                    val username =
                        stateEvent.username.toRequestBody("text/plain".toMediaTypeOrNull())
                    val bio = RequestBody.create("text/plain".toMediaTypeOrNull(), stateEvent.bio)
                    accountRepository.saveAccountProperties(
                        stateEvent = stateEvent,
                        email = email,
                        username = username,
                        bio = bio,
                        image = stateEvent.image
                    )
                }

                is ChangePasswordEvent -> {
                    accountRepository.updatePassword(
                        stateEvent = stateEvent,
                        currentPassword = stateEvent.currentPassword,
                        newPassword = stateEvent.newPassword,
                        confirmNewPassword = stateEvent.confirmNewPassword
                    )
                }

                is LogoutEvent -> {
                    accountRepository.logout(stateEvent)
                }

                else -> {
                    flow {
                        emit(
                            DataState.error<AccountViewState>(
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

    private fun setAccountPropertiesData(accountProperties: AccountProperties) {
        val update = getCurrentViewStateOrNew()
        if (update.accountProperties == accountProperties) {
            return
        }
        update.accountProperties = accountProperties
        setViewState(update)
    }

    fun setUpdatedUri(uri: Uri) {
        val update = getCurrentViewStateOrNew()
        if (update.updatedImageUri == uri) {
            return
        }
        update.updatedImageUri = uri
        setViewState(update)
    }

    override fun initNewViewState(): AccountViewState {
        return AccountViewState()
    }

    fun logout() {
        setStateEvent(LogoutEvent())
        sessionManager.logout()
    }

    fun getUpdatedImageUri(): Uri? {
        getCurrentViewStateOrNew().let {
            it.updatedImageUri?.let {
                return it
            }
        }
        return null
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}
