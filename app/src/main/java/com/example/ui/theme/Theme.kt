package com.example.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = SleekDarkPrimary,
    onPrimary = SleekDarkBg,
    primaryContainer = SleekDarkContainer,
    onPrimaryContainer = SleekOnDarkContainer,
    secondary = SleekDarkPrimary,
    background = SleekDarkBg,
    surface = SleekDarkSurface,
    surfaceVariant = SleekDarkSurface,
    onBackground = SleekTextLight,
    onSurface = SleekTextLight,
    onSurfaceVariant = SleekTextSecondaryLight
)

private val LightColorScheme = lightColorScheme(
    primary = SleekIndigoPrimary,
    onPrimary = Color.White,
    primaryContainer = SleekIndigoContainer,
    onPrimaryContainer = SleekOnIndigoContainer,
    secondary = SleekIndigoPrimary,
    background = SleekLightBg,
    surface = Color.White,
    surfaceVariant = SleekLightSurface,
    onBackground = SleekTextDark,
    onSurface = SleekTextDark,
    onSurfaceVariant = SleekTextSecondaryDark
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // For design aesthetic, force Sleek customized design over OS Dynamic colors
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
