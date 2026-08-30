package com.memorymoments.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.memorymoments.app.R

val PressStart2P = FontFamily(
    Font(R.font.press_start_2p, FontWeight.Normal)
)

val ReadableFont = FontFamily.SansSerif

/** Pixel/arcade labels only — never long copy. */
@Immutable
data class ArcadeTypography(
    val title: TextStyle = TextStyle(
        fontFamily = PressStart2P,
        fontWeight = FontWeight.Normal,
        fontSize = 26.sp,
        lineHeight = 36.sp
    ),
    val titleSmall: TextStyle = TextStyle(
        fontFamily = PressStart2P,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 28.sp
    ),
    val hud: TextStyle = TextStyle(
        fontFamily = PressStart2P,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 18.sp
    ),
    val label: TextStyle = TextStyle(
        fontFamily = PressStart2P,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 16.sp
    )
)

val Typography = Typography(
    displayLarge = ArcadeTypography().title,
    headlineLarge = TextStyle(
        fontFamily = ReadableFont,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        color = NeTextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = ReadableFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        color = NeTextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = ReadableFont,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        color = NeTextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = ReadableFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        color = NeTextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = ReadableFont,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        color = NeTextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = ReadableFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = NeTextSecondary
    ),
    labelLarge = TextStyle(
        fontFamily = ReadableFont,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = NeTextPrimary
    ),
    labelMedium = TextStyle(
        fontFamily = ReadableFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = NeTextSecondary
    )
)

