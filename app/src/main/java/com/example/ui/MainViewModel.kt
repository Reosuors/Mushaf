package com.example.ui

import android.app.Application
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Vibrator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.network.NetworkClient
import com.example.ui.theme.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("mushaf_prefs", Context.MODE_PRIVATE)
    private val vibrator = application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    // Core Settings
    var lang by mutableStateOf(prefs.getString("lang", "ar") ?: "ar")
        private set
    var themeId by mutableStateOf(prefs.getString("theme", "dark_gold") ?: "dark_gold")
        private set
    var activeTheme by mutableStateOf(ALL_THEMES.find { it.id == themeId } ?: DarkGoldTheme)
        private set
    var currentSection by mutableStateOf("quran") // default section

    // Hijri date
    var hijriDate by mutableStateOf("—")
        private set

    // Tasbih State
    var selectedTasbihIndex by mutableIntStateOf(0)
    var tasbihCount by mutableIntStateOf(0)
    var tasbihTotalToday by mutableIntStateOf(prefs.getInt("tsb_total", 0))

    // Azkar checkmark states
    val morningCounts = mutableStateListOf<Int>()
    val eveningCounts = mutableStateListOf<Int>()
    val sleepCounts = mutableStateListOf<Int>()

    // Prayer State
    var cityInput by mutableStateOf(prefs.getString("city", "Mecca") ?: "Mecca")
    var countryInput by mutableStateOf(prefs.getString("country", "Saudi Arabia") ?: "Saudi Arabia")
    var prayerMethod by mutableIntStateOf(prefs.getInt("method", 2))
    var prayerTimings by mutableStateOf<PrayerTimings?>(null)
    var isPrayerLoading by mutableStateOf(false)
    var prayerError by mutableStateOf<String?>(null)
    var nextPrayerName by mutableStateOf("")
    var nextPrayerTime by mutableStateOf("")
    var countdownText by mutableStateOf("—")
    var dateReadable by mutableStateOf("")

    // Quran State
    var surahSearchQuery by mutableStateOf("")
    var ayahOfDayText by mutableStateOf("")
    var ayahOfDayRef by mutableStateOf("")
    var isAyahLoading by mutableStateOf(false)

    // Active Surah Detail Screen
    var currentSurahNum by mutableStateOf<Int?>(null)
    var isSurahLoading by mutableStateOf(false)
    var surahError by mutableStateOf<String?>(null)
    var loadedAyahs by mutableStateOf<List<AyahData>>(emptyList())
    var currentSurahQuery by mutableStateOf("")
    var showAyahDrop by mutableStateOf(false)

    // Text configuration
    var quranFontSizeScale by mutableStateOf(1.5f)
    var isContinuousMode by mutableStateOf(true)

    // Reciters State
    var recitersList by mutableStateOf<List<RawReciter>>(emptyList())
    var selectedReciter by mutableStateOf<RawReciter?>(null)
    var loadedAyahTimings by mutableStateOf<List<AyahTiming>>(emptyList())

    // Audio Player State
    private var mediaPlayer: MediaPlayer? = null
    var isPlayingAudio by mutableStateOf(false)
    var currentlyPlayingAyahNum by mutableStateOf(0)
    var audioCurrentTimeSec by mutableStateOf(0f)
    var audioDurationSec by mutableStateOf(0f)
    var currentMediaPlayingUrl by mutableStateOf("")

    // Loop/Repetition custom player state
    var isLoopActive by mutableStateOf(false)
    var loopMode by mutableStateOf("ayah") // "ayah" or "surah"
    var loopFromAyah by mutableIntStateOf(1)
    var loopToAyah by mutableIntStateOf(7)
    var loopTimes by mutableIntStateOf(3)
    var loopFromSurahNum by mutableIntStateOf(1)
    var loopToSurahNum by mutableIntStateOf(1)

    // Running Repeat loop variables
    var activeRepeatNum by mutableIntStateOf(0)
    var activeRepeatStatus by mutableStateOf("")
    var activeRepeatTitle by mutableStateOf("")
    var activeRepeatAyahText by mutableStateOf("")
    var isRepeatPaused by mutableStateOf(false)
    private var loopJob: Job? = null

    // Date timer job
    private var prayerTimerJob: Job? = null

    init {
        resetDailyAdhkar()
        loadReciters()
        loadAyahOfDay()
        searchPrayerTimes()
        fetchHijriDate()

        // Start dynamic prayer dates counter
        startPrayerTimer()
    }

    private fun startPrayerTimer() {
        prayerTimerJob?.cancel()
        prayerTimerJob = viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                updatePrayerCountdown()
                delay(15000) // Update countdown accuracy every 15s
            }
        }
    }

    private fun resetDailyAdhkar() {
        // Morning
        morningCounts.clear()
        AdhkarProvider.morning.forEach {
            morningCounts.add(prefs.getInt("mor_${it.id}", 0))
        }
        // Evening
        eveningCounts.clear()
        AdhkarProvider.evening.forEach {
            eveningCounts.add(prefs.getInt("eve_${it.id}", 0))
        }
        // Sleep
        sleepCounts.clear()
        AdhkarProvider.sleep.forEach {
            sleepCounts.add(prefs.getInt("sle_${it.id}", 0))
        }
    }

    // Core Settings handlers
    fun changeLanguage(langCode: String) {
        lang = langCode
        prefs.edit().putString("lang", langCode).apply()
        loadReciters() // reload reciters names in localized form
    }

    fun changeTheme(id: String) {
        themeId = id
        prefs.edit().putString("theme", id).apply()
        activeTheme = ALL_THEMES.find { it.id == id } ?: DarkGoldTheme
    }

    fun performHapticFeedback(durationMs: Long = 20) {
        try {
            vibrator?.vibrate(durationMs)
        } catch (_: Exception) {}
    }

    // Settings storage helpers
    fun incrementTasbih() {
        val maxTarget = TASBIH_LIST.getOrNull(selectedTasbihIndex)?.target ?: 100
        if (tasbihCount < maxTarget) {
            tasbihCount++
            tasbihTotalToday++
            prefs.edit().putInt("tsb_total", tasbihTotalToday).apply()
            performHapticFeedback(25)
        }
    }

    fun decrementTasbih() {
        if (tasbihCount > 0) {
            tasbihCount--
            if (tasbihTotalToday > 0) {
                tasbihTotalToday--
                prefs.edit().putInt("tsb_total", tasbihTotalToday).apply()
            }
            performHapticFeedback(20)
        }
    }

    fun resetTasbihProgress() {
        tasbihCount = 0
        performHapticFeedback(45)
    }

    // Adhkar Click Increments
    fun incrementAdhkarCount(category: String, idx: Int, max: Int) {
        when (category) {
            "sabah" -> {
                if (morningCounts[idx] < max) {
                    morningCounts[idx] = morningCounts[idx] + 1
                    prefs.edit().putInt("mor_${idx + 1}", morningCounts[idx]).apply()
                    performHapticFeedback(20)
                }
            }
            "masa" -> {
                if (eveningCounts[idx] < max) {
                    eveningCounts[idx] = eveningCounts[idx] + 1
                    prefs.edit().putInt("eve_${idx + 1}", eveningCounts[idx]).apply()
                    performHapticFeedback(20)
                }
            }
            "nawm" -> {
                if (sleepCounts[idx] < max) {
                    sleepCounts[idx] = sleepCounts[idx] + 1
                    prefs.edit().putInt("sle_${idx + 1}", sleepCounts[idx]).apply()
                    performHapticFeedback(20)
                }
            }
        }
    }

    fun decrementAdhkarCount(category: String, idx: Int) {
        when (category) {
            "sabah" -> {
                if (morningCounts[idx] > 0) {
                    morningCounts[idx] = morningCounts[idx] - 1
                    prefs.edit().putInt("mor_${idx + 1}", morningCounts[idx]).apply()
                }
            }
            "masa" -> {
                if (eveningCounts[idx] > 0) {
                    eveningCounts[idx] = eveningCounts[idx] - 1
                    prefs.edit().putInt("eve_${idx + 1}", eveningCounts[idx]).apply()
                }
            }
            "nawm" -> {
                if (sleepCounts[idx] > 0) {
                    sleepCounts[idx] = sleepCounts[idx] - 1
                    prefs.edit().putInt("sle_${idx + 1}", sleepCounts[idx]).apply()
                }
            }
        }
    }

    fun resetAdhkarGroup(category: String) {
        val editor = prefs.edit()
        when (category) {
            "sabah" -> {
                for (i in morningCounts.indices) {
                    morningCounts[i] = 0
                    editor.putInt("mor_${i + 1}", 0)
                }
            }
            "masa" -> {
                for (i in eveningCounts.indices) {
                    eveningCounts[i] = 0
                    editor.putInt("eve_${i + 1}", 0)
                }
            }
            "nawm" -> {
                for (i in sleepCounts.indices) {
                    sleepCounts[i] = 0
                    editor.putInt("sle_${i + 1}", 0)
                }
            }
        }
        editor.apply()
        performHapticFeedback(45)
    }

    // Prayer Service Callers
    fun searchPrayerTimes() {
        val city = cityInput.trim()
        val country = countryInput.trim()
        if (city.isEmpty()) return

        isPrayerLoading = true
        prayerError = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                val dateStr = dateFormat.format(Date())
                val response = NetworkClient.aladhan.getTimingsByCity(
                    date = dateStr,
                    city = city,
                    country = country,
                    method = prayerMethod
                )
                if (response.code == 200 && response.data != null) {
                    withContext(Dispatchers.Main) {
                        prayerTimings = response.data.timings
                        dateReadable = response.data.date.readable
                        // Cache values
                        prefs.edit()
                            .putString("city", city)
                            .putString("country", country)
                            .putInt("method", prayerMethod)
                            .apply()
                        updatePrayerCountdown()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        prayerError = "فشل تحميل مواقيت الصلاة"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    prayerError = e.localizedMessage ?: "حدث خطأ في الشبكة"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isPrayerLoading = false
                }
            }
        }
    }

    fun updateCalculationMethod(methodId: Int) {
        prayerMethod = methodId
        searchPrayerTimes()
    }

    private fun updatePrayerCountdown() {
        val timings = prayerTimings ?: return
        val currentLocal = Calendar.getInstance()
        val hr = currentLocal.get(Calendar.HOUR_OF_DAY)
        val min = currentLocal.get(Calendar.MINUTE)
        val nowMins = hr * 60 + min

        val calendarMap = listOf(
            "Fajr" to timings.fajr,
            "Sunrise" to timings.sunrise,
            "Dhuhr" to timings.dhuhr,
            "Asr" to timings.asr,
            "Maghrib" to timings.maghrib,
            "Isha" to timings.isha
        )

        var matchedName = ""
        var matchedTime = ""
        var smallestDiff = Double.MAX_VALUE

        for ((name, timeStr) in calendarMap) {
            val parts = timeStr.split(":")
            if (parts.size >= 2) {
                val ph = parts[0].toIntOrNull() ?: 12
                val pm = parts[1].toIntOrNull() ?: 0
                val targetMins = ph * 60 + pm
                var diff = (targetMins - nowMins).toDouble()
                if (diff < 0) diff += 1440.0 // wrap over midnight
                if (diff < smallestDiff) {
                    smallestDiff = diff
                    matchedName = name
                    matchedTime = timeStr
                }
            }
        }

        if (matchedName.isEmpty()) {
            matchedName = "Fajr"
            matchedTime = timings.fajr
            val parts = timings.fajr.split(":")
            val ph = parts.getOrNull(0)?.toIntOrNull() ?: 4
            val pm = parts.getOrNull(1)?.toIntOrNull() ?: 30
            var diff = (ph * 60 + pm - nowMins).toDouble()
            if (diff < 0) diff += 1440.0
            smallestDiff = diff
        }

        val remHours = (smallestDiff / 60.0).toInt()
        val remMins = (smallestDiff % 60.0).toInt()
        val computedCountdownText = if (remHours > 0) "${remHours}h ${remMins}m" else "${remMins}m"

        viewModelScope.launch(Dispatchers.Main) {
            nextPrayerName = matchedName
            nextPrayerTime = matchedTime
            countdownText = computedCountdownText
        }
    }

    fun searchLocationByGps(lat: Double, lng: Double) {
        isPrayerLoading = true
        prayerError = null
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Reverse geocoding via Nominatim
                val url = "https://nominatim.openstreetmap.org/reverse?lat=$lat&lon=$lng&format=json"
                val geoResponse = NetworkClient.nominatim.reverseGeocode(url)
                val addr = geoResponse.address
                val resolvedCity = addr?.city ?: addr?.town ?: addr?.county ?: "Unknown"
                val resolvedCountry = addr?.country ?: ""

                withContext(Dispatchers.Main) {
                    cityInput = resolvedCity
                    countryInput = resolvedCountry
                }

                // Fetch Prayer timings for timestamp
                val epoch = System.currentTimeMillis() / 1000
                val response = NetworkClient.aladhan.getTimingsByCoords(
                    timestamp = epoch,
                    latitude = lat,
                    longitude = lng,
                    method = prayerMethod
                )
                if (response.code == 200 && response.data != null) {
                    withContext(Dispatchers.Main) {
                        prayerTimings = response.data.timings
                        dateReadable = response.data.date.readable
                        // Cache coordinates settings
                        prefs.edit()
                            .putString("city", resolvedCity)
                            .putString("country", resolvedCountry)
                            .apply()
                        updatePrayerCountdown()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        prayerError = "فشل تحديد معطيات الأوقات الجغرافية"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    prayerError = "خطأ في تحديد الموقع: ${e.localizedMessage}"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isPrayerLoading = false
                }
            }
        }
    }

    private fun fetchHijriDate() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = NetworkClient.aladhan.getTimingsByCity(
                    date = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date()),
                    city = "Mecca",
                    country = "Saudi Arabia",
                    method = 4
                )
                if (response.code == 200 && response.data != null) {
                    val h = response.data.date.hijri
                    withContext(Dispatchers.Main) {
                        hijriDate = "${h.day} ${h.month.ar} ${h.year}"
                    }
                }
            } catch (_: Exception) {}
        }
    }

    // Random refresh Ayah of the day
    fun loadAyahOfDay(forceRandom: Boolean = false) {
        if (isAyahLoading) return
        isAyahLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // AOD_PICKS is matched with random verses
                val dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                val index = if (forceRandom) {
                    Random().nextInt(SURAH_LIST.size)
                } else {
                    (dayOfMonth) % SURAH_LIST.size
                }
                val randomSurah = SURAH_LIST[index]
                val randomAyahNum = Random().nextInt(randomSurah.verses) + 1

                val res = NetworkClient.alquranCloud.getAyahUthmani("${randomSurah.number}:$randomAyahNum")
                if (res.code == 200 && res.data != null) {
                    withContext(Dispatchers.Main) {
                        ayahOfDayText = res.data.text
                        ayahOfDayRef = "سورۃ ${res.data.surah.name} • آية ${res.data.numberInSurah}"
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        ayahOfDayText = "﴿ إِنَّا أَنزَلْنَاهُ فِي لَيْلَةِ الْقَدْرِ ﴾"
                        ayahOfDayRef = "سورۃ القدر • آية ١"
                    }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    ayahOfDayText = "﴿ إِنَّا أَنزَلْنَاهُ فِي لَيْلَةِ الْقَدْرِ ﴾"
                    ayahOfDayRef = "سورۃ القدر • آية ١"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isAyahLoading = false
                }
            }
        }
    }

    // Surah View
    fun loadSurah(num: Int) {
        currentSurahNum = num
        isSurahLoading = true
        surahError = null
        loadedAyahs = emptyList()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = NetworkClient.alquranCloud.getSurahUthmani(num)
                if (result.code == 200 && result.data != null) {
                    withContext(Dispatchers.Main) {
                        loadedAyahs = result.data.ayahs
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        surahError = "حدث خطأ أثناء تحميل السورة الكريمة"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    surahError = e.localizedMessage ?: "تعذر الإتصال بالخادم الرئيسي"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isSurahLoading = false
                }
            }
        }
    }

    private fun loadReciters() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val apiLang = when (lang) {
                    "en" -> "eng"
                    "ur" -> "ur"
                    "bn" -> "bn"
                    else -> "ar"
                }
                val result = NetworkClient.mp3quran.getReciters(apiLang)
                val filtered = result.reciters.filter { rec ->
                    rec.moshaf.any { m -> m.surahList.split(",").size >= 100 }
                }
                withContext(Dispatchers.Main) {
                    recitersList = filtered
                    // Preselect beautiful reciter Al-Afasy if found
                    val defReciter = filtered.find { it.name.contains("العفاسي") || it.name.contains("Afasy") }
                        ?: filtered.firstOrNull()
                    selectedReciter = defReciter
                    if (defReciter != null) {
                        val activeSurah = currentSurahNum
                        if (activeSurah != null) {
                            loadSurahTimingOffsets(activeSurah, defReciter.moshaf.first().id)
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun selectReciter(reciter: RawReciter) {
        selectedReciter = reciter
        val sn = currentSurahNum ?: return
        val mId = reciter.moshaf.firstOrNull()?.id ?: return
        loadSurahTimingOffsets(sn, mId)
    }

    private fun loadSurahTimingOffsets(surahNum: Int, readId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val timings = NetworkClient.mp3quran.getAyahTimings(surahNum, readId)
                withContext(Dispatchers.Main) {
                    loadedAyahTimings = timings.filter { it.ayah > 0 }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    loadedAyahTimings = emptyList()
                }
            }
        }
    }

    // AUDIO SYSTEM
    fun playSurahAudio() {
        val sn = currentSurahNum ?: return
        val rec = selectedReciter ?: return
        val moshaf = rec.moshaf.firstOrNull() ?: return
        val serverUrl = moshaf.server
        val finalUrl = "${serverUrl}${String.format(Locale.US, "%03d", sn)}.mp3"

        playUrl(finalUrl)
    }

    fun playAyahAudio(ayah: AyahData) {
        val sn = currentSurahNum ?: return
        val rec = selectedReciter ?: return
        val moshaf = rec.moshaf.firstOrNull() ?: return
        val serverUrl = moshaf.server
        val finalUrl = "${serverUrl}${String.format(Locale.US, "%03d", sn)}.mp3"

        if (currentMediaPlayingUrl != finalUrl) {
            viewModelScope.launch {
                playUrl(finalUrl)
                // seek to target
                delay(500)
                seekToAyah(ayah.numberInSurah)
            }
        } else {
            seekToAyah(ayah.numberInSurah)
        }
    }

    private fun playUrl(url: String) {
        try {
            stopLoopRecitation()
            mediaPlayer?.release()
            mediaPlayer = null

            currentMediaPlayingUrl = url
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setOnErrorListener { _, _, _ ->
                    isPlayingAudio = false
                    true
                }
                setDataSource(url)
                setOnPreparedListener { mp ->
                    try {
                        audioDurationSec = mp.duration / 1000f
                        mp.start()
                        isPlayingAudio = true
                        startUpdateProgressJob()
                    } catch (_: Exception) {}
                }
                setOnCompletionListener {
                    isPlayingAudio = false
                }
                prepareAsync()
            }
        } catch (_: Exception) {}
    }

    fun togglePlayPauseAudio() {
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.pause()
                    isPlayingAudio = false
                } else {
                    mp.start()
                    isPlayingAudio = true
                    startUpdateProgressJob()
                }
            }
        } catch (_: Exception) {}
    }

    fun stopAudio() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        isPlayingAudio = false
        currentlyPlayingAyahNum = 0
    }

    fun seekToPosition(progressPercent: Float) {
        try {
            mediaPlayer?.let { mp ->
                val targetMs = (progressPercent / 100f * mp.duration).toInt()
                mp.seekTo(targetMs)
            }
        } catch (_: Exception) {}
    }

    private fun seekToAyah(ayahNum: Int) {
        try {
            val matchingTiming = loadedAyahTimings.find { it.ayah == ayahNum }
            matchingTiming?.let { tm ->
                mediaPlayer?.seekTo(tm.startTime)
                currentlyPlayingAyahNum = ayahNum
            }
        } catch (_: Exception) {}
    }

    fun skipCurrentAyah(direction: Int) {
        val nextNum = currentlyPlayingAyahNum + direction
        val maxAyahs = loadedAyahs.size
        if (nextNum in 1..maxAyahs) {
            seekToAyah(nextNum)
        }
    }

    private fun startUpdateProgressJob() {
        viewModelScope.launch(Dispatchers.Default) {
            while (isPlayingAudio) {
                try {
                    val mp = mediaPlayer
                    if (mp != null && mp.isPlaying) {
                        val currentMs = mp.currentPosition
                        val currentS = currentMs / 1000f
                        val timingOffset = loadedAyahTimings
                        var activeAyah = 0
                        for (i in timingOffset.indices) {
                            val activeOffset = timingOffset[i]
                            val startS = activeOffset.startTime / 1000f
                            val endS = if (i < timingOffset.size - 1) timingOffset[i + 1].startTime / 1000f else Float.MAX_VALUE
                            if (currentS >= startS && currentS < endS) {
                                activeAyah = activeOffset.ayah
                                break
                            }
                        }
                        withContext(Dispatchers.Main) {
                            audioCurrentTimeSec = currentS
                            if (activeAyah > 0) {
                                currentlyPlayingAyahNum = activeAyah
                            }
                        }
                    }
                } catch (_: Exception) {
                    break
                }
                delay(200)
            }
        }
    }

    // memorizing loop player
    fun startRepeatSession() {
        stopAudio()
        isLoopActive = true
        isRepeatPaused = false
        activeRepeatNum = 0
        activeRepeatStatus = "جاري تحضير ملفات التلاوة..."
        activeRepeatAyahText = ""

        loopJob?.cancel()
        loopJob = viewModelScope.launch(Dispatchers.Default) {
            if (loopMode == "ayah") {
                val surahNum = currentSurahNum ?: 1
                val targetReciter = selectedReciter ?: recitersList.firstOrNull() ?: return@launch
                val moshaf = targetReciter.moshaf.firstOrNull() ?: return@launch
                val url = "${moshaf.server}${String.format(Locale.US, "%03d", surahNum)}.mp3"

                // Load custom Surah texts
                var localAyatTextMap = mapOf<Int, String>()
                try {
                    val data = NetworkClient.alquranCloud.getSurahUthmani(surahNum)
                    if (data.code == 200 && data.data != null) {
                        localAyatTextMap = data.data.ayahs.associate { it.numberInSurah to it.text }
                    }
                } catch (_: Exception) {}

                // Build aggregate display phrasing
                val sTitle = SURAH_LIST.find { it.number == surahNum }?.localizedName(lang) ?: ""
                withContext(Dispatchers.Main) {
                    activeRepeatTitle = "تلقين سورة $sTitle"
                }

                // Execute repeat times
                for (rep in 1..loopTimes) {
                    if (!isLoopActive) break
                    while (isRepeatPaused) delay(300)

                    withContext(Dispatchers.Main) {
                        activeRepeatNum = rep
                        activeRepeatStatus = "تكرار التلاوة جاري: $rep من أصل $loopTimes"
                    }

                    // Loop through range of verses
                    val start = Math.min(loopFromAyah, loopToAyah)
                    val toClamp = SURAH_LIST.find { it.number == surahNum }?.verses ?: 7
                    val end = Math.min(Math.max(loopFromAyah, loopToAyah), toClamp)

                    val rangeText = (start..end).joinToString(" ") { verseNum ->
                        val text = localAyatTextMap[verseNum] ?: ""
                        "$text ﴿${NumbersArabicConverter.convert(verseNum)}﴾"
                    }
                    withContext(Dispatchers.Main) {
                        activeRepeatAyahText = rangeText
                    }

                    // Get timestamps
                    val timingFrom = loadedAyahTimings.find { it.ayah == start }
                    val timingToNext = loadedAyahTimings.find { it.ayah == end + 1 }
                    val startMs = timingFrom?.startTime ?: 0
                    val endMs = timingToNext?.startTime ?: -1

                    playRangeMsSynchronous(url, startMs, endMs)
                    if (rep < loopTimes && isLoopActive) delay(900)
                }

            } else {
                // Surah repetition
                val startS = Math.min(loopFromSurahNum, loopToSurahNum)
                val endS = Math.max(loopFromSurahNum, loopToSurahNum)
                val targetReciter = selectedReciter ?: recitersList.firstOrNull() ?: return@launch
                val moshaf = targetReciter.moshaf.firstOrNull() ?: return@launch
                val totalS = (endS - startS + 1) * loopTimes
                var totalCompleted = 0

                for (rep in 1..loopTimes) {
                    if (!isLoopActive) break
                    for (sn in startS..endS) {
                        if (!isLoopActive) break
                        while (isRepeatPaused) delay(300)

                        val info = SURAH_LIST.find { it.number == sn } ?: continue
                        val label = info.localizedName(lang)
                        totalCompleted++

                        withContext(Dispatchers.Main) {
                            activeRepeatTitle = "تكرار السور: $label"
                            activeRepeatNum = totalCompleted
                            activeRepeatStatus = "تلاوة سورة $label • التكرار $rep من أصل $loopTimes"
                        }

                        // Load text preview
                        try {
                            val data = NetworkClient.alquranCloud.getSurahUthmani(sn)
                            if (data.code == 200 && data.data != null) {
                                val previewText = data.data.ayahs.take(5).joinToString(" ") { a ->
                                    "${a.text} ﴿${NumbersArabicConverter.convert(a.numberInSurah)}﴾"
                                }
                                withContext(Dispatchers.Main) {
                                    activeRepeatAyahText = previewText + (if (info.verses > 5) "..." else "")
                                }
                            }
                        } catch (_: Exception) {}

                        val url = "${moshaf.server}${String.format(Locale.US, "%03d", sn)}.mp3"
                        playRangeMsSynchronous(url, 0, -1)
                        if (isLoopActive) delay(600)
                    }
                }
            }

            // Cleanup & mark done
            withContext(Dispatchers.Main) {
                activeRepeatStatus = "اكتمل التلاوة والتكرار بنجاح! جزاك الله خيراً"
                delay(2000)
                stopLoopRecitation()
            }
        }
    }

    private suspend fun playRangeMsSynchronous(url: String, startMs: Int, endMs: Int) {
        val completedEvent = CompletableDeferred<Unit>()
        val parentScope = CoroutineScope(Dispatchers.IO + Job())

        withContext(Dispatchers.Main) {
            try {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setOnErrorListener { _, _, _ ->
                        completedEvent.complete(Unit)
                        true
                    }
                    setDataSource(url)
                    setOnPreparedListener { mp ->
                        try {
                            mp.seekTo(startMs)
                            mp.start()
                            parentScope.launch {
                                try {
                                    while (isLoopActive) {
                                        var isPlaying = false
                                        var currentPos = 0
                                        try {
                                            isPlaying = mp.isPlaying
                                            currentPos = mp.currentPosition
                                        } catch (_: Exception) {
                                            break
                                        }

                                        if (!isPlaying && !isRepeatPaused) {
                                            break
                                        }

                                        if (endMs > 0 && currentPos >= endMs) {
                                            try { mp.pause() } catch (_: Exception) {}
                                            break
                                        }

                                        while (isRepeatPaused) {
                                            try {
                                                if (mp.isPlaying) mp.pause()
                                            } catch (_: Exception) {}
                                            delay(100)
                                        }

                                        try {
                                            if (!mp.isPlaying && isLoopActive && !isRepeatPaused) {
                                                mp.start()
                                            }
                                        } catch (_: Exception) {}

                                        delay(150)
                                    }
                                } catch (_: Exception) {
                                } finally {
                                    completedEvent.complete(Unit)
                                }
                            }
                        } catch (e: Exception) {
                            completedEvent.complete(Unit)
                        }
                    }
                    setOnCompletionListener {
                        completedEvent.complete(Unit)
                    }
                    prepareAsync()
                }
            } catch (e: Exception) {
                completedEvent.complete(Unit)
            }
        }

        // Wait until completion
        completedEvent.await()
        parentScope.cancel()
    }

    fun toggleRepeatPause() {
        isRepeatPaused = !isRepeatPaused
        try {
            mediaPlayer?.let { mp ->
                if (isRepeatPaused) {
                    if (mp.isPlaying) mp.pause()
                } else {
                    if (!mp.isPlaying) mp.start()
                }
            }
        } catch (_: Exception) {}
    }

    fun stopLoopRecitation() {
        loopJob?.cancel()
        loopJob = null
        isLoopActive = false
        isRepeatPaused = false
        activeRepeatAyahText = ""
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
    }

    override fun onCleared() {
        super.onCleared()
        stopAudio()
        stopLoopRecitation()
        prayerTimerJob?.cancel()
    }
}

// Arabic numerical glyph mapper for Ayah count circles
object NumbersArabicConverter {
    fun convert(n: Int): String {
        return n.toString().map { d ->
            "٠١٢٣٤٥٦٧٨٩"[d - '0']
        }.joinToString("")
    }
}

// Global static dhikr lists matched exactly to the provided HTML Tasbih targets
val TASBIH_LIST = listOf(
    AdhkarItem(1, "سُبْحَانَ اللَّهِ", 33, ""),
    AdhkarItem(2, "الْحَمْدُ لِلَّهِ", 33, ""),
    AdhkarItem(3, "اللَّهُ أَكْبَرُ", 34, ""),
    AdhkarItem(4, "لاَ إِلَهَ إِلاَّ اللَّهُ", 100, ""),
    AdhkarItem(5, "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ", 100, ""),
    AdhkarItem(6, "أَسْتَغْفِرُ اللَّهَ", 100, ""),
    AdhkarItem(7, "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ", 100, ""),
    AdhkarItem(8, "لاَ حَوْلَ وَلاَ قُوَّةَ إِلاَّ بِاللَّهِ", 100, "")
)
