package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
  primary = MathPrimaryDark,
  onPrimary = MathOnPrimaryDark,
  primaryContainer = MathPrimaryContainerDark,
  onPrimaryContainer = MathOnPrimaryContainerDark,
  secondary = MathSecondaryDark,
  onSecondary = MathOnSecondaryDark,
  secondaryContainer = MathSecondaryContainerDark,
  onSecondaryContainer = MathOnSecondaryContainerDark,
  tertiary = MathTertiaryDark,
  onTertiary = MathOnTertiaryDark,
  tertiaryContainer = MathTertiaryContainerDark,
  onTertiaryContainer = MathOnTertiaryContainerDark,
  background = MathBackgroundDark,
  onBackground = MathOnBackgroundDark,
  surface = MathSurfaceDark,
  onSurface = MathOnSurfaceDark,
  surfaceVariant = MathSurfaceVariantDark,
  onSurfaceVariant = MathOnSurfaceVariantDark,
)

private val LightColorScheme = lightColorScheme(
  primary = MathPrimaryLight,
  onPrimary = MathOnPrimaryLight,
  primaryContainer = MathPrimaryContainerLight,
  onPrimaryContainer = MathOnPrimaryContainerLight,
  secondary = MathSecondaryLight,
  onSecondary = MathOnSecondaryLight,
  secondaryContainer = MathSecondaryContainerLight,
  onSecondaryContainer = MathOnSecondaryContainerLight,
  tertiary = MathTertiaryLight,
  onTertiary = MathOnTertiaryLight,
  tertiaryContainer = MathTertiaryContainerLight,
  onTertiaryContainer = MathOnTertiaryContainerLight,
  background = MathBackgroundLight,
  onBackground = MathOnBackgroundLight,
  surface = MathSurfaceLight,
  onSurface = MathOnSurfaceLight,
  surfaceVariant = MathSurfaceVariantLight,
  onSurfaceVariant = MathOnSurfaceVariantLight,
)

@Composable
fun MathMasterTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MathMasterTheme(darkTheme = darkTheme, content = content)
}

