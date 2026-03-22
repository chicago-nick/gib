package perozzi.gib.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = AccentStrong,
    secondary = Accent,
    surface = Card,
    background = Canvas,
    onPrimary = Card,
    onSecondary = Ink,
    onSurface = Ink,
    onBackground = Ink,
)

private val DarkColors = darkColorScheme(
    primary = Accent,
    secondary = AccentStrong,
    background = Ink,
    surface = ColorTokens.DarkSurface,
    onPrimary = Ink,
    onSecondary = Card,
    onBackground = Card,
    onSurface = Card,
)

private object ColorTokens {
    val DarkSurface = androidx.compose.ui.graphics.Color(0xFF243036)
}

@Composable
fun GibTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = GibTypography,
        content = content,
    )
}
