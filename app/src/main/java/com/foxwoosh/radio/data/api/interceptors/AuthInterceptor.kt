package com.foxwoosh.radio.data.api.interceptors

import com.foxwoosh.radio.data.storage.local.user.IUserLocalStorage
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val userLocalStorage: IUserLocalStorage
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { userLocalStorage.getToken() }

        return if (token.isNullOrEmpty()) {
            chain.proceed(chain.request())
        } else {
            val newRequest = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()

            chain.proceed(newRequest)
        }
    }
}