package com.sportec.sporthub.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.sportec.sporthub.domain.Actividad
import com.sportec.sporthub.domain.DatosMock
import com.sportec.sporthub.domain.TipoActividad

private val FondosIlustracion = listOf(Color(0xFFBADBB4), Color(0xFFEDC8AA), Color(0xFFB6D9D0), Color(0xFFD1C4DF))
private val CanchaEspacio = Color(0xFF417F66)
private val CanchaClase = Color(0xFFBE895E)
private val LineaCancha = Color(0xFFF4F2D9)
private val MatVerde = Color(0xFFD9E8BD)
private val MatDurazno = Color(0xFFF1D3B5)

/** Ilustración generativa por categoría, equivalente a la función art() del prototipo (.art svg). */
@Composable
fun IlustracionActividad(actividad: Actividad, modifier: Modifier = Modifier) {
    if (actividad.fotoUri != null) {
        AsyncImage(
            model = actividad.fotoUri,
            contentDescription = actividad.nombre,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .fillMaxWidth()
                .height(142.dp)
                .clip(RoundedCornerShape(topStart = 19.dp, topEnd = 19.dp))
        )
        return
    }
    val indice = DatosMock.categorias.indexOfFirst { it.nombre == actividad.categoria }.coerceAtLeast(0)
    val fondo = FondosIlustracion[indice % FondosIlustracion.size]
    val esEspacio = actividad.tipo == TipoActividad.ESPACIO
    val colorCancha = if (esEspacio) CanchaEspacio else CanchaClase

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(142.dp)
            .clip(RoundedCornerShape(topStart = 19.dp, topEnd = 19.dp))
            .background(fondo)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.15f),
                radius = size.width * 0.28f,
                center = Offset(size.width * 0.92f, size.height * 0.1f)
            )
            rotate(degrees = 12f, pivot = Offset(size.width * 0.5f, size.height * 0.5f)) {
                val anchoRect = size.width * 0.6f
                val altoRect = size.height * 1.2f
                val left = size.width * 0.2f
                val top = -size.height * 0.08f
                drawRoundRect(
                    color = colorCancha,
                    topLeft = Offset(left, top),
                    size = Size(anchoRect, altoRect),
                    cornerRadius = CornerRadius(18f, 18f)
                )
                if (esEspacio) {
                    val grosor = 3.dp.toPx()
                    drawLine(LineaCancha, Offset(left, top + altoRect / 2), Offset(left + anchoRect, top + altoRect / 2), strokeWidth = grosor)
                    drawLine(LineaCancha, Offset(left + anchoRect * 0.3f, top), Offset(left + anchoRect * 0.3f, top + altoRect), strokeWidth = grosor)
                    drawLine(LineaCancha, Offset(left + anchoRect * 0.7f, top), Offset(left + anchoRect * 0.7f, top + altoRect), strokeWidth = grosor)
                } else {
                    drawRoundRect(
                        color = MatVerde,
                        topLeft = Offset(left + anchoRect * 0.14f, top + altoRect * 0.16f),
                        size = Size(anchoRect * 0.28f, altoRect * 0.68f),
                        cornerRadius = CornerRadius(30f, 30f)
                    )
                    drawRoundRect(
                        color = MatDurazno,
                        topLeft = Offset(left + anchoRect * 0.56f, top + altoRect * 0.24f),
                        size = Size(anchoRect * 0.28f, altoRect * 0.68f),
                        cornerRadius = CornerRadius(30f, 30f)
                    )
                }
            }
            drawCircle(
                color = Color(0xFF173C2E).copy(alpha = 0.25f),
                radius = size.height * 0.16f,
                center = Offset(size.height * 0.16f, size.height * 0.94f)
            )
        }
        Etiqueta(
            texto = if (esEspacio) "Espacio completo" else "Clase · ${actividad.capacidad} cupo${if (actividad.capacidad != 1) "s" else ""}",
            contenedor = Color.White.copy(alpha = 0.94f),
            contenido = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        )
    }
}
