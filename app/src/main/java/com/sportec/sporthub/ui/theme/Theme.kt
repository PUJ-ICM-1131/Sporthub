package com.sportec.sporthub.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Radios de esquina calcados de los tokens CSS del prototipo: .slot/input 12px, .primary/.card button 13px, .card 17px, .hero 23px. */
private val SportHubShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(13.dp),
    medium = RoundedCornerShape(17.dp),
    large = RoundedCornerShape(23.dp),
    extraLarge = RoundedCornerShape(22.dp)
)

private val EsquemaClaro = lightColorScheme(
    primary = VerdeSportHub,
    onPrimary = Blanco,
    primaryContainer = Menta,
    onPrimaryContainer = Tinta,
    secondary = Tenue,
    onSecondary = Blanco,
    secondaryContainer = Linea,
    onSecondaryContainer = Tinta,
    tertiary = CalidoTexto,
    onTertiary = Blanco,
    tertiaryContainer = Calido,
    onTertiaryContainer = CalidoTexto,
    background = Papel,
    onBackground = Tinta,
    surface = Papel,
    onSurface = Tinta,
    surfaceVariant = Linea,
    onSurfaceVariant = Tenue,
    surfaceContainerLowest = Blanco,
    surfaceContainerLow = Color(0xFFF7FBF8),
    surfaceContainer = Color(0xFFE8F1EB),
    surfaceContainerHigh = Color(0xFFE2ECE5),
    surfaceContainerHighest = Color(0xFFDBE7DF),
    outline = Tenue,
    outlineVariant = Linea,
    error = Rojo,
    onError = Blanco
)

private val EsquemaOscuro = darkColorScheme(
    primary = VerdeClaro,
    onPrimary = VerdeProfundo,
    primaryContainer = VerdeProfundo,
    onPrimaryContainer = Menta,
    secondary = TenueOscuro,
    onSecondary = FondoOscuro,
    secondaryContainer = LineaOscura,
    onSecondaryContainer = TextoOscuro,
    tertiary = Calido,
    onTertiary = CalidoOscuro,
    tertiaryContainer = CalidoOscuro,
    onTertiaryContainer = Calido,
    background = FondoOscuro,
    onBackground = TextoOscuro,
    surface = FondoOscuro,
    onSurface = TextoOscuro,
    surfaceVariant = LineaOscura,
    onSurfaceVariant = TenueOscuro,
    surfaceContainerLowest = Color(0xFF0A120E),
    surfaceContainerLow = SuperficieOscura,
    surfaceContainer = Color(0xFF1C2B24),
    surfaceContainerHigh = Color(0xFF22332B),
    surfaceContainerHighest = Color(0xFF293B32),
    outline = TenueOscuro,
    outlineVariant = LineaOscura,
    error = RojoClaro,
    onError = Color(0xFF5C1016)
)

@Composable
fun SportHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = SportHubTypography,
        shapes = SportHubShapes,
        content = content
    )
}
