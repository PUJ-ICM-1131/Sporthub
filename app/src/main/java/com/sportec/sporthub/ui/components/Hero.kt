package com.sportec.sporthub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val VerdeHeroInicio = Color(0xFF163F34)
private val VerdeHeroFin = Color(0xFF208152)
private val VerdeHeroTexto = Color(0xFFD2E7D4)

/** Tarjeta oscura degradada, igual a .hero del prototipo: título, subtítulo y una etiqueta opcional. */
@Composable
fun TarjetaHero(
    titulo: String,
    subtitulo: String,
    modifier: Modifier = Modifier,
    pillTexto: String? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(23.dp))
            .background(Brush.linearGradient(listOf(VerdeHeroInicio, VerdeHeroFin)))
            .padding(20.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 36.dp, y = 36.dp)
                .size(170.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        )
        Column {
            if (pillTexto != null) {
                Etiqueta(
                    texto = pillTexto,
                    contenedor = Color(0xFFD7F4A2),
                    contenido = Color(0xFF284832)
                )
                Box(modifier = Modifier.padding(top = 10.dp))
            }
            Text(
                text = titulo,
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitulo,
                color = VerdeHeroTexto,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
