package app.arcane.android.ui.theme

import androidx.compose.ui.graphics.toArgb
import org.junit.Assert.assertEquals
import org.junit.Test

class ArcaneColorTokensTest {
    @Test
    fun `Arcane theme defaults to dark app palette regardless of system theme`() {
        assertEquals(true, UseArcaneDarkThemeByDefault)
    }

    @Test
    fun `dark theme uses Arcane web palette foundations`() {
        assertEquals(0xFF08080A.toInt(), ArcaneColors.Background.toArgb())
        assertEquals(0xFF111116.toInt(), ArcaneColors.Surface.toArgb())
        assertEquals(0xFF181822.toInt(), ArcaneColors.SurfaceElevated.toArgb())
        assertEquals(0xFF8E51FF.toInt(), ArcaneColors.PrimaryPurple.toArgb())
        assertEquals(0xFF00D084.toInt(), ArcaneColors.SuccessGreen.toArgb())
        assertEquals(0xFF67E8F9.toInt(), ArcaneColors.PortBlue.toArgb())
    }
}
