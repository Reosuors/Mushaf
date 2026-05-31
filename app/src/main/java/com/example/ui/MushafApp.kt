package com.example.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MushafApp(vm: MainViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showThemeSheet by remember { mutableStateOf(false) }
    var showAsmaDetail by remember { mutableStateOf<AsmaName?>(null) }
    var showRepeatSettings by remember { mutableStateOf(false) }

    // Multi-Language Strings helper
    val localizedSectionList = mapOf(
        "prayer" to when (vm.lang) {
            "en" -> "Prayer"
            "ur" -> "نماز"
            "bn" -> "নামাজ"
            else -> "الصلاة"
        },
        "quran" to when (vm.lang) {
            "en" -> "Quran"
            "ur" -> "قرآن"
            "bn" -> "কুরআন"
            else -> "القرآن"
        },
        "azkar" to when (vm.lang) {
            "en" -> "Azkar"
            "ur" -> "اذکار"
            "bn" -> "আযকার"
            else -> "الأذكار"
        },
        "tasbih" to when (vm.lang) {
            "en" -> "Tasbih"
            "ur" -> "تسبیح"
            "bn" -> "তাসবীহ"
            else -> "التسبيح"
        },
        "asma" to when (vm.lang) {
            "en" -> "Names"
            "ur" -> "اسماء"
            "bn" -> "নামসমূহ"
            else -> "الأسماء"
        },
        "support" to when (vm.lang) {
            "en" -> "Support"
            "ur" -> "حمایت"
            "bn" -> "সহায়তা"
            else -> "ادعمنا"
        }
    )

    MushafTheme(themeColors = vm.activeTheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (vm.lang) {
                                "en" -> "Mushaf"
                                "ur" -> "مصحف"
                                "bn" -> "মুসহাফ"
                                else -> "مصحف"
                            },
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = vm.activeTheme.primary
                        )
                    },
                    actions = {
                        // Hijri Date display
                        Text(
                            text = vm.hijriDate,
                            fontSize = 11.sp,
                            color = vm.activeTheme.textSecondary,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .background(
                                    vm.activeTheme.primary.copy(alpha = 0.08f),
                                    RoundedCornerShape(6.dp)
                                )
                                .border(1.dp, vm.activeTheme.border, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )

                        // Theme switch triggering card
                        IconButton(onClick = { showThemeSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Themes",
                                tint = vm.activeTheme.primary
                            )
                        }

                        // Localized Language Select menu
                        var showLangDropdown by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { showLangDropdown = true }) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = "Language",
                                    tint = vm.activeTheme.textSecondary
                                )
                            }
                            DropdownMenu(
                                expanded = showLangDropdown,
                                onDismissRequest = { showLangDropdown = false },
                                modifier = Modifier.background(vm.activeTheme.surface)
                            ) {
                                val languages = listOf(
                                    Triple("ar", "🇸🇦", "العربية"),
                                    Triple("en", "🇬🇧", "English"),
                                    Triple("ur", "🇵🇰", "اردو"),
                                    Triple("bn", "🇧🇩", "বাংলা")
                                )
                                languages.forEach { (code, flag, name) ->
                                    DropdownMenuItem(
                                        text = { Text("$flag $name", color = vm.activeTheme.text) },
                                        onClick = {
                                            vm.changeLanguage(code)
                                            showLangDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = vm.activeTheme.background,
                        titleContentColor = vm.activeTheme.text
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = vm.activeTheme.background,
                    tonalElevation = 8.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    val navItems = listOf(
                        Triple("prayer", Icons.Default.Place, localizedSectionList["prayer"]),
                        Triple("quran", Icons.Default.Book, localizedSectionList["quran"]),
                        Triple("azkar", Icons.Default.VolunteerActivism, localizedSectionList["azkar"]),
                        Triple("tasbih", Icons.Default.Refresh, localizedSectionList["tasbih"]),
                        Triple("asma", Icons.Default.Star, localizedSectionList["asma"]),
                        Triple("support", Icons.Default.Favorite, localizedSectionList["support"])
                    )

                    navItems.forEach { (sec, icon, label) ->
                        NavigationBarItem(
                            selected = vm.currentSection == sec,
                            onClick = {
                                if (vm.currentSection == "quran" && vm.currentSurahNum != null && sec == "quran") {
                                    // Go back to surah list if re-clicking Quran tab
                                    vm.currentSurahNum = null
                                } else {
                                    vm.currentSection = sec
                                }
                                vm.performHapticFeedback(15)
                            },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (vm.currentSection == sec) vm.activeTheme.primary else vm.activeTheme.textMuted
                                )
                            },
                            label = {
                                Text(
                                    text = label ?: "",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (vm.currentSection == sec) vm.activeTheme.primary else vm.activeTheme.textMuted
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = vm.activeTheme.primary.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            },
            containerColor = vm.activeTheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Section Router
                when (vm.currentSection) {
                    "prayer" -> PrayerSection(vm)
                    "quran" -> QuranSection(vm, onShowRepeat = { showRepeatSettings = true })
                    "azkar" -> AzkarSection(vm)
                    "tasbih" -> TasbihSection(vm)
                    "asma" -> AsmaSection(vm, onNameClick = { showAsmaDetail = it })
                    "support" -> SupportSection(vm)
                }

                // Global Audio Bar floating on top of current view (above nav)
                AnimatedVisibility(
                    visible = vm.isPlayingAudio || vm.currentlyPlayingAyahNum > 0,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    AudioPlayerBar(vm)
                }

                // Inline FULLSCREEN repetition player loop (overlaying everything)
                AnimatedVisibility(
                    visible = vm.isLoopActive,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    RepeatProgressScreen(vm)
                }
            }
        }

        // Themes Sheet Menu (Bottom Sheet alternative using customized Dialog)
        if (showThemeSheet) {
            AlertDialog(
                onDismissRequest = { showThemeSheet = false },
                confirmButton = {
                    TextButton(onClick = { showThemeSheet = false }) {
                        Text(
                            text = if (vm.lang == "en") "Close" else "إغلاق",
                            color = vm.activeTheme.primary
                        )
                    }
                },
                title = {
                    Text(
                        text = if (vm.lang == "en") "Select Theme" else "🎨 اختر الثيم المفضل",
                        color = vm.activeTheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .height(280.dp)
                            .fillMaxWidth()
                    ) {
                        items(ALL_THEMES) { themeConfig ->
                            val isActive = themeConfig.id == vm.themeId
                            Card(
                                onClick = {
                                    vm.changeTheme(themeConfig.id)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = if (isActive) 2.dp else 1.dp,
                                        color = if (isActive) vm.activeTheme.primary else themeConfig.border,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                colors = CardDefaults.cardColors(containerColor = themeConfig.cardBg)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = if (vm.lang == "ar") themeConfig.nameAr else themeConfig.nameEn,
                                            color = themeConfig.text,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(themeConfig.primary)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(themeConfig.background)
                                            )
                                        }
                                    }
                                    if (isActive) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = themeConfig.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                containerColor = vm.activeTheme.surface
            )
        }

        // Asma Detail Bottom Sheet Menu
        showAsmaDetail?.let { asmaName ->
            AlertDialog(
                onDismissRequest = { showAsmaDetail = null },
                confirmButton = {
                    TextButton(onClick = { showAsmaDetail = null }) {
                        Text(
                            text = if (vm.lang == "en") "Close" else "إغلاق",
                            color = vm.activeTheme.primary
                        )
                    }
                },
                title = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${NumbersArabicConverter.convert(asmaName.number)} / ٩٩",
                            color = vm.activeTheme.textMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = asmaName.arabic,
                            color = vm.activeTheme.primary,
                            fontFamily = FontFamily.Serif,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = asmaName.transliteration,
                            color = vm.activeTheme.textSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = vm.activeTheme.border)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = asmaName.localizedMeaning(vm.lang),
                            color = vm.activeTheme.text,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    vm.activeTheme.primary.copy(alpha = 0.05f),
                                    RoundedCornerShape(10.dp)
                                )
                                .border(1.dp, vm.activeTheme.border.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = when (vm.lang) {
                                    "en" -> "Benefits: reciting Allah's Name brings tranquility, protection, and boundless grace to the believer's house."
                                    "ur" -> "برکات: اللہ کے اس نام کا ورد سکونِ قلب، برکت اور ہر قسم کی حفاظت کے لیے اکسیر ہے۔"
                                    "bn" -> "বরকত: আল্লাহর এই নাম জিকির করার মাধ্যমে অন্তরে অফুরন্ত প্রশান্তি এবং পারিবারিক নেয়ামত অর্জিত হয়।"
                                    else -> "الفضل والبركة: ملازمة هذا الاسم العظيم تجلب السكينة، تفرج الكروب، وتزيد البركة في الرزق والدار."
                                },
                                color = vm.activeTheme.textSecondary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                },
                containerColor = vm.activeTheme.surface
            )
        }

        // Memorization Recitation range setting sheet
        if (showRepeatSettings) {
            AlertDialog(
                onDismissRequest = { showRepeatSettings = false },
                confirmButton = {
                    Button(
                        onClick = {
                            showRepeatSettings = false
                            vm.startRepeatSession()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = vm.activeTheme.primary)
                    ) {
                        Text(
                            text = if (vm.lang == "en") "Start" else "ابدأ التكرار والتلقين",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRepeatSettings = false }) {
                        Text(
                            text = if (vm.lang == "en") "Cancel" else "إلغاء",
                            color = vm.activeTheme.textSecondary
                        )
                    }
                },
                title = {
                    Text(
                        text = if (vm.lang == "en") "Quranic Memorization Loop" else "🔁 إعدادات تكرار التلاوة",
                        color = vm.activeTheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Select repeat mode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(vm.activeTheme.cardBg, RoundedCornerShape(12.dp))
                                .padding(4.dp)
                        ) {
                            val activeStyle = ButtonDefaults.buttonColors(containerColor = vm.activeTheme.primary)
                            val inactiveStyle = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                            Button(
                                onClick = { vm.loopMode = "ayah" },
                                colors = if (vm.loopMode == "ayah") activeStyle else inactiveStyle,
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = if (vm.lang == "en") "Repeat Ayahs" else "تكرار آيات",
                                    color = if (vm.loopMode == "ayah") Color.Black else vm.activeTheme.textSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Button(
                                onClick = { vm.loopMode = "surah" },
                                colors = if (vm.loopMode == "surah") activeStyle else inactiveStyle,
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = if (vm.lang == "en") "Repeat Surahs" else "تكرار سور",
                                    color = if (vm.loopMode == "surah") Color.Black else vm.activeTheme.textSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (vm.loopMode == "ayah") {
                            // Target Ayahs range slider input values
                            val activeSurah = SURAH_LIST.find { it.number == (vm.currentSurahNum ?: 1) }
                            val totalVerses = activeSurah?.verses ?: 7
                            val title = activeSurah?.localizedName(vm.lang) ?: ""

                            Text(
                                text = "${if (vm.lang == "en") "Surah" else "السورة"}: $title",
                                color = vm.activeTheme.text,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = vm.loopFromAyah.toString(),
                                    onValueChange = {
                                        vm.loopFromAyah = it.toIntOrNull()?.coerceIn(1, totalVerses) ?: 1
                                    },
                                    label = { Text(if (vm.lang == "en") "From Ayah" else "من آية") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = vm.activeTheme.primary,
                                        unfocusedBorderColor = vm.activeTheme.border
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = vm.loopToAyah.toString(),
                                    onValueChange = {
                                        vm.loopToAyah = it.toIntOrNull()?.coerceIn(1, totalVerses) ?: totalVerses
                                    },
                                    label = { Text(if (vm.lang == "en") "To Ayah" else "إلى آية") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = vm.activeTheme.primary,
                                        unfocusedBorderColor = vm.activeTheme.border
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            // Surah repetition config
                            Text(
                                text = if (vm.lang == "en") "Repeats whole Surahs sequentially" else "تلاوة سور كاملة متوالية وتكرارها",
                                color = vm.activeTheme.textSecondary,
                                fontSize = 11.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    var showS1Drop by remember { mutableStateOf(false) }
                                    val activeSurahName = SURAH_LIST.find { it.number == vm.loopFromSurahNum }?.localizedName(vm.lang) ?: ""
                                    OutlinedButton(
                                        onClick = { showS1Drop = true },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("$activeSurahName (${vm.loopFromSurahNum})", color = vm.activeTheme.text, fontSize = 11.sp)
                                    }
                                    DropdownMenu(expanded = showS1Drop, onDismissRequest = { showS1Drop = false }) {
                                        SURAH_LIST.take(20).forEach { s ->
                                            DropdownMenuItem(
                                                text = { Text("${s.number}. ${s.localizedName(vm.lang)}") },
                                                onClick = {
                                                    vm.loopFromSurahNum = s.number
                                                    showS1Drop = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Box(modifier = Modifier.weight(1f)) {
                                    var showS2Drop by remember { mutableStateOf(false) }
                                    val activeSurahName = SURAH_LIST.find { it.number == vm.loopToSurahNum }?.localizedName(vm.lang) ?: ""
                                    OutlinedButton(
                                        onClick = { showS2Drop = true },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("$activeSurahName (${vm.loopToSurahNum})", color = vm.activeTheme.text, fontSize = 11.sp)
                                    }
                                    DropdownMenu(expanded = showS2Drop, onDismissRequest = { showS2Drop = false }) {
                                        SURAH_LIST.take(20).forEach { s ->
                                            DropdownMenuItem(
                                                text = { Text("${s.number}. ${s.localizedName(vm.lang)}") },
                                                onClick = {
                                                    vm.loopToSurahNum = s.number
                                                    showS2Drop = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Loop repeat counts
                        OutlinedTextField(
                            value = vm.loopTimes.toString(),
                            onValueChange = {
                                vm.loopTimes = it.toIntOrNull()?.coerceIn(1, 100) ?: 3
                            },
                            label = { Text(if (vm.lang == "en") "Repeat Times" else "عدد مرات تكرار التلاوة") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = vm.activeTheme.primary,
                                unfocusedBorderColor = vm.activeTheme.border
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Beautiful reciter info in Loop Config
                        val rName = vm.selectedReciter?.name ?: "العفاسي"
                        Text(
                            text = "${if (vm.lang == "en") "Selected Reciter" else "القارئ النشط"}: $rName",
                            color = vm.activeTheme.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                containerColor = vm.activeTheme.surface
            )
        }
    }
}

// ==========================================
// 1. PRAYER SECTION
// ==========================================
@Composable
fun PrayerSection(vm: MainViewModel) {
    val context = LocalContext.current
    var coordsGranted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Location Manual Lookup Inputs Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = vm.activeTheme.cardBg),
            border = BorderStroke(1.dp, vm.activeTheme.border)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🕌", fontSize = 24.sp)
                    Text(
                        text = if (vm.lang == "en") "Prayer Times Database" else "مواقيت الصلاة الدقيقة",
                        style = MaterialTheme.typography.titleMedium,
                        color = vm.activeTheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = vm.cityInput,
                        onValueChange = { vm.cityInput = it },
                        label = { Text(if (vm.lang == "en") "City Name" else "اسم المدينة") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = vm.activeTheme.primary,
                            unfocusedBorderColor = vm.activeTheme.border
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = vm.countryInput,
                        onValueChange = { vm.countryInput = it },
                        label = { Text(if (vm.lang == "en") "Country" else "الدولة") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = vm.activeTheme.primary,
                            unfocusedBorderColor = vm.activeTheme.border
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { vm.searchPrayerTimes() },
                        colors = ButtonDefaults.buttonColors(containerColor = vm.activeTheme.primary),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (vm.lang == "en") "Search" else "بحث",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Simulated GPS Search to bypass instrumented location configurations safely
                    OutlinedButton(
                        onClick = {
                            // simulated coordinates fallback to preserve robust and compiler-safe state execution
                            vm.searchLocationByGps(21.4225, 39.8262) // Coordinates of Mecca
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = vm.activeTheme.primary),
                        border = BorderStroke(1.dp, vm.activeTheme.border),
                        modifier = Modifier.width(52.dp),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(imageVector = Icons.Default.MyLocation, contentDescription = "Mecca Coordinates")
                    }
                }

                // Preset select options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (vm.lang == "en") "Browse preset capitals" else "أو اختر العواصم الإسلامية مباشرة:",
                        fontSize = 11.sp,
                        color = vm.activeTheme.textSecondary
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(
                        "Mecca" to "Saudi Arabia",
                        "Cairo" to "Egypt",
                        "Madinah" to "Saudi Arabia",
                        "Jerusalem" to "Palestine",
                        "Jakarta" to "Indonesia",
                        "Islamabad" to "Pakistan",
                        "Dhaka" to "Bangladesh",
                        "Riyadh" to "Saudi Arabia",
                        "Baghdad" to "Iraq",
                        "Damascus" to "Syria",
                        "Istanbul" to "Turkey",
                        "Beirut" to "Lebanon",
                        "Kuwait" to "Kuwait"
                    )
                    presets.forEach { (city, country) ->
                        Box(
                            modifier = Modifier
                                .background(
                                    vm.activeTheme.primary.copy(alpha = 0.08f),
                                    RoundedCornerShape(8.dp)
                                )
                                .border(1.dp, vm.activeTheme.border, RoundedCornerShape(8.dp))
                                .clickable {
                                    vm.cityInput = city
                                    vm.countryInput = country
                                    vm.searchPrayerTimes()
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (vm.lang == "ar") when (city) {
                                    "Mecca" -> "مكة المكرمة"
                                    "Cairo" -> "القاهرة"
                                    "Madinah" -> "المدينة المنورة"
                                    "Jerusalem" -> "القدس الشريف"
                                    "Jakarta" -> "جاكرتا"
                                    "Islamabad" -> "إسلام آباد"
                                    "Dhaka" -> "دكا"
                                    "Riyadh" -> "الرياض"
                                    "Baghdad" -> "بغداد"
                                    "Damascus" -> "دمشق"
                                    "Istanbul" -> "إسطنبول"
                                    "Beirut" -> "بيروت"
                                    "Kuwait" -> "الكويت"
                                    else -> city
                                } else city,
                                color = vm.activeTheme.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Location Info and Next Prayer countdown header card
        vm.prayerTimings?.let { timings ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = vm.activeTheme.primary.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, vm.activeTheme.primary.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "📍 ${vm.cityInput} • ${vm.countryInput}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = vm.activeTheme.primaryVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = vm.dateReadable,
                            fontSize = 12.sp,
                            color = vm.activeTheme.textSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when (vm.lang) {
                                "en" -> "Next Prayer"
                                "ur" -> "اگلی نماز"
                                "bn" -> "পরবর্তী নামাজ"
                                else -> "الصلاة القادمة"
                            },
                            fontSize = 11.sp,
                            color = vm.activeTheme.textSecondary
                        )
                        Text(
                            text = when (vm.nextPrayerName) {
                                "Fajr" -> if (vm.lang == "ar") "الفجر" else "Fajr"
                                "Sunrise" -> if (vm.lang == "ar") "الشروق" else "Sunrise"
                                "Dhuhr" -> if (vm.lang == "ar") "الظهر" else "Dhuhr"
                                "Asr" -> if (vm.lang == "ar") "العصر" else "Asr"
                                "Maghrib" -> if (vm.lang == "ar") "المغرب" else "Maghrib"
                                "Isha" -> if (vm.lang == "ar") "العشاء" else "Isha"
                                else -> vm.nextPrayerName
                            },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = vm.activeTheme.primary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Box(
                            modifier = Modifier
                                .background(
                                    vm.activeTheme.primary.copy(alpha = 0.15f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = vm.nextPrayerTime,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = vm.activeTheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "⏱ ${vm.countdownText}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = vm.activeTheme.success
                        )
                    }
                }
            }

            // Timings listing Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val arrayItems = listOf(
                    Triple("Fajr", if (vm.lang == "ar") "الفجر" else "Fajr", timings.fajr),
                    Triple("Sunrise", if (vm.lang == "ar") "الشروق" else "Sunrise", timings.sunrise),
                    Triple("Dhuhr", if (vm.lang == "ar") "الظهر" else "Dhuhr", timings.dhuhr),
                    Triple("Asr", if (vm.lang == "ar") "العصر" else "Asr", timings.asr),
                    Triple("Maghrib", if (vm.lang == "ar") "المغرب" else "Maghrib", timings.maghrib),
                    Triple("Isha", if (vm.lang == "ar") "العشاء" else "Isha", timings.isha)
                )

                arrayItems.forEach { (key, arabic, timeStr) ->
                    val isNext = vm.nextPrayerName == key
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isNext) 1.dp else 0.dp,
                                color = if (isNext) vm.activeTheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isNext) vm.activeTheme.primary.copy(alpha = 0.05f) else vm.activeTheme.cardBg
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val em = when (key) {
                                    "Fajr" -> "🌄"
                                    "Sunrise" -> "🌅"
                                    "Dhuhr" -> "🌞"
                                    "Asr" -> "🌤"
                                    "Maghrib" -> "🌇"
                                    else -> "🌙"
                                }
                                Text(em, fontSize = 20.sp)
                                Text(
                                    text = arabic,
                                    color = if (isNext) vm.activeTheme.primary else vm.activeTheme.text,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                if (isNext) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                vm.activeTheme.primary,
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (vm.lang == "en") "Next" else "القادمة",
                                            color = Color.Black,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Text(
                                text = timeStr,
                                color = if (isNext) vm.activeTheme.primary else vm.activeTheme.textSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        } ?: run {
            if (vm.isPrayerLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = vm.activeTheme.primary)
                }
            } else {
                // Empty instruction card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = vm.activeTheme.cardBg),
                    border = BorderStroke(1.dp, vm.activeTheme.border)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🕌", fontSize = 36.sp)
                        Text(
                            text = if (vm.lang == "en") "No Timings Cached" else "لم يتم تهيئة مواقيت الصلاة للبلد الحالي",
                            color = vm.activeTheme.primaryVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (vm.lang == "en") "Enter your city coordinates above to cache local Fajr/Maghrib timings instantly." else "اكتب اسم مدينتك واضغط بحث للتحميل المباشر والآمن لجدول الصلاة اليومية.",
                            color = vm.activeTheme.textSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Calculation Method configs wrapper
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            colors = CardDefaults.cardColors(containerColor = vm.activeTheme.cardBg),
            border = BorderStroke(1.dp, vm.activeTheme.border)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (vm.lang == "en") "Calculation Method" else "طريقة حساب الأوقات الفلكية",
                    color = vm.activeTheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )

                var expandMethod by remember { mutableStateOf(false) }
                val currentMethodLabel = when (vm.prayerMethod) {
                    1 -> "Karachi (Univ. of Islamic Sciences)"
                    2 -> "ISNA (Islamic Society of North America)"
                    3 -> "MWL (Muslim World League)"
                    4 -> "Umm Al-Qura (Makkah)"
                    5 -> "Egyptian General Authority"
                    7 -> "Tehran University"
                    11 -> "Singapore (MUIS)"
                    12 -> "France (UOIF)"
                    13 -> "Turkey (Diyanet)"
                    else -> "Ligue Mondiale"
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expandMethod = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = vm.activeTheme.text),
                        border = BorderStroke(1.dp, vm.activeTheme.border)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(currentMethodLabel, modifier = Modifier.weight(1f), fontSize = 11.sp, maxLines = 1)
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                        }
                    }

                    DropdownMenu(
                        expanded = expandMethod,
                        onDismissRequest = { expandMethod = false },
                        modifier = Modifier.background(vm.activeTheme.cardBg)
                    ) {
                        val items = listOf(
                            1 to "University of Islamic Sciences, Karachi",
                            2 to "ISNA (Islamic Society of North America)",
                            3 to "Muslim World League",
                            4 to "Umm Al-Qura University, Makkah",
                            5 to "Egyptian General Authority of Survey",
                            11 to "Majlis Ugama Islam Singapura (MUIS)",
                            12 to "Union Organisation Islamique de France",
                            13 to "Diyanet İşleri Başkanlığı (Turkey)"
                        )
                        items.forEach { (id, label) ->
                            DropdownMenuItem(
                                text = { Text(label, color = vm.activeTheme.text, fontSize = 11.sp) },
                                onClick = {
                                    vm.updateCalculationMethod(id)
                                    expandMethod = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. QURAN SECTION
// ==========================================
@Composable
fun QuranSection(vm: MainViewModel, onShowRepeat: () -> Unit) {
    if (vm.currentSurahNum != null) {
        SurahDetailSubScreen(vm, onShowRepeat = onShowRepeat)
    } else {
        SurahListSubScreen(vm)
    }
}

@Composable
fun SurahListSubScreen(vm: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Beautiful Premium Hero Banner using the mushaf_logo
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = vm.activeTheme.cardBg),
            border = BorderStroke(1.dp, vm.activeTheme.border)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                vm.activeTheme.primary.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(vm.activeTheme.surface)
                            .border(1.dp, vm.activeTheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = com.example.R.drawable.mushaf_logo),
                            contentDescription = "Mushaf logo",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = when (vm.lang) {
                                "en" -> "The Holy Quran"
                                "ur" -> "قرآن پاک"
                                "bn" -> "পবিত্র কুরআন"
                                else -> "القرآن الكريم"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = vm.activeTheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (vm.lang == "en") "Read and listen to high-quality audio recitation" else "تلاوة خاشعة بالرسم العثماني مع كبار القراء",
                            fontSize = 11.sp,
                            color = vm.activeTheme.textSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Surah Search input
        OutlinedTextField(
            value = vm.surahSearchQuery,
            onValueChange = { vm.surahSearchQuery = it },
            placeholder = { Text(if (vm.lang == "en") "Search for Surah..." else "ابحث باسم السورة أو رقمها...") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = vm.activeTheme.textMuted) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = vm.activeTheme.primary,
                unfocusedBorderColor = vm.activeTheme.border,
                focusedContainerColor = vm.activeTheme.cardBg,
                unfocusedContainerColor = vm.activeTheme.cardBg
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // Ayah of the Day beautiful card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = vm.activeTheme.primary.copy(alpha = 0.05f)),
            border = BorderStroke(1.dp, vm.activeTheme.primary.copy(alpha = 0.20f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✨ " + (if (vm.lang == "en") "Verse of the Day" else "آية اليوم العطرة"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = vm.activeTheme.primaryVariant
                    )
                    Text(
                        text = vm.ayahOfDayRef,
                        fontSize = 10.sp,
                        color = vm.activeTheme.textSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (vm.isAyahLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = vm.activeTheme.primary, strokeWidth = 2.dp)
                    }
                } else {
                    Text(
                        text = vm.ayahOfDayText,
                        fontFamily = FontFamily.Serif,
                        fontSize = 18.sp,
                        color = vm.activeTheme.text,
                        lineHeight = 28.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = { vm.loadAyahOfDay(true) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = vm.activeTheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Surah Items column List
        val filteredList = remember(vm.surahSearchQuery) {
            val q = vm.surahSearchQuery.trim().lowercase()
            if (q.isEmpty()) {
                SURAH_LIST
            } else {
                SURAH_LIST.filter { s ->
                    s.arabic.contains(q) ||
                    s.english.lowercase().contains(q) ||
                    s.number.toString() == q
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filteredList) { surah ->
                Card(
                    onClick = {
                        vm.loadSurah(surah.number)
                        vm.performHapticFeedback(15)
                    },
                    colors = CardDefaults.cardColors(containerColor = vm.activeTheme.cardBg),
                    border = BorderStroke(1.dp, vm.activeTheme.border)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Circular index tag
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(vm.activeTheme.primary.copy(alpha = 0.08f), CircleShape)
                                    .border(1.dp, vm.activeTheme.border, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = surah.number.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = vm.activeTheme.primary
                                )
                            }

                            Column {
                                Text(
                                    text = surah.localizedName(vm.lang),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = vm.activeTheme.text
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val badgeBg = if (surah.type == "مكية") {
                                        Color(0xFF8B6914).copy(alpha = 0.2f)
                                    } else {
                                        Color(0xFF377373).copy(alpha = 0.2f)
                                    }
                                    val badgeFg = if (surah.type == "مكية") {
                                        Color(0xFFE8C26E)
                                    } else {
                                        Color(0xFF7ECECE)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(badgeBg, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = surah.localizedType(vm.lang),
                                            color = badgeFg,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "${surah.verses} " + (if (vm.lang == "en") "verses" else "آية"),
                                        fontSize = 11.sp,
                                        color = vm.activeTheme.textSecondary
                                    )
                                }
                            }
                        }

                        // Play indicator icon
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Read",
                            tint = vm.activeTheme.primaryVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SurahDetailSubScreen(vm: MainViewModel, onShowRepeat: () -> Unit) {
    val surahNum = vm.currentSurahNum ?: return
    val surahInfo = SURAH_LIST.find { it.number == surahNum } ?: return

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(surahNum) {
        vm.loadSurah(surahNum)
        listState.scrollToItem(0)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Back toolbar header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(vm.activeTheme.cardBg)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(onClick = { vm.currentSurahNum = null }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = vm.activeTheme.text
                )
            }
            Column {
                Text(
                    text = "${if (vm.lang == "en") "Surah" else "سورة"} ${surahInfo.localizedName(vm.lang)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = vm.activeTheme.primary
                )
                Text(
                    text = "${surahInfo.verses} ${if (vm.lang == "en") "verses" else "آية"} • ${surahInfo.localizedType(vm.lang)}",
                    fontSize = 11.sp,
                    color = vm.activeTheme.textSecondary
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            // Loop Settings invocation button
            IconButton(onClick = onShowRepeat) {
                Icon(
                    imageVector = Icons.Default.Loop,
                    contentDescription = "Memories range",
                    tint = vm.activeTheme.primary
                )
            }
        }

        // Top control filters: font size, continuous mode, reciter select, Search verse
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(vm.activeTheme.cardBg.copy(alpha = 0.5f))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Font resize sliders and recitation mode triggers
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("A-", fontSize = 11.sp, color = vm.activeTheme.textSecondary)
                    Slider(
                        value = vm.quranFontSizeScale,
                        onValueChange = { vm.quranFontSizeScale = it },
                        valueRange = 1.1f..2.5f,
                        modifier = Modifier.width(100.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = vm.activeTheme.primary,
                            activeTrackColor = vm.activeTheme.primary
                        )
                    )
                    Text("A+", fontSize = 11.sp, color = vm.activeTheme.textSecondary)
                }

                // Continuous Vs Verse playback triggers
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val activeBg = vm.activeTheme.primary
                    val activeFg = Color.Black
                    val inactiveBg = vm.activeTheme.border
                    val inactiveFg = vm.activeTheme.textSecondary

                    Box(
                        modifier = Modifier
                            .background(
                                if (vm.isContinuousMode) activeBg else inactiveBg,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { vm.isContinuousMode = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (vm.lang == "en") "Continuous" else "تلاوة متواصلة",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (vm.isContinuousMode) activeFg else inactiveFg
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                if (!vm.isContinuousMode) activeBg else inactiveBg,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { vm.isContinuousMode = false }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (vm.lang == "en") "Verse-by-Verse" else "آية بآية",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!vm.isContinuousMode) activeFg else inactiveFg
                        )
                    }
                }
            }

            // Reciter Dynamic Selector Dropdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = (if (vm.lang == "en") "Reciter" else "القارئ") + ":",
                    fontSize = 11.sp,
                    color = vm.activeTheme.textSecondary
                )
                var showRecitersDropdown by remember { mutableStateOf(false) }
                val currentRecLabel = vm.selectedReciter?.name ?: (if (vm.lang == "en") "Choose Reciter" else "اختر القارئ")

                Box(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                    OutlinedButton(
                        onClick = { showRecitersDropdown = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, vm.activeTheme.border)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(currentRecLabel, modifier = Modifier.weight(1f), fontSize = 11.sp, maxLines = 1, color = vm.activeTheme.text)
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Drop", tint = vm.activeTheme.primary)
                        }
                    }

                    DropdownMenu(
                        expanded = showRecitersDropdown,
                        onDismissRequest = { showRecitersDropdown = false },
                        modifier = Modifier
                            .height(240.dp)
                            .width(280.dp)
                            .background(vm.activeTheme.surface)
                    ) {
                        if (vm.recitersList.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text(if (vm.lang == "en") "Loading List..." else "جاري جلب القراء...", color = vm.activeTheme.textSecondary) },
                                onClick = {}
                            )
                        } else {
                            vm.recitersList.forEach { rec ->
                                DropdownMenuItem(
                                    text = { Text(rec.name, color = vm.activeTheme.text, fontSize = 11.sp) },
                                    onClick = {
                                        vm.selectReciter(rec)
                                        showRecitersDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Jump to Verse inline dynamic search
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = vm.currentSurahQuery,
                    onValueChange = { query ->
                        vm.currentSurahQuery = query
                        val searchNum = query.toIntOrNull()
                        if (searchNum != null && searchNum in 1..surahInfo.verses) {
                            vm.showAyahDrop = true
                        } else {
                            vm.showAyahDrop = query.trim().isNotEmpty()
                        }
                    },
                    placeholder = { Text(if (vm.lang == "en") "Type verse number or Arabic text..." else "ابحث عن آية أو اكتب رقمها...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = vm.activeTheme.primary,
                        unfocusedBorderColor = vm.activeTheme.border,
                        focusedContainerColor = vm.activeTheme.surface,
                        unfocusedContainerColor = vm.activeTheme.surface
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    textStyle = TextStyle(fontSize = 11.sp),
                    singleLine = true
                )

                Button(
                    onClick = {
                        val num = vm.currentSurahQuery.trim().toIntOrNull()
                        if (num != null && num in 1..surahInfo.verses) {
                            scope.launch {
                                listState.animateScrollToItem(num)
                            }
                        }
                        vm.showAyahDrop = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = vm.activeTheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text(if (vm.lang == "en") "Go" else "اذهب", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Floating filtered dropdown list for verses lookup results
            if (vm.showAyahDrop && vm.currentSurahQuery.trim().isNotEmpty()) {
                val matchingAyat = remember(vm.currentSurahQuery) {
                    val q = vm.currentSurahQuery.trim()
                    val num = q.toIntOrNull()
                    if (num != null) {
                        vm.loadedAyahs.filter { it.numberInSurah == num }
                    } else {
                        vm.loadedAyahs.filter { it.text.contains(q) }.take(6)
                    }
                }

                if (matchingAyat.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(vm.activeTheme.background)
                            .border(1.dp, vm.activeTheme.border, RoundedCornerShape(6.dp))
                            .padding(4.dp)
                    ) {
                        matchingAyat.forEach { a ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch {
                                            listState.animateScrollToItem(a.numberInSurah)
                                        }
                                        vm.showAyahDrop = false
                                    }
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("آية ${a.numberInSurah}", color = vm.activeTheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(a.text.take(34) + "...", color = vm.activeTheme.text, fontSize = 11.sp, fontFamily = FontFamily.Serif)
                            }
                        }
                    }
                }
            }
        }

        // Scrollable Arabic Text verses view
        if (vm.isSurahLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = vm.activeTheme.primary)
            }
        } else if (vm.surahError != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(vm.surahError ?: "", color = Color.Red, textAlign = TextAlign.Center)
            }
        } else {
            MushafPageFrame(themeColors = vm.activeTheme) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Traditional Illuminated Surah Header Banner inside list
                    item {
                        SurahIntroCard(
                            themeColors = vm.activeTheme,
                            name = surahInfo.localizedName("ar"), // Always absolute Arabic name in calligraphy
                            verses = surahInfo.verses,
                            type = surahInfo.localizedType(vm.lang)
                        )
                    }

                    // Bismillah introduction (except Al-Tawbah)
                    if (surahNum != 9) {
                        item {
                            BismillahHeader(
                                themeColors = vm.activeTheme,
                                fontSize = (vm.quranFontSizeScale * 14.5f).sp
                            )
                        }
                    }

                    // Continuous Mushaf Reading Flow
                    if (vm.isContinuousMode) {
                        val chunks = vm.loadedAyahs.chunked(10)
                        items(chunks.size) { chunkIdx ->
                            val chunk = chunks[chunkIdx]
                            // Build beautiful continuous Arabic block
                            val annotatedString = buildAnnotatedString {
                                chunk.forEach { ayah ->
                                    val isPlaying = vm.currentlyPlayingAyahNum == ayah.numberInSurah
                                    val start = length
                                    append(ayah.text)
                                    val end = length
                                    
                                    addStringAnnotation(
                                        tag = "AYAH",
                                        annotation = ayah.numberInSurah.toString(),
                                        start = start,
                                        end = end
                                    )
                                    
                                    if (isPlaying) {
                                        addStyle(
                                            style = SpanStyle(
                                                color = vm.activeTheme.primary,
                                                fontWeight = FontWeight.Bold,
                                                background = vm.activeTheme.primary.copy(alpha = 0.18f)
                                            ),
                                            start = start,
                                            end = end
                                        )
                                    } else {
                                        addStyle(
                                            style = SpanStyle(
                                                color = vm.activeTheme.text
                                            ),
                                            start = start,
                                            end = end
                                        )
                                    }
                                    
                                    append(" ﴿")
                                    val numStart = length
                                    append(NumbersArabicConverter.convert(ayah.numberInSurah))
                                    val numEnd = length
                                    addStyle(
                                        style = SpanStyle(
                                            color = vm.activeTheme.primaryVariant,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        start = numStart,
                                        end = numEnd
                                    )
                                    append("﴾ ")
                                }
                            }
                            
                            ClickableText(
                                text = annotatedString,
                                style = TextStyle(
                                    fontFamily = FontFamily.Serif,
                                    fontSize = (vm.quranFontSizeScale * 14.5f).sp,
                                    lineHeight = (vm.quranFontSizeScale * 25f).sp,
                                    textAlign = TextAlign.Justify,
                                    textDirection = TextDirection.Rtl
                                ),
                                onClick = { offset ->
                                    annotatedString.getStringAnnotations(tag = "AYAH", start = offset, end = offset)
                                        .firstOrNull()?.let { annotation ->
                                            val ayahNum = annotation.item.toIntOrNull()
                                            if (ayahNum != null) {
                                                val matchingAyah = chunk.find { it.numberInSurah == ayahNum }
                                                if (matchingAyah != null) {
                                                    vm.playAyahAudio(matchingAyah)
                                                }
                                            }
                                        }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 6.dp)
                            )
                        }
                    } else {
                        // Verse-by-Verse Cards
                        items(vm.loadedAyahs) { ayah ->
                            val isPlaying = vm.currentlyPlayingAyahNum == ayah.numberInSurah
                            val verseTextFontSize = (vm.quranFontSizeScale * 14.5f).sp
                            
                            Card(
                                onClick = { vm.playAyahAudio(ayah) },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isPlaying) vm.activeTheme.primary.copy(alpha = 0.08f) else vm.activeTheme.cardBg
                                ),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isPlaying) vm.activeTheme.primary else vm.activeTheme.border
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .background(
                                                    if (isPlaying) vm.activeTheme.primary else vm.activeTheme.border.copy(alpha = 0.4f),
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = ayah.numberInSurah.toString(),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isPlaying) Color.Black else vm.activeTheme.primary
                                            )
                                        }
                                        if (isPlaying) {
                                            Text(
                                                text = if (vm.lang == "en") "Currently Reciting" else "تلاوة جارية الآن",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = vm.activeTheme.primary
                                            )
                                        }
                                    }
                                    
                                    Text(
                                        text = ayah.text,
                                        fontFamily = FontFamily.Serif,
                                        fontSize = verseTextFontSize,
                                        color = if (isPlaying) vm.activeTheme.primary else vm.activeTheme.text,
                                        lineHeight = (vm.quranFontSizeScale * 23f).sp,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. AZKAR SECTION
// ==========================================
@Composable
fun AzkarSection(vm: MainViewModel) {
    var azType by remember { mutableStateOf("sabah") } // "sabah", "masa", "nawm"

    val listData = when (azType) {
        "masa" -> AdhkarProvider.evening
        "nawm" -> AdhkarProvider.sleep
        else -> AdhkarProvider.morning
    }

    val progressCounts = when (azType) {
        "masa" -> vm.eveningCounts
        "nawm" -> vm.sleepCounts
        else -> vm.morningCounts
    }

    val totalCompletedCount = progressCounts.filterIndexed { index, current ->
        current >= listData[index].target
    }.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Tab selectors
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(vm.activeTheme.cardBg, RoundedCornerShape(14.dp))
                .padding(4.dp)
        ) {
            val activeStyle = ButtonDefaults.buttonColors(containerColor = vm.activeTheme.primary)
            val inactiveStyle = ButtonDefaults.buttonColors(containerColor = Color.Transparent)

            Button(
                onClick = { azType = "sabah" },
                colors = if (azType == "sabah") activeStyle else inactiveStyle,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                Text(
                    text = "🌅 " + (when (vm.lang) {
                        "en" -> "Morning"
                        "ur" -> "صبح"
                        "bn" -> "সকাল"
                        else -> "الصباح"
                    }),
                    color = if (azType == "sabah") Color.Black else vm.activeTheme.textSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick = { azType = "masa" },
                colors = if (azType == "masa") activeStyle else inactiveStyle,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                Text(
                    text = "🌇 " + (when (vm.lang) {
                        "en" -> "Evening"
                        "ur" -> "شام"
                        "bn" -> "সন্ধ্যা"
                        else -> "المساء"
                    }),
                    color = if (azType == "masa") Color.Black else vm.activeTheme.textSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick = { azType = "nawm" },
                colors = if (azType == "nawm") activeStyle else inactiveStyle,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                Text(
                    text = "🌙 " + (when (vm.lang) {
                        "en" -> "Sleep"
                        "ur" -> "نوم"
                        "bn" -> "ঘুমানো"
                        else -> "النوم"
                    }),
                    color = if (azType == "nawm") Color.Black else vm.activeTheme.textSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Metrics and Reset header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${if (vm.lang == "en") "Progress" else "المنجز"}: $totalCompletedCount / ${listData.size}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = vm.activeTheme.primary
            )
            OutlinedButton(
                onClick = { vm.resetAdhkarGroup(azType) },
                border = BorderStroke(1.dp, vm.activeTheme.border)
            ) {
                Text(
                    text = if (vm.lang == "en") "Reset" else "إعادة تعيين",
                    fontSize = 10.sp,
                    color = vm.activeTheme.primary
                )
            }
        }

        // Scrollable list of checking Adhkar cards
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(listData.size) { index ->
                val adhkar = listData[index]
                val count = progressCounts.getOrNull(index) ?: 0
                val isDone = count >= adhkar.target
                val completedPercent = (count.toFloat() / adhkar.target.toFloat()).coerceIn(0f, 1f)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDone) vm.activeTheme.primary.copy(alpha = 0.02f) else vm.activeTheme.cardBg
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isDone) vm.activeTheme.success.copy(alpha = 0.5f) else vm.activeTheme.border
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Local progress bar
                        LinearProgressIndicator(
                            progress = { completedPercent },
                            modifier = Modifier.fillMaxWidth().height(3.dp),
                            color = if (isDone) vm.activeTheme.success else vm.activeTheme.primary,
                            trackColor = Color.Transparent
                        )

                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(vm.activeTheme.border, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = (index + 1).toString(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = vm.activeTheme.primary
                                    )
                                }
                                Text(
                                    text = "${if (vm.lang == "en") "Repeat" else "التكرار"}: $count / ${adhkar.target}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDone) vm.activeTheme.success else vm.activeTheme.primary
                                )
                            }

                            Text(
                                text = adhkar.text,
                                fontFamily = FontFamily.Serif,
                                fontSize = 16.sp,
                                color = if (isDone) vm.activeTheme.text.copy(alpha = 0.72f) else vm.activeTheme.text,
                                lineHeight = 24.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (adhkar.blessing.isNotEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            vm.activeTheme.primary.copy(alpha = 0.03f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("🌟", fontSize = 11.sp)
                                    Text(
                                        text = adhkar.blessing,
                                        fontSize = 11.sp,
                                        color = vm.activeTheme.textSecondary,
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedIconButton(
                                        onClick = { vm.decrementAdhkarCount(azType, index) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.size(32.dp),
                                        border = BorderStroke(1.dp, vm.activeTheme.border)
                                    ) {
                                        Text("−", color = vm.activeTheme.text, fontWeight = FontWeight.Bold)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .width(28.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = count.toString(),
                                            fontWeight = FontWeight.Bold,
                                            color = vm.activeTheme.primary
                                        )
                                    }

                                    Button(
                                        onClick = { vm.incrementAdhkarCount(azType, index, adhkar.target) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isDone) vm.activeTheme.success else vm.activeTheme.primary
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(34.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp),
                                        enabled = !isDone
                                    ) {
                                        Text(
                                            text = if (isDone) "✓" else "TAP",
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDone) Color.White else Color.Black,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. TASBIH SECTION
// ==========================================
@Composable
fun TasbihSection(vm: MainViewModel) {
    val activeDhikr = TASBIH_LIST[vm.selectedTasbihIndex]
    val r = 70f
    val circumference = 2 * Math.PI * r
    val isCompleted = vm.tasbihCount >= activeDhikr.target
    val sweepPercent = (vm.tasbihCount.toFloat() / activeDhikr.target.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "📿 " + (if (vm.lang == "en") "Tasbih Counter" else "مسبحة الذكر والاستغفار"),
            style = MaterialTheme.typography.titleMedium,
            color = vm.activeTheme.primary,
            fontWeight = FontWeight.Bold
        )

        // Selected Dhikr Selector Dropdown
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = vm.activeTheme.cardBg),
            border = BorderStroke(1.dp, vm.activeTheme.border)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (vm.lang == "en") "Choose Dhikr" else "اختر صيغة الذكر:",
                    fontSize = 11.sp,
                    color = vm.activeTheme.textSecondary
                )

                var showDhikrDrop by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showDhikrDrop = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, vm.activeTheme.border)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(activeDhikr.text, color = vm.activeTheme.text, maxLines = 1)
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = vm.activeTheme.primary)
                        }
                    }

                    DropdownMenu(
                        expanded = showDhikrDrop,
                        onDismissRequest = { showDhikrDrop = false },
                        modifier = Modifier.background(vm.activeTheme.surface)
                    ) {
                        TASBIH_LIST.forEachIndexed { idx, item ->
                            DropdownMenuItem(
                                text = { Text("${item.text} (${item.target}×)", color = vm.activeTheme.text) },
                                onClick = {
                                    vm.selectedTasbihIndex = idx
                                    vm.resetTasbihProgress()
                                    showDhikrDrop = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Circular dynamic gauge
        Box(
            modifier = Modifier
                .size(180.dp)
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val circleCenter = center
                // Draw background circle
                drawCircle(
                    color = vm.activeTheme.border,
                    radius = r.dp.toPx(),
                    style = Stroke(width = 8.dp.toPx())
                )
                // Draw progress sweep
                drawArc(
                    color = if (isCompleted) vm.activeTheme.success else vm.activeTheme.primary,
                    startAngle = -90f,
                    sweepAngle = sweepPercent * 360f,
                    useCenter = false,
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = vm.tasbihCount.toString(),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isCompleted) vm.activeTheme.success else vm.activeTheme.primaryVariant
                )
                Text(
                    text = "من أصل ${activeDhikr.target}",
                    fontSize = 11.sp,
                    color = vm.activeTheme.textSecondary
                )
            }
        }

        // Large tap button driving counts with haptics
        Button(
            onClick = { vm.incrementTasbih() },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isCompleted) vm.activeTheme.success else vm.activeTheme.primary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(32.dp)
        ) {
            Text(
                text = if (isCompleted) (if (vm.lang == "en") "Completed!" else "مكتمل وبوركت!") else "TAP",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black
            )
        }

        // Action controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { vm.decrementTasbih() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = vm.activeTheme.primary),
                border = BorderStroke(1.dp, vm.activeTheme.border)
            ) {
                Text(if (vm.lang == "en") "Subtract" else "إنقاص واحد")
            }
            OutlinedButton(
                onClick = { vm.resetTasbihProgress() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = vm.activeTheme.primary),
                border = BorderStroke(1.dp, vm.activeTheme.border)
            ) {
                Text(if (vm.lang == "en") "Reset" else "تصفير")
            }
        }

        // Totals card
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            colors = CardDefaults.cardColors(containerColor = vm.activeTheme.primary.copy(alpha = 0.05f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (vm.lang == "en") "Today's Total Count" else "إجمالي التسبيح اليوم:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = vm.activeTheme.textSecondary
                )
                Text(
                    text = vm.tasbihTotalToday.toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = vm.activeTheme.primary
                )
            }
        }
    }
}

// ==========================================
// 5. ASMA UL HUSNA SECTION
// ==========================================
@Composable
fun AsmaSection(vm: MainViewModel, onNameClick: (AsmaName) -> Unit) {
    var query by remember { mutableStateOf("") }

    val filteredAsma = remember(query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) {
            ASMA_LIST
        } else {
            ASMA_LIST.filter { a ->
                a.arabic.contains(q) ||
                a.english.lowercase().contains(q) ||
                a.number.toString() == q
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("☪️", fontSize = 24.sp)
            Column {
                Text(
                    text = when (vm.lang) {
                        "en" -> "Asma-ul-Husna"
                        "ur" -> "اسماء الحسنیٰ"
                        "bn" -> "আসমাউল হুসনা"
                        else -> "أسماء الله الحسنى"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = vm.activeTheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (vm.lang == "en") "99 Beautiful Names of Allah" else "٩٩ اسماً شريفاً من صفات الجلال والكمال",
                    fontSize = 11.sp,
                    color = vm.activeTheme.textSecondary
                )
            }
        }

        // Search names
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text(if (vm.lang == "en") "Search beautiful name..." else "ابحث عن اسم شريف...") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = vm.activeTheme.textMuted) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = vm.activeTheme.primary,
                unfocusedBorderColor = vm.activeTheme.border,
                focusedContainerColor = vm.activeTheme.cardBg,
                unfocusedContainerColor = vm.activeTheme.cardBg
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredAsma) { item ->
                Card(
                    onClick = {
                        onNameClick(item)
                        vm.performHapticFeedback(12)
                    },
                    colors = CardDefaults.cardColors(containerColor = vm.activeTheme.cardBg),
                    border = BorderStroke(1.dp, vm.activeTheme.border)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = NumbersArabicConverter.convert(item.number),
                            fontSize = 8.sp,
                            color = vm.activeTheme.textMuted,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.arabic,
                            fontFamily = FontFamily.Serif,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = vm.activeTheme.primaryVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.localizedName(vm.lang),
                            fontSize = 10.sp,
                            color = vm.activeTheme.textSecondary,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. SUPPORT / CHARITY SECTION
// ==========================================
@Composable
fun SupportSection(vm: MainViewModel) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Centered Mushaf Logo Icon
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(vm.activeTheme.cardBg)
                .border(2.dp, vm.activeTheme.primary.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = com.example.R.drawable.mushaf_logo),
                contentDescription = "Mushaf logo",
                modifier = Modifier.fillMaxSize()
            )
        }
        Text(
            text = if (vm.lang == "en") "Support Our Islamic Work" else "ادعم تطبيق المصحف الشريف",
            style = MaterialTheme.typography.titleLarge,
            color = vm.activeTheme.primaryVariant,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = if (vm.lang == "en") "Help us improve server latency and maintain reciters Timing database globally." else "نعمل جاهدين ليبقى التطبيق خالياً من الإعلانات وخادماً للمسلمين في أرجاء الأرض.",
            fontSize = 12.sp,
            color = vm.activeTheme.textSecondary,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center
        )

        Box(
            modifier = Modifier
                .background(
                    vm.activeTheme.primary.copy(alpha = 0.05f),
                    RoundedCornerShape(12.dp)
                )
                .border(1.dp, vm.activeTheme.primary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (vm.lang == "en") "«Whoever guides someone to goodness will have circles of rewards like the doer»" else "«مَن دَلَّ على خيرٍ فله مِثلُ أجرِ فاعله»",
                    fontFamily = FontFamily.Serif,
                    fontSize = 13.sp,
                    color = vm.activeTheme.primary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "رواه مسلم • Prophet Muhammad ﷺ",
                    fontSize = 10.sp,
                    color = vm.activeTheme.textMuted
                )
            }
        }

        // Donation Goal bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = vm.activeTheme.cardBg),
            border = BorderStroke(1.dp, vm.activeTheme.border)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (vm.lang == "en") "Monthly Server Goal" else "هدف تكاليف السيرفر هذا الشهر",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = vm.activeTheme.text
                    )
                    Box(
                        modifier = Modifier
                            .background(vm.activeTheme.primary.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("47%", color = vm.activeTheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Progress Bar
                LinearProgressIndicator(
                    progress = { 0.47f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = vm.activeTheme.primary,
                    trackColor = vm.activeTheme.border
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("$47 raised", fontSize = 11.sp, color = vm.activeTheme.primaryVariant, fontWeight = FontWeight.Bold)
                    Text("Goal: $100", fontSize = 11.sp, color = vm.activeTheme.textSecondary)
                }
            }
        }

        // Ko-fi Card link
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = vm.activeTheme.surface),
            border = BorderStroke(1.dp, vm.activeTheme.primary.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFFFA000).copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("☕", fontSize = 24.sp)
                    }
                    Column {
                        Text(
                            text = if (vm.lang == "en") "Support via Ko-fi" else "تبرع عن طريق Ko-fi",
                            fontWeight = FontWeight.Bold,
                            color = vm.activeTheme.text,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (vm.lang == "en") "Direct, secure and instant contributions" else "دعم آمن وسريع بكبسة زر واحدة",
                            fontSize = 11.sp,
                            color = vm.activeTheme.textSecondary
                        )
                    }
                }

                Button(
                    onClick = {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ko-fi.com/mushaf_islamic"))
                        context.startActivity(browserIntent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5E5B)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (vm.lang == "en") "Support Us on Ko-fi ☕" else "ادعمنا الآن عبر Ko-fi ☕",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Prophetic Duo card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    vm.activeTheme.primary.copy(alpha = 0.03f),
                    RoundedCornerShape(16.dp)
                )
                .border(1.dp, vm.activeTheme.border, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "اللَّهُمَّ بَارِكْ فِي مَن أَعَانَنَا وَاجْعَلْهُ فِي مِيزَانِ حَسَنَاتِهِ",
                    fontFamily = FontFamily.Serif,
                    fontSize = 15.sp,
                    color = vm.activeTheme.primaryVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
                Text(
                    text = if (vm.lang == "en") "O Allah, bless those who support us and write it in their scale of good deeds." else "جزاك الله خيراً وشكر الله سعيك في ميزان الحسنات.",
                    fontSize = 11.sp,
                    color = vm.activeTheme.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ==========================================
// 7. GLOBAL FLOATING AUDIO PLAYER BAR Composable
// ==========================================
@Composable
fun AudioPlayerBar(vm: MainViewModel) {
    val durationText = fmtTime((vm.audioDurationSec).toInt())
    val elapsedText = fmtTime((vm.audioCurrentTimeSec).toInt())
    val progressPr = if (vm.audioDurationSec > 0) (vm.audioCurrentTimeSec / vm.audioDurationSec * 100f) else 0f

    Surface(
        color = vm.activeTheme.surface,
        tonalElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, vm.activeTheme.primary.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
        contentColor = vm.activeTheme.text
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Info Section
                Column(modifier = Modifier.weight(1f)) {
                    val sName = SURAH_LIST.find { it.number == (vm.currentSurahNum ?: 1) }?.localizedName(vm.lang) ?: "الفاتحة"
                    Text(
                        text = "سورة $sName • آية ${vm.currentlyPlayingAyahNum}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = vm.activeTheme.primaryVariant,
                        maxLines = 1
                    )
                    Text(
                        text = vm.selectedReciter?.name ?: "الشيخ العفاسي",
                        fontSize = 10.sp,
                        color = vm.activeTheme.textSecondary,
                        maxLines = 1
                    )
                }

                // Controls Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = { vm.skipCurrentAyah(-1) }) {
                        Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = "Prev", tint = vm.activeTheme.primary, modifier = Modifier.size(20.dp))
                    }
                    IconButton(
                        onClick = { vm.togglePlayPauseAudio() },
                        modifier = Modifier
                            .size(36.dp)
                            .background(vm.activeTheme.primary, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (vm.isPlayingAudio) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "PlayPause",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = { vm.skipCurrentAyah(1) }) {
                        Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Next", tint = vm.activeTheme.primary, modifier = Modifier.size(20.dp))
                    }
                }

                // Close Player button
                IconButton(onClick = { vm.stopAudio() }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = vm.activeTheme.textMuted, modifier = Modifier.size(18.dp))
                }
            }

            // Progress seek bar row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(elapsedText, fontSize = 9.sp, color = vm.activeTheme.textSecondary)
                Slider(
                    value = progressPr,
                    onValueChange = { vm.seekToPosition(it) },
                    valueRange = 0f..100f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = vm.activeTheme.primary,
                        activeTrackColor = vm.activeTheme.primary
                    )
                )
                Text(durationText, fontSize = 9.sp, color = vm.activeTheme.textSecondary)
            }
        }
    }
}

// ==========================================
// 8. FULLSCREEN MEMORIZE REPETITION SCREEN
// ==========================================
@Composable
fun RepeatProgressScreen(vm: MainViewModel) {
    val progressSweepPercent = if (vm.loopTimes > 0) (vm.activeRepeatNum.toFloat() / vm.loopTimes.toFloat()).coerceIn(0f, 1f) else 0f
    val r = 62f
    val circ = 2 * Math.PI * r

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.98f))
            .clickable(enabled = false) {} // block clickthroughs
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = if (vm.lang == "en") "Recitation loop engine active" else "جاري تكرار التلاوة للتلقين والحفظ",
                color = vm.activeTheme.textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = vm.activeRepeatTitle,
                fontFamily = FontFamily.Serif,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = vm.activeTheme.primary
            )

            // Large circular repeating indicator metric
            Box(
                modifier = Modifier.size(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color(0xFF1E1A10),
                        radius = r.dp.toPx(),
                        style = Stroke(width = 8.dp.toPx())
                    )
                    drawArc(
                        color = vm.activeTheme.primary,
                        startAngle = -90f,
                        sweepAngle = progressSweepPercent * 360f,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = vm.activeRepeatNum.toString(),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = vm.activeTheme.primaryVariant
                    )
                    Text(
                        text = "/ ${vm.loopTimes}",
                        fontSize = 13.sp,
                        color = vm.activeTheme.textMuted
                    )
                }
            }

            Text(
                text = vm.activeRepeatStatus,
                fontSize = 14.sp,
                color = vm.activeTheme.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            // Displays the repeated Quran Ayah/surah text beautifully
            if (vm.activeRepeatAyahText.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            vm.activeTheme.primary.copy(alpha = 0.05f),
                            RoundedCornerShape(16.dp)
                        )
                        .border(1.dp, vm.activeTheme.primary.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = vm.activeRepeatAyahText,
                        fontFamily = FontFamily.Serif,
                        fontSize = 20.sp,
                        color = vm.activeTheme.text,
                        lineHeight = 32.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Loop Controls: Pause/Stop
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { vm.toggleRepeatPause() },
                    colors = ButtonDefaults.buttonColors(containerColor = vm.activeTheme.cardBg),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = if (vm.isRepeatPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = vm.activeTheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (vm.isRepeatPaused) (if (vm.lang == "en") "Resume" else "استئناف") else (if (vm.lang == "en") "Pause" else "إيقاف مؤقت"),
                        color = vm.activeTheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { vm.stopLoopRecitation() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB43232).copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFB43232))
                ) {
                    Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop", tint = Color(0xFFEE7777))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (vm.lang == "en") "Stop" else "إنهاء التلقين",
                        color = Color(0xFFEE7777),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Helpers
private fun fmtTime(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", m, s)
}

@Composable
fun MushafPageFrame(
    themeColors: AppThemeColors,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(themeColors.background)
            .padding(4.dp) // Outer page margin
            .drawBehind {
                val primaryColor = themeColors.primary
                val width = size.width
                val height = size.height
                
                // Draw elegant outer border
                drawRect(
                    color = primaryColor.copy(alpha = 0.35f),
                    topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(width - 8.dp.toPx(), height - 8.dp.toPx()),
                    style = Stroke(width = 1.5.dp.toPx())
                )
                
                // Draw elegant inner double border
                drawRect(
                    color = primaryColor.copy(alpha = 0.2f),
                    topLeft = Offset(8.dp.toPx(), 8.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(width - 16.dp.toPx(), height - 16.dp.toPx()),
                    style = Stroke(width = 0.75.dp.toPx())
                )
                
                // Draw decorative corners (the classical Islamic star/arcs)
                val cornerSize = 12.dp.toPx()
                val offset8 = 8.dp.toPx()
                
                // Top-Left corner
                drawLine(
                    color = primaryColor,
                    start = Offset(offset8, offset8 + cornerSize),
                    end = Offset(offset8, offset8),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = primaryColor,
                    start = Offset(offset8, offset8),
                    end = Offset(offset8 + cornerSize, offset8),
                    strokeWidth = 2.dp.toPx()
                )
                
                // Top-Right corner
                drawLine(
                    color = primaryColor,
                    start = Offset(width - offset8, offset8 + cornerSize),
                    end = Offset(width - offset8, offset8),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = primaryColor,
                    start = Offset(width - offset8, offset8),
                    end = Offset(width - offset8 - cornerSize, offset8),
                    strokeWidth = 2.dp.toPx()
                )
                
                // Bottom-Left corner
                drawLine(
                    color = primaryColor,
                    start = Offset(offset8, height - offset8 - cornerSize),
                    end = Offset(offset8, height - offset8),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = primaryColor,
                    start = Offset(offset8, height - offset8),
                    end = Offset(offset8 + cornerSize, height - offset8),
                    strokeWidth = 2.dp.toPx()
                )
                
                // Bottom-Right corner
                drawLine(
                    color = primaryColor,
                    start = Offset(width - offset8, height - offset8 - cornerSize),
                    end = Offset(width - offset8, height - offset8),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = primaryColor,
                    start = Offset(width - offset8, height - offset8),
                    end = Offset(width - offset8 - cornerSize, height - offset8),
                    strokeWidth = 2.dp.toPx()
                )
            }
            .padding(10.dp) // Content padding inner frame
    ) {
        content()
    }
}

@Composable
fun SurahIntroCard(
    themeColors: AppThemeColors,
    name: String,
    verses: Int,
    type: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        themeColors.primary.copy(alpha = 0.12f),
                        themeColors.primary.copy(alpha = 0.02f)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                border = BorderStroke(1.dp, themeColors.primary.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Double ornamental border inside card
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(3.dp)
                .border(
                    border = BorderStroke(0.5.dp, themeColors.primary.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(9.dp)
                )
        )
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "سُورَةُ",
                fontFamily = FontFamily.Serif,
                fontSize = 13.sp,
                color = themeColors.primaryVariant,
                fontWeight = FontWeight.Light,
                letterSpacing = 1.sp
            )
            Text(
                text = name,
                fontFamily = FontFamily.Serif,
                fontSize = 24.sp,
                color = themeColors.primary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .background(themeColors.primary.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = type,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.primary
                )
                Text(
                    text = "•",
                    fontSize = 10.sp,
                    color = themeColors.primary.copy(alpha = 0.5f)
                )
                Text(
                    text = "$verses آية",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.primary
                )
            }
        }
    }
}

@Composable
fun BismillahHeader(themeColors: AppThemeColors, fontSize: androidx.compose.ui.unit.TextUnit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, themeColors.primary.copy(alpha = 0.3f))
                        )
                    )
            )
            Text(
                text = "بِسۡمِ ٱللَّهِ ٱلرَّحۡمَٰنِ ٱلرَّحِيمِ",
                fontFamily = FontFamily.Serif,
                fontSize = fontSize,
                color = themeColors.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(themeColors.primary.copy(alpha = 0.3f), Color.Transparent)
                        )
                    )
            )
        }
    }
}
