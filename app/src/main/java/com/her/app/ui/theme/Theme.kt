package com.her.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HERColorScheme = lightColorScheme(
    primary = Saffron,
    onPrimary = Color.White,
    primaryContainer = WarmSurface,
    onPrimaryContainer = DarkText,
    secondary = DeepRose,
    onSecondary = Color.White,
    background = WarmBackground,
    onBackground = DarkText,
    surface = Color.White,
    onSurface = DarkText,
    surfaceVariant = WarmSurface,
    onSurfaceVariant = MediumText,
    outline = Divider
)

@Composable
fun HERTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HERColorScheme,
        typography = HERTypography,
        content = content
    )
}
