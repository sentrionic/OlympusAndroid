package xyz.harmonyapp.olympusblog.api.auth

import androidx.lifecycle.LiveData
import retrofit2.http.Body
import retrofit2.http.POST
import xyz.harmonyapp.olympusblog.api.auth.dto.LoginDTO
import xyz.harmonyapp.olympusblog.api.auth.dto.RegisterDTO
import xyz.harmonyapp.olympusblog.models.AccountProperties
import xyz.harmonyapp.olympusblog.utils.GenericApiResponse

interface AuthService {

    @POST("users/login")
    fun login(
        @Body body: LoginDTO
    ): LiveData<GenericApiResponse<AccountProperties>>

    @POST("users")
    fun register(
        @Body body: RegisterDTO
    ): LiveData<GenericApiResponse<AccountProperties>>
}