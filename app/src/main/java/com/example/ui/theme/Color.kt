package com.example.ui.theme

import androidx.compose.ui.graphics.Color

data class AppThemeColors(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val isDark: Boolean,
    val primary: Color,
    val primaryVariant: Color,
    val background: Color,
    val surface: Color,
    val cardBg: Color,
    val border: Color,
    val text: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val success: Color
)

val DarkGoldTheme = AppThemeColors(
    id = "dark_gold",
    nameAr = "الليل الذهبي",
    nameEn = "Dark Gold",
    isDark = true,
    primary = Color(0xFFC9A84C),
    primaryVariant = Color(0xFFE8CC7A),
    background = Color(0xFF080808),
    surface = Color(0xFF111111),
    cardBg = Color(0xFF181818),
    border = Color(0xFF2A2416),
    text = Color(0xFFF0E8D8),
    textSecondary = Color(0xFF9A8868),
    textMuted = Color(0xFF4A4030),
    success = Color(0xFF3DA366)
)

val DayLightTheme = AppThemeColors(
    id = "light",
    nameAr = "النهار الناصع",
    nameEn = "Day Light",
    isDark = false,
    primary = Color(0xFFB8903C),
    primaryVariant = Color(0xFF9A7428),
    background = Color(0xFFF8F5EE),
    surface = Color(0xFFFFFFFF),
    cardBg = Color(0xFFF0ECE0),
    border = Color(0xFFD0C8A8),
    text = Color(0xFF1A1208),
    textSecondary = Color(0xFF6B5230),
    textMuted = Color(0xFFB0976A),
    success = Color(0xFF2E7D4F)
)

val DeepOceanTheme = AppThemeColors(
    id = "ocean",
    nameAr = "عمق البحر",
    nameEn = "Deep Ocean",
    isDark = true,
    primary = Color(0xFF4DB8F0),
    primaryVariant = Color(0xFF7DCEFF),
    background = Color(0xFF040D1A),
    surface = Color(0xFF071428),
    cardBg = Color(0xFF0A1F38),
    border = Color(0xFF123460),
    text = Color(0xFFC8E8FF),
    textSecondary = Color(0xFF6A9EC0),
    textMuted = Color(0xFF2A5070),
    success = Color(0xFF28B890)
)

val EmeraldTheme = AppThemeColors(
    id = "emerald",
    nameAr = "الزمرد",
    nameEn = "Emerald",
    isDark = true,
    primary = Color(0xFF3DC878),
    primaryVariant = Color(0xFF60E898),
    background = Color(0xFF030F08),
    surface = Color(0xFF071A10),
    cardBg = Color(0xFF0A2418),
    border = Color(0xFF103020),
    text = Color(0xFFD0F0E0),
    textSecondary = Color(0xFF5A9870),
    textMuted = Color(0xFF204838),
    success = Color(0xFF3DA366)
)

val AmethystTheme = AppThemeColors(
    id = "amethyst",
    nameAr = "الياقوت البنفسجي",
    nameEn = "Amethyst",
    isDark = true,
    primary = Color(0xFFB87AE8),
    primaryVariant = Color(0xFFD0A0FF),
    background = Color(0xFF0A060F),
    surface = Color(0xFF120818),
    cardBg = Color(0xFF1A0C24),
    border = Color(0xFF280E38),
    text = Color(0xFFF0D8FF),
    textSecondary = Color(0xFF8A68A0),
    textMuted = Color(0xFF40285a),
    success = Color(0xFF7A54C0)
)

val DesertRoseTheme = AppThemeColors(
    id = "rose",
    nameAr = "وردة الصحراء",
    nameEn = "Desert Rose",
    isDark = true,
    primary = Color(0xFFE8607A),
    primaryVariant = Color(0xFFFF8FA0),
    background = Color(0xFF100608),
    surface = Color(0xFF1A0A0E),
    cardBg = Color(0xFF240E14),
    border = Color(0xFF34121C),
    text = Color(0xFFFFE8EC),
    textSecondary = Color(0xFFA06070),
    textMuted = Color(0xFF502030),
    success = Color(0xFFB85480)
)

