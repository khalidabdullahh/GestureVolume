package com.gesturevolume.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.gesturevolume.app.R

// Chelsea Market (body / UI) and Cabin Sketch (titles / display)
val ChelseaMarket = FontFamily(
    Font(R.font.chelsea_market_regular, FontWeight.Normal)
)

val CabinSketch = FontFamily(
    Font(R.font.cabin_sketch_regular, FontWeight.Normal),
    Font(R.font.cabin_sketch_bold, FontWeight.Bold)
)

private val base = Typography()

private fun TextStyle.chelsea() = copy(fontFamily = ChelseaMarket, fontSynthesis = FontSynthesis.None)
private fun TextStyle.sketch() = copy(fontFamily = CabinSketch, fontWeight = FontWeight.Bold, fontSynthesis = FontSynthesis.None)

val Typography = Typography(
    displayLarge = base.displayLarge.sketch(),
    displayMedium = base.displayMedium.sketch(),
    displaySmall = base.displaySmall.sketch(),
    headlineLarge = TextStyle(
        fontFamily = CabinSketch,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        color = TextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = CabinSketch,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        color = TextPrimary
    ),
    headlineSmall = base.headlineSmall.sketch(),
    titleLarge = TextStyle(
        fontFamily = CabinSketch,
        fontWeight = FontWeight.Bold,
        fontSize = 21.sp,
        lineHeight = 26.sp,
        color = TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = ChelseaMarket,
        fontWeight = FontWeight.Normal,
        fontSynthesis = FontSynthesis.None,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = TextPrimary
    ),
    titleSmall = base.titleSmall.chelsea(),
    bodyLarge = TextStyle(
        fontFamily = ChelseaMarket,
        fontWeight = FontWeight.Normal,
        fontSynthesis = FontSynthesis.None,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        color = TextSecondary
    ),
    bodyMedium = TextStyle(
        fontFamily = ChelseaMarket,
        fontWeight = FontWeight.Normal,
        fontSynthesis = FontSynthesis.None,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        color = TextSecondary
    ),
    bodySmall = base.bodySmall.chelsea(),
    labelLarge = TextStyle(
        fontFamily = ChelseaMarket,
        fontWeight = FontWeight.Normal,
        fontSynthesis = FontSynthesis.None,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = TextPrimary
    ),
    labelMedium = base.labelMedium.chelsea(),
    labelSmall = base.labelSmall.chelsea()
)
