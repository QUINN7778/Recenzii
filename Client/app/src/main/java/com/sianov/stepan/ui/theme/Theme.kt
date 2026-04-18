package com.sianov.stepan.ui.theme

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)
private val LightColorScheme = lightColorScheme(primary = Purple40, secondary = PurpleGrey40, tertiary = Pink40)

private val TheatreLightColorScheme = lightColorScheme(primary = TheatrePrimary, secondary = TheatreSecondary, tertiary = TheatreTertiary)
private val TheatreDarkColorScheme = darkColorScheme(primary = TheatreSecondary, secondary = TheatrePrimary, tertiary = TheatreTertiary)

private val OceanLightColorScheme = lightColorScheme(primary = OceanPrimary, secondary = OceanSecondary, tertiary = OceanTertiary)
private val OceanDarkColorScheme = darkColorScheme(primary = OceanSecondary, secondary = OceanPrimary, tertiary = OceanTertiary)

private val ForestLightColorScheme = lightColorScheme(primary = ForestPrimary, secondary = ForestSecondary, tertiary = ForestTertiary)
private val ForestDarkColorScheme = darkColorScheme(primary = ForestSecondary, secondary = ForestPrimary, tertiary = ForestTertiary)

fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun ForStepanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    fontSizeMultiplier: Float = 1.0f,
    themeColorIndex: Int = 0,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    val colorScheme = when {
        dynamicColor && themeColorIndex == 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        themeColorIndex == 1 -> if (darkTheme) TheatreDarkColorScheme else TheatreLightColorScheme
        themeColorIndex == 2 -> if (darkTheme) OceanDarkColorScheme else OceanLightColorScheme
        themeColorIndex == 3 -> if (darkTheme) ForestDarkColorScheme else ForestLightColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Используем Typography из файла Type.kt, который, надеюсь, существует
        content = content
    )
}
