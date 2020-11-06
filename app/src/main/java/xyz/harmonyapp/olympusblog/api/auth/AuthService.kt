package xyz.harmonyapp.olympusblog.api.auth

import retrofit2.http.Body
import retrofit2.http.POST
import xyz.harmonyapp.olympusblog.api.auth.dto.LoginDTO
import xyz.harmonyapp.olympusblog.api.auth.dto.RegisterDTO
import xyz.harmonyapp.olympusblog.di.auth.AuthScope
import xyz.harmonyapp.olympusblog.models.AccountProperties

@AuthScope
interface AuthService {

    @POST("users/login")
    suspend fun login(
        @Body body: LoginDTO
    ): AccountProperties

    @POST("users")
    suspend fun register(
        @Body body: RegisterDTO
    ): AccountProperties
}