package com.example.network

import com.example.data.*
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface AladhanApi {
    @GET("v1/timingsByCity/{date}")
    suspend fun getTimingsByCity(
        @Path("date") date: String,
        @Query("city") city: String,
        @Query("country") country: String,
        @Query("method") method: Int
    ): PrayerResponse

    @GET("v1/timings/{timestamp}")
    suspend fun getTimingsByCoords(
        @Path("timestamp") timestamp: Long,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int
    ): PrayerResponse
}

interface AlquranCloudApi {
    @GET("v1/surah/{number}/quran-uthmani")
    suspend fun getSurahUthmani(
        @Path("number") number: Int
    ): SurahResponse

    @GET("v1/ayah/{key}/quran-uthmani")
    suspend fun getAyahUthmani(
        @Path("key") key: String
    ): SingleAyahResponse
}

interface Mp3QuranApi {
    @GET("api/v3/reciters")
    suspend fun getReciters(
        @Query("language") language: String
    ): ReciterListResponse

    @GET("api/v3/ayat_timing")
    suspend fun getAyahTimings(
        @Query("surah") surah: Int,
        @Query("read") read: Int
    ): List<AyahTiming>
}

interface NominatimApi {
    @GET
    suspend fun reverseGeocode(
        @Url url: String
    ): ReverseGeocodeResponse
}

object NetworkClient {
    private val moshiConverterFactory = MoshiConverterFactory.create()

    val aladhan: AladhanApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.aladhan.com/")
            .addConverterFactory(moshiConverterFactory)
            .build()
            .create(AladhanApi::class.java)
    }

    val alquranCloud: AlquranCloudApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.alquran.cloud/")
            .addConverterFactory(moshiConverterFactory)
            .build()
            .create(AlquranCloudApi::class.java)
    }

    val mp3quran: Mp3QuranApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://mp3quran.net/")
            .addConverterFactory(moshiConverterFactory)
            .build()
            .create(Mp3QuranApi::class.java)
    }

    val nominatim: NominatimApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org/")
            .addConverterFactory(moshiConverterFactory)
            .build()
            .create(NominatimApi::class.java)
    }
}
