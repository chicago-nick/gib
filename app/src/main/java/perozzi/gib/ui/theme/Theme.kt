package perozzi.gib.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = AccentStrong,
    secondary = Accent,
    primaryContainer = Card,
    secondaryContainer = Canvas,
    surface = Card,
    background = Canvas,
    onPrimary = Card,
    onPrimaryContainer = Ink,
    onSecondary = Ink,
    onSurface = Ink,
    onBackground = Ink,
)

private val DarkColors = darkColorScheme(
    primary = Card,
    secondary = Accent,
    primaryContainer = Ink,
    background = Ink,
    surface = ColorTokens.DarkSurface,
    onPrimary = Ink,
    onPrimaryContainer = Card,
    onSecondary = Card,
    onBackground = Card,
    onSurface = Card,
)

private object ColorTokens {
    val DarkSurface = androidx.compose.ui.graphics.Color(0xFF1A1A1A)
}

@Composable
fun GibTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = GibTypography,
        content = content,
    )
}
