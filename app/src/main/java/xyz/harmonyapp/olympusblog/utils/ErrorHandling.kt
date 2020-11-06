package xyz.harmonyapp.olympusblog.utils

class ErrorHandling {


    companion object {
        const val ERROR_SAVE_ACCOUNT_PROPERTIES =
            "Error saving account properties.\nTry restarting the app."
        const val ERROR_SAVE_AUTH_TOKEN =
            "Error saving authentication token.\nTry restarting the app."
        const val ERROR_SOMETHING_WRONG_WITH_IMAGE = "Something went wrong with the image."
        const val ERROR_MUST_SELECT_IMAGE = "You must select an image."

        const val ERROR_UNKNOWN = "Unknown error"
        const val SOMETHING_WRONG_WITH_IMAGE = "Something went wrong with the image."
        const val INVALID_STATE_EVENT = "Invalid state event"
        const val NETWORK_ERROR = "Network error"
        const val NETWORK_ERROR_TIMEOUT = "Network timeout"
        const val CACHE_ERROR_TIMEOUT = "Cache timeout"
        const val UNKNOWN_ERROR = "Unknown error"

    }
}