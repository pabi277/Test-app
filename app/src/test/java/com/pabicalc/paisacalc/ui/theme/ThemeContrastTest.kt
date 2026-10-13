package com.pabicalc.paisacalc.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeContrastTest {

    @Test
    fun lightThemeTextPairsMeetWcagAaContrast() {
        assertThemeTextContrast(LightColors, "light")
    }

    @Test
    fun darkThemeTextPairsMeetWcagAaContrast() {
        assertThemeTextContrast(DarkColors, "dark")
    }

    private fun assertThemeTextContrast(scheme: ColorScheme, themeName: String) {
        val pairs = listOf(
            "primary" to (scheme.onPrimary to scheme.primary),
            "primary container" to (scheme.onPrimaryContainer to scheme.primaryContainer),
            "secondary" to (scheme.onSecondary to scheme.secondary),
            "secondary container" to (scheme.onSecondaryContainer to scheme.secondaryContainer),
            "tertiary" to (scheme.onTertiary to scheme.tertiary),
            "tertiary container" to (scheme.onTertiaryContainer to scheme.tertiaryContainer),
            "background" to (scheme.onBackground to scheme.background),
            "surface" to (scheme.onSurface to scheme.surface),
            "surface variant" to (scheme.onSurfaceVariant to scheme.surfaceVariant),
            "error" to (scheme.onError to scheme.error),
            "error container" to (scheme.onErrorContainer to scheme.errorContainer),
        )
        pairs.forEach { (name, colors) ->
            val ratio = contrastRatio(colors.first, colors.second)
            assertTrue("$themeName $name contrast was $ratio:1", ratio >= MINIMUM_TEXT_CONTRAST)
        }
    }

    private fun contrastRatio(foreground: Color, background: Color): Double {
        val first = relativeLuminance(foreground)
        val second = relativeLuminance(background)
        val lighter = maxOf(first, second)
        val darker = minOf(first, second)
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun relativeLuminance(color: Color): Double {
        fun linear(channel: Float): Double {
            val srgb = channel.toDouble()
            return if (srgb <= 0.04045) srgb / 12.92 else Math.pow((srgb + 0.055) / 1.055, 2.4)
        }
        return 0.2126 * linear(color.red) + 0.7152 * linear(color.green) + 0.0722 * linear(color.blue)
    }

    private companion object {
        const val MINIMUM_TEXT_CONTRAST = 4.5
    }
}
