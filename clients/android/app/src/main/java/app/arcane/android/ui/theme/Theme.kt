package app.arcane.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ArcaneDarkColors = darkColorScheme(
    primary = ArcaneColors.PrimaryPurple,
    onPrimary = Color.White,
    primaryContainer = ArcaneColors.PrimaryPurpleContainer,
    onPrimaryContainer = ArcaneColors.TextPrimary,
    secondary = ArcaneColors.PortBlue,
    onSecondary = ArcaneColors.Background,
    secondaryContainer = Color(0xFF082F3A),
    onSecondaryContainer = ArcaneColors.TextPrimary,
    tertiary = ArcaneColors.SuccessGreen,
    onTertiary = ArcaneColors.Background,
    tertiaryContainer = ArcaneColors.SuccessGreenContainer,
    onTertiaryContainer = ArcaneColors.SuccessGreen,
    background = ArcaneColors.Background,
    onBackground = ArcaneColors.TextPrimary,
    surface = ArcaneColors.Surface,
    onSurface = ArcaneColors.TextPrimary,
    surfaceVariant = ArcaneColors.SurfaceElevated,
    onSurfaceVariant = ArcaneColors.TextSecondary,
    surfaceContainerLowest = ArcaneColors.Background,
    surfaceContainerLow = ArcaneColors.Surface,
    surfaceContainer = ArcaneColors.SurfaceElevated,
    surfaceContainerHigh = ArcaneColors.SurfaceMuted,
    outline = ArcaneColors.Border,
    outlineVariant = ArcaneColors.BorderSelected,
    error = ArcaneColors.ErrorRed,
)

private val ArcaneLightColors = lightColorScheme(
    primary = ArcaneColors.PrimaryPurpleDark,
    secondary = Color(0xFF0369A1),
    tertiary = Color(0xFF047857),
    background = Color(0xFFFAFAFC),
    surface = Color.White,
    surfaceVariant = Color(0xFFF1F1F6),
    outline = Color(0xFFD8D8E2),
)

private val AppTypography = Typography()

val ArcaneDarkColorScheme: ColorScheme = ArcaneDarkColors

@Composable
fun ArcaneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) ArcaneDarkColors else ArcaneLightColors,
        typography = AppTypography,
        content = content,
    )
}
