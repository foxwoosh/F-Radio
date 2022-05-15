package com.foxwoosh.radio.api

import com.foxwoosh.radio.api.responses.CheckIDResponse
import com.foxwoosh.radio.api.responses.CurrentTrackResponse
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiService @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val converterFactory = json.asConverterFactory(MediaType.get("application/json"))

    val api: Api = Retrofit.Builder()
        .baseUrl("https://meta.fmgid.com/")
        .client(client)
        .addConverterFactory(converterFactory)
        .build()
        .create(Api::class.java)

    interface Api {
        @GET("stations/ultra/current.json")
        suspend fun getCurrentTrack(@Query("t") time: Long): CurrentTrackResponse

        @GET("stations/ultra/id.json")
        suspend fun checkID(@Query("t") time: Long): CheckIDResponse
    }
}