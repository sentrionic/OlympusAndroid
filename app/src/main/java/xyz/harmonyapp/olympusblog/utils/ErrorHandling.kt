package xyz.harmonyapp.olympusblog.utils

import android.util.Log
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import xyz.harmonyapp.olympusblog.api.response.FieldError

class ErrorHandling {


    companion object {

        private val TAG: String = "AppDebug"

        const val UNABLE_TO_RESOLVE_HOST = "Unable to resolve host"
        const val UNABLE_TODO_OPERATION_WO_INTERNET =
            "Can't do that operation without an internet connection"

        const val ERROR_SAVE_ACCOUNT_PROPERTIES =
            "Error saving account properties.\nTry restarting the app."
        const val ERROR_SAVE_AUTH_TOKEN =
            "Error saving authentication token.\nTry restarting the app."
        const val ERROR_SOMETHING_WRONG_WITH_IMAGE = "Something went wrong with the image."
        const val ERROR_MUST_SELECT_IMAGE = "You must select an image."

        const val GENERIC_AUTH_ERROR = "Error"
        const val PAGINATION_DONE_ERROR = "Invalid page."
        const val ERROR_CHECK_NETWORK_CONNECTION = "Check network connection."
        const val ERROR_UNKNOWN = "Unknown error"
        private val errorList = Types.newParameterizedType(MutableList::class.java, FieldError::class.java)
        private val adapter: JsonAdapter<List<FieldError>> = Moshi.Builder().build().adapter(errorList)


        fun isNetworkError(msg: String): Boolean {
            return when {
                msg.contains(UNABLE_TO_RESOLVE_HOST) -> true
                else -> false
            }
        }

        fun parseDetailJsonResponse(rawJson: String?): String {
            Log.d(TAG, "parseDetailJsonResponse: ${rawJson}")
            try {
                if (!rawJson.isNullOrBlank()) {
                    if (rawJson == ERROR_CHECK_NETWORK_CONNECTION) {
                        return PAGINATION_DONE_ERROR
                    }
                    val errorJson = JSONObject(rawJson).get("errors")
                    val errors = JSONArray(errorJson.toString())
                    var out = ""
                    adapter.fromJson(errors.toString())?.forEach { error -> out += "${error.message}\n" }
                    return out
                }
            } catch (e: JSONException) {
                Log.e(TAG, "parseDetailJsonResponse: ${e.message}")
            }
            return ""
        }

    }
}