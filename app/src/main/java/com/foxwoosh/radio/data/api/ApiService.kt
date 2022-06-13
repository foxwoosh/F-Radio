package com.foxwoosh.radio.data.api

import com.foxwoosh.radio.AppJson
import com.foxwoosh.radio.data.api.foxy.responses.LyricsResponse
import com.foxwoosh.radio.data.api.musixmatch.LyricsMatchResponse
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.create
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

    private val converterFactory = AppJson.asConverterFactory("application/json".toMediaType())

    val musixmatch: MusixmatchApi = Retrofit.Builder()
        .baseUrl("https://api.musixmatch.com/ws/1.1/")
        .client(client)
        .addConverterFactory(converterFactory)
        .build()
        .create()

    val foxy: FoxyApi = Retrofit.Builder()
        .baseUrl("https://foxwoosh.space/")
        .client(client)
        .addConverterFactory(converterFactory)
        .build()
        .create()

    interface MusixmatchApi {
        @GET("matcher.lyrics.get")
        suspend fun getLyrics(
            @Query("apikey") key: String,
            @Query("q_track") title: String,
            @Query("q_artist") artist: String
        ) : LyricsMatchResponse
    }

    interface FoxyApi {
        @GET("lyrics")
        suspend fun getLyrics(
            @Query("k") key: String,
            @Query("s") source: String,
            @Query("q") query: String
        ): LyricsResponse
    }
}