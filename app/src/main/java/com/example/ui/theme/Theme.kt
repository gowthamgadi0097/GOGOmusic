package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val VaultDarkColorScheme = darkColorScheme(
    primary = NeonViolet,
    onPrimary = Color.White,
    primaryContainer = NeonVioletDark,
    onPrimaryContainer = NeonVioletLight,
    secondary = NeonCyan,
    onSecondary = Color(0xFF032B30),
    secondaryContainer = NeonCyanDark,
    onSecondaryContainer = NeonCyanLight,
    tertiary = NeonPink,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder,
    error = DangerRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Music player defaults to immersive dark vault
    dynamicColor: Boolean = false, // Keep sleek custom branding colors
    content: @Composable () -> Unit
) {
    val colorScheme = VaultDarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
