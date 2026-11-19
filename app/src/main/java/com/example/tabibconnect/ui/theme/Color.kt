package com.example.tabibconnect.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// TabibConnect Primary Palette (exact match from mockups)
val TealPrimary = Color(0xFF007E8A)
val TealDark = Color(0xFF006872)
val TealLightContainer = Color(0xFFE2F4F3)
val TealLightIconBg = Color(0xFFD8F2F0)

val NavyDark = Color(0xFF0B1E2D)
val NavyAvatar = Color(0xFF0F2537)
val TextDarkNavy = Color(0xFF0F2537)

val SoftPeach = Color(0xFFFDE8E4)
val SoftPeachText = Color(0xFFC85A48)
val SoftPeachIconBg = Color(0xFFFBECE9)

val SoftPurpleIconBg = Color(0xFFEAEBFF)
val SoftGreenIconBg = Color(0xFFE1F5FE)
val SoftGreenIcon = Color(0xFF4CAF50)

val BackgroundScreen = Color(0xFFF5F8FA)
val SurfaceWhite = Color(0xFFFFFFFF)
val TextGray = Color(0xFF6B7C85)
val TextLightGray = Color(0xFF94A3B8)

val BorderLight = Color(0xFFE2E8F0)
val SelectedBorderTeal = Color(0xFF007E8A)

// Compatibility aliases
val PrimaryTeal = TealPrimary
val PrimaryDarkTeal = TextDarkNavy
val SecondaryTeal = TealDark
val LightTealContainer = TealLightContainer
val EmergencyRed = Color(0xFFE63946)
val AccentOrange = Color(0xFFF4A261)
val BackgroundLight = BackgroundScreen
val SurfaceCard = SurfaceWhite
val TextPrimary = TextDarkNavy
val TextSecondary = TextGray

val HeaderGradient = Brush.horizontalGradient(
    colors = listOf(TealPrimary, TealDark)
)
val CardGradient = Brush.horizontalGradient(
    colors = listOf(TealPrimary, TealDark)
)
val AccentGradient = Brush.horizontalGradient(
    colors = listOf(TealPrimary, TealLightContainer)
)
val SoftPurple = Color(0xFF6366F1)
val SoftGreen = Color(0xFF10B981)
