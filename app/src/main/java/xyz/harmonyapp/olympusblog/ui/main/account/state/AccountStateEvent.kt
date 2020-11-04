package xyz.harmonyapp.olympusblog.ui.main.account.state

import okhttp3.MultipartBody

sealed class AccountStateEvent {

    class GetAccountPropertiesEvent : AccountStateEvent()

    data class UpdateAccountPropertiesEvent(
        val email: String,
        val username: String,
        val bio: String,
        val image: MultipartBody.Part?
    ) : AccountStateEvent()

    data class ChangePasswordEvent(
        val currentPassword: String,
        val newPassword: String,
        val confirmNewPassword: String
    ) : AccountStateEvent()

    class None : AccountStateEvent()
}