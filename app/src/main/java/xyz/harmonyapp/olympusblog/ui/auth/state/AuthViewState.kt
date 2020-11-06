package xyz.harmonyapp.olympusblog.ui.auth.state

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import xyz.harmonyapp.olympusblog.models.AuthToken

const val AUTH_VIEW_STATE_BUNDLE_KEY =
    "package xyz.harmonyapp.olympusblog.ui.auth.state.AuthViewState"

@Parcelize
data class AuthViewState(
    var registrationFields: RegistrationFields? = null,
    var loginFields: LoginFields? = null,
    var authToken: AuthToken? = null
) : Parcelable


@Parcelize
data class RegistrationFields(
    var registration_email: String? = null,
    var registration_username: String? = null,
    var registration_password: String? = null
) : Parcelable {

    class RegistrationError {
        companion object {

            fun mustFillAllFields(): String {
                return "All fields are required."
            }

            fun none(): String {
                return "None"
            }

        }
    }

    fun isValidForRegistration(): String {
        if (registration_email.isNullOrEmpty()
            || registration_username.isNullOrEmpty()
            || registration_password.isNullOrEmpty()
        ) {
            return RegistrationError.mustFillAllFields()
        }

        return RegistrationError.none()
    }
}

@Parcelize
data class LoginFields(
    var login_email: String? = null,
    var login_password: String? = null
) : Parcelable {
    class LoginError {

        companion object {

            fun mustFillAllFields(): String {
                return "You can't login without an email and password."
            }

            fun none(): String {
                return "None"
            }

        }
    }

    fun isValidForLogin(): String {

        if (login_email.isNullOrEmpty()
            || login_password.isNullOrEmpty()
        ) {

            return LoginError.mustFillAllFields()
        }
        return LoginError.none()
    }

    override fun toString(): String {
        return "LoginState(email=$login_email, password=$login_password)"
    }
}
