package com.sportec.sporthub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class TipoAviso { NORMAL, ADVERTENCIA, ERROR }

private val NormalFondo = Color(0xFFF0F5E9)
private val NormalBorde = Color(0xFF83AB64)
private val NormalTexto = Color(0xFF375539)
private val AdvertenciaFondo = Color(0xFFFFF4E2)
private val AdvertenciaBorde = Color(0xFFD79E3F)
private val AdvertenciaTexto = Color(0xFF715320)
private val ErrorFondo = Color(0xFFFFF0EE)
private val ErrorBorde = Color(0xFFBE4D4D)
private val ErrorTexto = Color(0xFF8A3337)

/** Banda con borde lateral de color, igual a .notice/.notice.warn/.notice.error del prototipo. */
@Composable
fun Aviso(
    texto: String,
    modifier: Modifier = Modifier,
    tipo: TipoAviso = TipoAviso.NORMAL
) {
    val (fondo, borde, contenido) = when (tipo) {
        TipoAviso.NORMAL -> Triple(NormalFondo, NormalBorde, NormalTexto)
        TipoAviso.ADVERTENCIA -> Triple(AdvertenciaFondo, AdvertenciaBorde, AdvertenciaTexto)
        TipoAviso.ERROR -> Triple(ErrorFondo, ErrorBorde, ErrorTexto)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(13.dp))
            .background(fondo)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(borde)
        )
        Text(
            text = texto,
            color = contenido,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(14.dp)
        )
    }
}
