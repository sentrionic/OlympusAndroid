package xyz.harmonyapp.olympusblog.ui.main.account

import android.net.Uri
import androidx.lifecycle.LiveData
import okhttp3.MultipartBody
import xyz.harmonyapp.olympusblog.models.AccountProperties
import xyz.harmonyapp.olympusblog.repository.main.AccountRepository
import xyz.harmonyapp.olympusblog.session.SessionManager
import xyz.harmonyapp.olympusblog.ui.BaseViewModel
import xyz.harmonyapp.olympusblog.ui.DataState
import xyz.harmonyapp.olympusblog.ui.Loading
import xyz.harmonyapp.olympusblog.ui.main.account.state.AccountStateEvent
import xyz.harmonyapp.olympusblog.ui.main.account.state.AccountStateEvent.*
import xyz.harmonyapp.olympusblog.ui.main.account.state.AccountViewState
import xyz.harmonyapp.olympusblog.utils.AbsentLiveData
import javax.inject.Inject

class AccountViewModel
@Inject
constructor(
    val sessionManager: SessionManager,
    val accountRepository: AccountRepository
) : BaseViewModel<AccountStateEvent, AccountViewState>() {
    override fun handleStateEvent(stateEvent: AccountStateEvent): LiveData<DataState<AccountViewState>> {
        return when (stateEvent) {

            is GetAccountPropertiesEvent -> {
                sessionManager.cachedToken.value?.let { authToken ->
                    accountRepository.getAccountProperties(authToken)
                } ?: AbsentLiveData.create()
            }

            is UpdateAccountPropertiesEvent -> {
                sessionManager.cachedToken.value?.let { authToken ->
                    authToken.account_id?.let { id ->
                        val newAccountProperties = AccountProperties(
                            id,
                            stateEvent.email,
                            stateEvent.username,
                            stateEvent.bio,
                            ""
                        )
                        accountRepository.saveAccountProperties(
                            newAccountProperties,
                            stateEvent.image
                        )
                    }
                } ?: AbsentLiveData.create()
            }

            is ChangePasswordEvent -> {
                accountRepository.updatePassword(
                    stateEvent.currentPassword,
                    stateEvent.newPassword,
                    stateEvent.confirmNewPassword
                )
            }

            is None -> {
                object : LiveData<DataState<AccountViewState>>() {
                    override fun onActive() {
                        super.onActive()
                        value = DataState(null, Loading(false), null)
                    }
                }
            }
        }
    }

    fun setAccountPropertiesData(accountProperties: AccountProperties) {
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

    fun cancelActiveJobs() {
        accountRepository.cancelActiveJobs() // cancel active jobs
        handlePendingData() // hide progress bar
    }

    private fun handlePendingData() {
        setStateEvent(None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}
