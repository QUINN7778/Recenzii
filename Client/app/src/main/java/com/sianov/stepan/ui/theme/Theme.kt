package com.sianov.stepan.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import android.os.Build

@Composable
fun ForStepanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    fontSizeMultiplier: Float = 1.0f,
    themeColorIndex: Int = 0,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> {
            if (darkTheme) {
                val primaryColor = when(themeColorIndex) {
                    1 -> TheatrePrimaryDark
                    2 -> OceanPrimaryDark
                    3 -> ForestPrimaryDark
                    4 -> MinimalPrimaryDark
                    else -> Purple80
                }
                darkColorScheme(
                    primary = primaryColor,
                    secondary = PurpleGrey80,
                    tertiary = Pink80,
                    onPrimary = Color.Black,
                    onSurface = Color.White,
                    onSurfaceVariant = Color(0xFFCAC4D0),
                    outline = Color(0xFF938F99),
                    surfaceVariant = Color(0xFF49454F)
                )
            } else {
                val primaryColor = when(themeColorIndex) {
                    1 -> TheatrePrimary
                    2 -> OceanPrimary
                    3 -> ForestPrimary
                    4 -> MinimalPrimary
                    else -> Purple40
                }
                lightColorScheme(
                    primary = primaryColor,
                    secondary = PurpleGrey40,
                    tertiary = Pink40,
                    onPrimary = Color.White,
                    onSurface = Color.Black,
                    onSurfaceVariant = Color(0xFF49454F),
                    outline = Color(0xFF79747E),
                    surfaceVariant = Color(0xFFE7E0EC)
                )
            }
        }
    }

    val scaledTypography = remember(fontSizeMultiplier) {
        Typography(
            bodyLarge = Typography.bodyLarge.copy(fontSize = (Typography.bodyLarge.fontSize.value * fontSizeMultiplier).sp),
            titleLarge = Typography.titleLarge.copy(fontSize = (Typography.titleLarge.fontSize.value * fontSizeMultiplier).sp),
            labelMedium = Typography.labelMedium.copy(fontSize = (Typography.labelMedium.fontSize.value * fontSizeMultiplier).sp),
            bodyMedium = Typography.bodyMedium.copy(fontSize = (Typography.bodyMedium.fontSize.value * fontSizeMultiplier).sp),
            titleMedium = Typography.titleMedium.copy(fontSize = (Typography.titleMedium.fontSize.value * fontSizeMultiplier).sp)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography,
        content = content
    )
}
