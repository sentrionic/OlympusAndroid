package xyz.harmonyapp.olympusblog.repository

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.HttpException
import xyz.harmonyapp.olympusblog.api.response.FieldError
import xyz.harmonyapp.olympusblog.utils.*
import xyz.harmonyapp.olympusblog.utils.ApiResult.*
import xyz.harmonyapp.olympusblog.utils.Constants.Companion.CACHE_TIMEOUT
import xyz.harmonyapp.olympusblog.utils.Constants.Companion.NETWORK_TIMEOUT
import xyz.harmonyapp.olympusblog.utils.ErrorHandling.Companion.CACHE_ERROR_TIMEOUT
import xyz.harmonyapp.olympusblog.utils.ErrorHandling.Companion.NETWORK_ERROR_TIMEOUT
import xyz.harmonyapp.olympusblog.utils.ErrorHandling.Companion.UNKNOWN_ERROR
import java.io.IOException

/**
 * Reference: https://medium.com/@douglas.iacovelli/how-to-handle-errors-with-retrofit-and-coroutines-33e7492a912
 */
private val TAG: String = "AppDebug"
private val errorList = Types.newParameterizedType(MutableList::class.java, FieldError::class.java)
private val adapter: JsonAdapter<List<FieldError>> = Moshi.Builder().build().adapter(errorList)

suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher,
    apiCall: suspend () -> T?
): ApiResult<T?> {
    return withContext(dispatcher) {
        try {
            // throws TimeoutCancellationException
            withTimeout(NETWORK_TIMEOUT) {
                Success(apiCall.invoke())
            }
        } catch (throwable: Throwable) {
            when (throwable) {
                is TimeoutCancellationException -> {
                    val code = 408 // timeout error code
                    GenericError(code, NETWORK_ERROR_TIMEOUT)
                }
                is IOException -> {
                    NetworkError
                }
                is HttpException -> {
                    val code = throwable.code()
                    val errorResponse = convertErrorBody(throwable)
                    GenericError(
                        code,
                        errorResponse
                    )
                }
                else -> {
                    GenericError(
                        null,
                        UNKNOWN_ERROR
                    )
                }
            }
        }
    }
}

suspend fun <T> safeCacheCall(
    dispatcher: CoroutineDispatcher,
    cacheCall: suspend () -> T?
): CacheResult<T?> {
    return withContext(dispatcher) {
        try {
            // throws TimeoutCancellationException
            withTimeout(CACHE_TIMEOUT) {
                CacheResult.Success(cacheCall.invoke())
            }
        } catch (throwable: Throwable) {
            when (throwable) {
                is TimeoutCancellationException -> {
                    CacheResult.GenericError(CACHE_ERROR_TIMEOUT)
                }
                else -> {
                    CacheResult.GenericError(UNKNOWN_ERROR)
                }
            }
        }
    }
}


fun <ViewState> buildError(
    message: String,
    uiComponentType: UIComponentType,
    stateEvent: StateEvent?
): DataState<ViewState> {
    return DataState.error(
        response = Response(
            message = "${stateEvent?.errorInfo()}\n\nReason: ${message}",
            uiComponentType = uiComponentType,
            messageType = MessageType.Error()
        ),
        stateEvent = stateEvent
    )
}

private fun convertErrorBody(throwable: HttpException): String? {
    return try {
        throwable.response()?.errorBody()?.string()?.let { errorString ->
            val errorJson = JSONObject(errorString).get("errors")
            val errors = JSONArray(errorJson.toString())
            var out = ""
            adapter.fromJson(errors.toString())?.forEach { error -> out += "${error.message}\n" }
            return out
        }
    } catch (exception: Exception) {
        UNKNOWN_ERROR
    }
}