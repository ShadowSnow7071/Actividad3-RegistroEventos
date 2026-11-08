package com.act3.app_eventos.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta modo claro
private val Claro = lightColorScheme(
    primary = Color(0xFF1D6F70),          // Primario
    onPrimary = Color(0xFFECF5EB),
    background = Color(0xFFECF5EB),       // Fondo
    surface = Color(0xFFB9DDCD),          // Superficie
    onBackground = Color(0xFF3F403F),     // Texto principal
    onSurface = Color(0xFF3F403F),
    onSurfaceVariant = Color(0xFF7C7D7C), // Texto secundario
    tertiary = Color(0xFFD5DA24),         // Acento
    outline = Color(0xFFC9E5DE)           // Bordes
)

// Paleta modo oscuro
private val Oscuro = darkColorScheme(
    primary = Color(0xFF1B7072),
    onPrimary = Color(0xFFEAF4EA),
    background = Color(0xFF313334),
    surface = Color(0xFF353E46),
    onBackground = Color(0xFFEAF4EA),
    onSurface = Color(0xFFEAF4EA),
    onSurfaceVariant = Color(0xFFAEAEAE),
    tertiary = Color(0xFFD7DA25),
    outline = Color(0xFF465058)
)

@Composable
fun App_eventosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) Oscuro else Claro,
        content = content
    )
}