package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// PRAYER TIMES
@JsonClass(generateAdapter = true)
data class PrayerResponse(
    @Json(name = "code") val code: Int,
    @Json(name = "status") val status: String,
    @Json(name = "data") val data: PrayerData?
)

@JsonClass(generateAdapter = true)
data class PrayerData(
    @Json(name = "timings") val timings: PrayerTimings,
    @Json(name = "date") val date: DateInfo
)

@JsonClass(generateAdapter = true)
data class PrayerTimings(
    @Json(name = "Fajr") val fajr: String,
    @Json(name = "Sunrise") val sunrise: String,
    @Json(name = "Dhuhr") val dhuhr: String,
    @Json(name = "Asr") val asr: String,
    @Json(name = "Maghrib") val maghrib: String,
    @Json(name = "Isha") val isha: String
)

@JsonClass(generateAdapter = true)
data class DateInfo(
    @Json(name = "readable") val readable: String,
    @Json(name = "hijri") val hijri: HijriInfo
)

@JsonClass(generateAdapter = true)
data class HijriInfo(
    @Json(name = "date") val date: String,
    @Json(name = "day") val day: String,
    @Json(name = "month") val month: HijriMonth,
    @Json(name = "year") val year: String
)

@JsonClass(generateAdapter = true)
data class HijriMonth(
    @Json(name = "number") val number: Int,
    @Json(name = "ar") val ar: String,
    @Json(name = "en") val en: String
)

// QURAN TEXT
@JsonClass(generateAdapter = true)
data class SurahResponse(
    @Json(name = "code") val code: Int,
    @Json(name = "status") val status: String,
    @Json(name = "data") val data: SurahData?
)

@JsonClass(generateAdapter = true)
data class SurahData(
    @Json(name = "number") val number: Int,
    @Json(name = "name") val name: String,
    @Json(name = "englishName") val englishName: String,
    @Json(name = "numberOfAyahs") val numberOfAyahs: Int,
    @Json(name = "ayahs") val ayahs: List<AyahData>
)

@JsonClass(generateAdapter = true)
data class AyahData(
    @Json(name = "number") val number: Int,
    @Json(name = "text") val text: String,
    @Json(name = "numberInSurah") val numberInSurah: Int
)

@JsonClass(generateAdapter = true)
data class SingleAyahResponse(
    @Json(name = "code") val code: Int,
    @Json(name = "data") val data: AyahResponseData?
)

@JsonClass(generateAdapter = true)
data class AyahResponseData(
    @Json(name = "number") val number: Int,
    @Json(name = "text") val text: String,
    @Json(name = "numberInSurah") val numberInSurah: Int,
    @Json(name = "surah") val surah: MiniSurahData
)

@JsonClass(generateAdapter = true)
data class MiniSurahData(
    @Json(name = "number") val number: Int,
    @Json(name = "name") val name: String,
    @Json(name = "englishName") val englishName: String
)

// RECITERS
@JsonClass(generateAdapter = true)
data class ReciterListResponse(
    @Json(name = "reciters") val reciters: List<RawReciter>
)

@JsonClass(generateAdapter = true)
data class RawReciter(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "moshaf") val moshaf: List<RawMoshaf>
)

@JsonClass(generateAdapter = true)
data class RawMoshaf(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "server") val server: String,
    @Json(name = "surah_list") val surahList: String
)

@JsonClass(generateAdapter = true)
data class AyahTiming(
    @Json(name = "ayah") val ayah: Int,
    @Json(name = "start_time") val startTime: Int
)

// NOMINATIM REVERSE GEOCODING
@JsonClass(generateAdapter = true)
data class ReverseGeocodeResponse(
    @Json(name = "address") val address: AddressLocation?
)

@JsonClass(generateAdapter = true)
data class AddressLocation(
    @Json(name = "city") val city: String?,
    @Json(name = "town") val town: String?,
    @Json(name = "county") val county: String?,
    @Json(name = "country") val country: String?
)
