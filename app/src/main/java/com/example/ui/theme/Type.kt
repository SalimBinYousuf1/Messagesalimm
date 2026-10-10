package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Apple Liquid Glass Frost Six Fixed Roles
val LargeTitle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Bold,
    fontSize = 34.sp,
    lineHeight = 41.sp,
    letterSpacing = 0.37.sp
)

val Title = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.SemiBold,
    fontSize = 28.sp,
    lineHeight = 34.sp,
    letterSpacing = 0.36.sp
)

val Headline = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.SemiBold,
    fontSize = 20.sp,
    lineHeight = 25.sp,
    letterSpacing = 0.38.sp
)

val Body = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 17.sp,
    lineHeight = 22.sp,
    letterSpacing = (-0.41).sp
)

val Callout = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    letterSpacing = (-0.24).sp
)

val Caption = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = (-0.08).sp
)

val Typography = Typography(
    headlineLarge = LargeTitle,
    headlineMedium = Title,
    headlineSmall = Headline,
    bodyLarge = Body,
    bodyMedium = Callout,
    bodySmall = Caption,
    titleLarge = Title,
    titleMedium = Headline,
    titleSmall = Callout,
    labelLarge = Callout,
    labelMedium = Caption,
    labelSmall = Caption.copy(fontSize = 11.sp, lineHeight = 14.sp)
)