val MedinaSandTheme = AppThemeColors(
    id = "sand",
    nameAr = "رمال المدينة",
    nameEn = "Medina Sand",
    isDark = true,
    primary = Color(0xFFD4A84C),
    primaryVariant = Color(0xFFF0C870),
    background = Color(0xFF110E07),
    surface = Color(0xFF1A160A),
    cardBg = Color(0xFF241E0E),
    border = Color(0xFF342C14),
    text = Color(0xFFF8E8C0),
    textSecondary = Color(0xFFA08850),
    textMuted = Color(0xFF504020),
    success = Color(0xFF70B048)
)

val MidnightBlueTheme = AppThemeColors(
    id = "midnight",
    nameAr = "منتصف الليل",
    nameEn = "Midnight",
    isDark = true,
    primary = Color(0xFF5890E8),
    primaryVariant = Color(0xFF80B0FF),
    background = Color(0xFF05080F),
    surface = Color(0xFF08101E),
    cardBg = Color(0xFF0C182E),
    border = Color(0xFF14243E),
    text = Color(0xFFD8E8FF),
    textSecondary = Color(0xFF6080A8),
    textMuted = Color(0xFF283858),
    success = Color(0xFF409898)
)

val SoftIvoryTheme = AppThemeColors(
    id = "soft_ivory",
    nameAr = "الورق العتيق",
    nameEn = "Vintage Paper",
    isDark = false,
    primary = Color(0xFF8B6B38),
    primaryVariant = Color(0xFF5D4037),
    background = Color(0xFFFAF2E1),
    surface = Color(0xFFFFFDF9),
    cardBg = Color(0xFFF4EAD4),
    border = Color(0xFFE8D7B8),
    text = Color(0xFF2B1D0E),
    textSecondary = Color(0xFF755D3D),
    textMuted = Color(0xFFAFA081),
    success = Color(0xFF436B3D)
)

val SufiGreenTheme = AppThemeColors(
    id = "sufi_green",
    nameAr = "الأخضر الصوفي",
    nameEn = "Sufi Green",
    isDark = true,
    primary = Color(0xFF4ED184),
    primaryVariant = Color(0xFF86EAA8),
    background = Color(0xFF040F0A),
    surface = Color(0xFF071B11),
    cardBg = Color(0xFF0C2418),
    border = Color(0xFF163E28),
    text = Color(0xFFE2F9EE),
    textSecondary = Color(0xFF7CB895),
    textMuted = Color(0xFF335C44),
    success = Color(0xFF43A047)
)

val KabahBlackTheme = AppThemeColors(
    id = "kabah_black",
    nameAr = "الكعبة المشرفة",
    nameEn = "Kabah Gold",
    isDark = true,
    primary = Color(0xFFE5C158),
    primaryVariant = Color(0xFFF2D785),
    background = Color(0xFF090909),
    surface = Color(0xFF121212),
    cardBg = Color(0xFF1A1A1A),
    border = Color(0xFF352D16),
    text = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFC0BCB4),
    textMuted = Color(0xFF6D6348),
    success = Color(0xFF4CAF50)
)

val KiswaTealTheme = AppThemeColors(
    id = "kiswa_teal",
    nameAr = "الكسوة الفيروزية",
    nameEn = "Kiswa Teal",
    isDark = true,
    primary = Color(0xFF4CB8C4),
    primaryVariant = Color(0xFF82DCE6),
    background = Color(0xFF081418),
    surface = Color(0xFF0D2126),
    cardBg = Color(0xFF132D34),
    border = Color(0xFF1F4852),
    text = Color(0xFFE6F7F9),
    textSecondary = Color(0xFF86BCC4),
    textMuted = Color(0xFF366068),
    success = Color(0xFF00BFA5)
)

val ALL_THEMES = listOf(
    DarkGoldTheme,
    DayLightTheme,
    DeepOceanTheme,
    EmeraldTheme,
    AmethystTheme,
    DesertRoseTheme,
    MedinaSandTheme,
    MidnightBlueTheme,
    SoftIvoryTheme,
    SufiGreenTheme,
    KabahBlackTheme,
    KiswaTealTheme
)
