package com.verdy.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = SageGreen,
    onPrimary = OnSageGreen,
    primaryContainer = SageGreenContainer,
    onPrimaryContainer = OnSageGreenContainer,
    secondary = EarthBrown,
    onSecondary = OnEarthBrown,
    secondaryContainer = EarthBrownContainer,
    onSecondaryContainer = OnEarthBrownContainer,
    tertiary = MossGreen,
    onTertiary = OnMossGreen,
    tertiaryContainer = MossGreenContainer,
    onTertiaryContainer = OnMossGreenContainer,
    background = Cream,
    surface = Surface,
    surfaceVariant = SurfaceVariant,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    error = ErrorRed,
    errorContainer = ErrorContainer,
    onError = OnError,
    onErrorContainer = OnErrorContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = SageGreenDark,
    onPrimary = OnSageGreenDark,
    primaryContainer = SageGreenContainerDark,
    onPrimaryContainer = OnSageGreenContainerDark,
    secondary = EarthBrownDark,
    onSecondary = OnEarthBrownDark,
    secondaryContainer = EarthBrownContainerDark,
    onSecondaryContainer = OnEarthBrownContainerDark,
    tertiary = MossGreenDark,
    onTertiary = OnMossGreenDark,
    tertiaryContainer = MossGreenContainerDark,
    onTertiaryContainer = OnMossGreenContainerDark,
    background = CreamDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurface = OnSurfaceDark,
    onSurfaceVariant = OnSurfaceVariantDark
)

@Composable
fun VerdyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VerdyTypography,
        content = content
    )
}
