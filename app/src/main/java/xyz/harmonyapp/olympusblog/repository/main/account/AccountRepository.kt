package xyz.harmonyapp.olympusblog.repository.main.account

import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.models.AuthToken
import xyz.harmonyapp.olympusblog.ui.main.account.state.AccountViewState
import xyz.harmonyapp.olympusblog.utils.DataState
import xyz.harmonyapp.olympusblog.utils.StateEvent

@MainScope
interface AccountRepository {

    fun getAccountProperties(
        authToken: AuthToken,
        stateEvent: StateEvent
    ): Flow<DataState<AccountViewState>>

    fun saveAccountProperties(
        email: RequestBody,
        username: RequestBody,
        bio: RequestBody,
        image: MultipartBody.Part?,
        stateEvent: StateEvent
    ): Flow<DataState<AccountViewState>>

    fun updatePassword(
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String,
        stateEvent: StateEvent
    ): Flow<DataState<AccountViewState>>

    fun logout(
        stateEvent: StateEvent
    ): Flow<DataState<AccountViewState>>
}