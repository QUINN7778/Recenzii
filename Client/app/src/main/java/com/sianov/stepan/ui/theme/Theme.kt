package com.sianov.stepan.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@Composable
fun ForStepanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontSizeMultiplier: Float = 1.0f,
    themeColorIndex: Int = 0,
    content: @Composable () -> Unit
) {
    val primaryColor = when(themeColorIndex) {
        1 -> TheatrePrimary
        2 -> OceanPrimary
        3 -> ForestPrimary
        4 -> MinimalPrimary
        else -> Color(0xFF6650a4)
    }

    val colorScheme = if (darkTheme) {
        darkColorScheme(primary = primaryColor)
    } else {
        lightColorScheme(primary = primaryColor)
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
