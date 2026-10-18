package com.sportec.sporthub.ui.screens.deportista

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos
import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportec.sporthub.domain.Negocios
import com.sportec.sporthub.domain.Ubicacion
import com.sportec.sporthub.ui.components.Aviso
import com.sportec.sporthub.ui.components.EstadoVacio
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.TipoAviso

@Composable
fun RutaScreen(
    negocioId: String,
    onBack: () -> Unit,
    onCambiarOrigen: () -> Unit,
    onBrujula: () -> Unit
) {
    val negocio = Negocios.negocioDe(negocioId)
    val origen by Ubicacion.origen.collectAsState()

    PantallaBase(titulo = Textos.obtener(R.string.ruta_titulo), onBack = onBack) { padding ->
        if (negocio == null) {
            EstadoVacio(mensaje = stringResource(R.string.ruta_aviso), modifier = Modifier.padding(padding))
            return@PantallaBase
        }
        val distancia = Ubicacion.distanciaKm(origen, negocio.latitud, negocio.longitud)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = negocio.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DatoRuta(stringResource(R.string.ruta_destino), negocio.direccion)
                        DatoRuta(stringResource(R.string.ruta_origen), origen.nombre)
                        DatoRuta(
                            stringResource(R.string.ruta_distancia),
                            stringResource(R.string.ruta_distancia_km, String.format("%.1f", distancia))
                        )
                    }
                }
            }
            item { TrazadoRuta() }
            item { Aviso(texto = stringResource(R.string.ruta_aviso), tipo = TipoAviso.NORMAL) }
            item {
                OutlinedButton(onClick = onCambiarOrigen, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.ruta_cambiar_origen))
                }
            }
            item {
                OutlinedButton(onClick = onBrujula, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.ruta_brujula))
                }
            }
        }
    }
}

@Composable
private fun DatoRuta(etiqueta: String, valor: String) {
    Column {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = valor, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun TrazadoRuta() {
    val trazo = MaterialTheme.colorScheme.primary
    val inicio = MaterialTheme.colorScheme.tertiary
    Card(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(24.dp)
        ) {
            val ruta = Path().apply {
                moveTo(size.width * 0.1f, size.height * 0.9f)
                lineTo(size.width * 0.1f, size.height * 0.45f)
                lineTo(size.width * 0.7f, size.height * 0.45f)
                lineTo(size.width * 0.7f, size.height * 0.1f)
                lineTo(size.width * 0.9f, size.height * 0.1f)
            }
            drawPath(
                path = ruta,
                color = trazo,
                style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            drawCircle(color = inicio, radius = 16f, center = Offset(size.width * 0.1f, size.height * 0.9f))
            drawCircle(color = trazo, radius = 16f, center = Offset(size.width * 0.9f, size.height * 0.1f))
        }
    }
}
