package xyz.harmonyapp.olympusblog.ui.main.account.state

import okhttp3.MultipartBody
import xyz.harmonyapp.olympusblog.utils.StateEvent

sealed class AccountStateEvent : StateEvent {

    class GetAccountPropertiesEvent : AccountStateEvent() {
        override fun errorInfo(): String {
            return "Error retrieving account properties."
        }

        override fun toString(): String {
            return "GetAccountPropertiesEvent"
        }
    }

    data class UpdateAccountPropertiesEvent(
        val email: String,
        val username: String,
        val bio: String,
        val image: MultipartBody.Part?
    ) : AccountStateEvent() {
        override fun errorInfo(): String {
            return "Error updating account properties."
        }

        override fun toString(): String {
            return "UpdateAccountPropertiesEvent"
        }
    }

    data class ChangePasswordEvent(
        val currentPassword: String,
        val newPassword: String,
        val confirmNewPassword: String
    ) : AccountStateEvent() {
        override fun errorInfo(): String {
            return "Error changing password."
        }

        override fun toString(): String {
            return "ChangePasswordEvent"
        }
    }

    class LogoutEvent : AccountStateEvent() {
        override fun errorInfo(): String {
            return "Error LogoutEvent."
        }

        override fun toString(): String {
            return "LogoutEvent"
        }
    }

    class None : AccountStateEvent() {
        override fun errorInfo(): String {
            return "None"
        }
    }
}