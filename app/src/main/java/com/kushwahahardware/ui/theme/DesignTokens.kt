package com.kushwahahardware.ui.theme

import androidx.compose.ui.graphics.Color

object DesignTokens {
    // Premium Teal Palette (HSL inspired)
    val DeepTeal = Color(0xFF00201D)
    val PremiumTeal = Color(0xFF004D40)
    val MidTeal = Color(0xFF00695C)
    val LightTeal = Color(0xFFE0F2F1)
    val TealAccent = Color(0xFF80CBC4)

    // Burnished Gold / Accent Palette
    val GoldAccent = Color(0xFFD4AF37)
    val SoftGold = Color(0xFFF9F1D0)
    val WarningOrange = Color(0xFFE65100)
    val ErrorRed = Color(0xFFC62828)
    val SuccessGreen = Color(0xFF2E7D32)

    // Neutral / Glass Palette
    val GlassWhite = Color.White.copy(alpha = 0.7f)
    val GlassStroke = Color.White.copy(alpha = 0.3f)
    val SubtitleGray = Color(0xFF757575)
    
    // Input Fields
    val InputBackground = Color(0xFFF5F7F8)
    val InputHint = Color(0xFFA0AAB2)
    
    // Gradients
    val PremiumGradient = listOf(PremiumTeal, MidTeal)
    val GlassGradient = listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.05f))
}
