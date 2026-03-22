package perozzi.gib.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import perozzi.gib.R

private val ZtNatureFontFamily = FontFamily(
    Font(R.font.zt_nature_regular, weight = FontWeight.Normal),
    Font(R.font.zt_nature_italic, weight = FontWeight.Normal, style = FontStyle.Italic),
    Font(R.font.zt_nature_bold, weight = FontWeight.Bold),
    Font(R.font.zt_nature_bold_italic, weight = FontWeight.Bold, style = FontStyle.Italic),
)

val GibTypography = Typography(
    displayMedium = TextStyle(
        fontFamily = ZtNatureFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 44.sp,
        lineHeight = 48.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = ZtNatureFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = ZtNatureFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = ZtNatureFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = ZtNatureFontFamily,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = ZtNatureFontFamily,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = ZtNatureFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = ZtNatureFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
    ),
)
