package com.foxwoosh.radio.data.api

import com.foxwoosh.radio.AppJson
import com.foxwoosh.radio.BuildConfig
import com.foxwoosh.radio.data.api.foxy.requests.LoginRequest
import com.foxwoosh.radio.data.api.foxy.requests.LyricsReportRequest
import com.foxwoosh.radio.data.api.foxy.requests.RegisterRequest
import com.foxwoosh.radio.data.api.foxy.responses.AuthResponse
import com.foxwoosh.radio.data.api.foxy.responses.LyricsReportResponse
import com.foxwoosh.radio.data.api.foxy.responses.LyricsResponse
import com.foxwoosh.radio.data.api.interceptors.AuthInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiService @Inject constructor(authInterceptor: AuthInterceptor) {

    private val baseUrl = StringBuilder()
        .append(if (BuildConfig.DEBUG) "http://" else "https://")
        .append(BuildConfig.BASE_URL)
        .append("/")
        .toString()

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .build()

    private val converterFactory = AppJson.asConverterFactory("application/json".toMediaType())

    val foxy: FoxyApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(converterFactory)
        .build()
        .create()

    interface FoxyApi {
        @GET("$V1/lyrics")
        suspend fun getLyrics(
            @Query("artist") artist: String,
            @Query("title") title: String
        ): LyricsResponse

        @PUT("$V1/lyrics/report")
        suspend fun reportLyrics(@Body body: LyricsReportRequest)

        @GET("$V1/lyrics/reports")
        suspend fun getUserReports(): List<LyricsReportResponse>

        @POST("$V1/register")
        suspend fun register(@Body body: RegisterRequest): AuthResponse

        @POST("$V1/login")
        suspend fun login(@Body body: LoginRequest): AuthResponse
    }

    private companion object {
        const val V1 = "v1"
    }
}