package com.infinum.sentinel.ui.shared.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import com.infinum.sentinel.R

/**
 * Material 3 theme for Sentinel's Compose screens.
 *
 * It mirrors `Sentinel.BaseTheme`: every color role that theme sets is read from the same `sentinel_*` color
 * resources, so `values-night` keeps driving dark mode. Roles the XML theme doesn't set keep the Material 3
 * baseline, as they do in the XML theme. Typography and shapes are the Material 3 defaults, which is what the
 * `TextAppearance.Material3.*` styles in the XML screens resolve to.
 */
@Composable
internal fun SentinelTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = sentinelColorScheme(),
        content = content,
    )
}

@Composable
private fun sentinelColorScheme(): ColorScheme {
    val baseline = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    val primary = colorResource(R.color.sentinel_primary)
    return baseline.copy(
        primary = primary,
        onPrimary = colorResource(R.color.sentinel_onPrimary),
        primaryContainer = colorResource(R.color.sentinel_primaryContainer),
        onPrimaryContainer = colorResource(R.color.sentinel_onPrimaryContainer),
        inversePrimary = colorResource(R.color.sentinel_primaryInverse),
        secondary = colorResource(R.color.sentinel_secondary),
        onSecondary = colorResource(R.color.sentinel_onSecondary),
        secondaryContainer = colorResource(R.color.sentinel_secondaryContainer),
        onSecondaryContainer = colorResource(R.color.sentinel_onSecondaryContainer),
        tertiary = colorResource(R.color.sentinel_tertiary),
        onTertiary = colorResource(R.color.sentinel_onTertiary),
        tertiaryContainer = colorResource(R.color.sentinel_tertiaryContainer),
        onTertiaryContainer = colorResource(R.color.sentinel_onTertiaryContainer),
        error = colorResource(R.color.sentinel_error),
        onError = colorResource(R.color.sentinel_onError),
        errorContainer = colorResource(R.color.sentinel_errorContainer),
        onErrorContainer = colorResource(R.color.sentinel_onErrorContainer),
        background = colorResource(R.color.sentinel_background),
        onBackground = colorResource(R.color.sentinel_onBackground),
        surface = colorResource(R.color.sentinel_surface),
        onSurface = colorResource(R.color.sentinel_onSurface),
        surfaceVariant = colorResource(R.color.sentinel_surfaceVariant),
        onSurfaceVariant = colorResource(R.color.sentinel_onSurfaceVariant),
        outline = colorResource(R.color.sentinel_outline),
        inverseSurface = colorResource(R.color.sentinel_inverseSurface),
        inverseOnSurface = colorResource(R.color.sentinel_inverseOnSurface),
        surfaceTint = primary,
    )
}
