package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
      primary = PurplePrimary,
      secondary = CyanAccent,
      tertiary = CyanAccent,
      background = DarkBackground,
      surface = SurfaceDark,
      surfaceTint = SurfaceGlass,
      surfaceVariant = SurfaceDark,
      onPrimary = Color.White,
      onSecondary = Color.Black,
      onBackground = TextPrimaryDark,
      onSurface = TextPrimaryDark,
      onSurfaceVariant = TextSecondaryDark
  )

private val LightColorScheme =
  lightColorScheme(
      primary = PurplePrimary,
      secondary = CyanAccent,
      tertiary = CyanAccent,
      background = LightBackground,
      surface = SurfaceLight,
      surfaceTint = SurfaceGlassLight,
      surfaceVariant = SurfaceLight,
      onPrimary = Color.White,
      onSecondary = Color.Black,
      onBackground = TextPrimaryLight,
      onSurface = TextPrimaryLight,
      onSurfaceVariant = TextSecondaryLight
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Disable dynamic colors for custom premium look
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
