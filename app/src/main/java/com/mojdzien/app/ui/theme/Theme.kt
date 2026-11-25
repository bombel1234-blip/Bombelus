package com.mojdzien.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Color(0xFF386A20),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB7F397),
    onPrimaryContainer = Color(0xFF072100),
    secondary = Color(0xFF55624C),
    secondaryContainer = Color(0xFFD8E7CB),
    onSecondaryContainer = Color(0xFF131F0D),
    background = Color(0xFFF8FAF1),
    onBackground = Color(0xFF191D17),
    surface = Color(0xFFF8FAF1),
    surfaceVariant = Color(0xFFDFE4D7),
    onSurfaceVariant = Color(0xFF43483F)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9CD674),
    onPrimary = Color(0xFF133800),
    primaryContainer = Color(0xFF225100),
    onPrimaryContainer = Color(0xFFB7F397),
    secondary = Color(0xFFBCCBB0),
    secondaryContainer = Color(0xFF3E4A36),
    onSecondaryContainer = Color(0xFFD8E7CB),
    background = Color(0xFF11140F),
    onBackground = Color(0xFFE0E4DB),
    surface = Color(0xFF11140F),
    surfaceVariant = Color(0xFF43483F),
    onSurfaceVariant = Color(0xFFC3C8BB)
)

@Composable
fun MyDayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
