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
      onBackground = TextPrimary,
      onSurface = TextPrimary,
      onSurfaceVariant = TextSecondary
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force Dark Mode
  dynamicColor: Boolean = false, // Disable dynamic colors for custom premium look
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
