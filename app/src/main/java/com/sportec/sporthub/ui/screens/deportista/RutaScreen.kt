package com.sportec.sporthub.ui.screens.deportista

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos
import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportec.sporthub.domain.ModoViaje
import com.sportec.sporthub.domain.Negocios
import com.sportec.sporthub.domain.Trayecto
import com.sportec.sporthub.domain.Trayectos
import com.sportec.sporthub.domain.Ubicacion
import com.sportec.sporthub.ui.components.Aviso
import com.sportec.sporthub.ui.components.EstadoVacio
import com.sportec.sporthub.ui.components.MapaMapbox
import com.sportec.sporthub.ui.components.MarcadorMapa
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.PermisoSistema
import com.sportec.sporthub.ui.components.SeguimientoUbicacion
import com.sportec.sporthub.ui.components.TipoAviso
import com.sportec.sporthub.ui.components.mapboxDisponible
import com.sportec.sporthub.utils.Coordenada
import com.sportec.sporthub.utils.DireccionesMapbox
import com.sportec.sporthub.utils.Geo
import kotlin.math.roundToInt

@Composable
fun RutaScreen(
    negocioId: String,
    onBack: () -> Unit,
    onCambiarOrigen: () -> Unit,
    onBrujula: () -> Unit
) {
    SeguimientoUbicacion()
    val negocio = Negocios.negocioDe(negocioId)
    val origen by Ubicacion.origen.collectAsState()
    val posicion by Ubicacion.posicionActual.collectAsState()
    val modo by Trayectos.modo.collectAsState()
    val guardado by Trayectos.actual.collectAsState()
    val token = stringResource(R.string.mapbox_access_token)
    val conMapbox = mapboxDisponible()
    var calculando by remember { mutableStateOf(false) }

    val inicio = Coordenada(origen.latitud, origen.longitud)
    val trayecto = if (negocio == null) null else Trayectos.vigente(negocioId, modo, inicio, guardado)

    LaunchedEffect(negocioId) { Ubicacion.fijarDestino(negocioId) }
    LaunchedEffect(negocioId, modo, trayecto == null) {
        if (negocio == null || trayecto != null) return@LaunchedEffect
        calculando = true
        try {
            val actual = Ubicacion.origen.value
            val desde = Coordenada(actual.latitud, actual.longitud)
            val hasta = Coordenada(negocio.latitud, negocio.longitud)
            val nuevo = DireccionesMapbox.consultar(token, negocioId, modo, desde, hasta)
                ?: Trayectos.estimado(negocioId, modo, desde, hasta)
            Trayectos.guardar(nuevo)
        } finally {
            calculando = false
        }
    }

    PantallaBase(titulo = Textos.obtener(R.string.ruta_titulo), onBack = onBack) { padding ->
        if (negocio == null) {
            EstadoVacio(mensaje = stringResource(R.string.texto_el_establecimiento_ya_no_existe), modifier = Modifier.padding(padding))
            return@PantallaBase
        }
        val destino = Coordenada(negocio.latitud, negocio.longitud)
        val encuadre = remember(trayecto) { trayecto?.puntos.orEmpty().ifEmpty { listOf(inicio, destino) } }
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ModoViaje.entries.forEach {
                        FilterChip(
                            selected = it == modo,
                            onClick = { Trayectos.elegirModo(it) },
                            label = { Text(stringResource(it.etiqueta)) }
                        )
                    }
                }
            }
            item {
                if (conMapbox) {
                    MapaMapbox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(20.dp)),
                        origen = inicio,
                        origenEsPosicionActual = origen.esPosicionActual,
                        marcadores = listOf(MarcadorMapa(negocio.id, destino, resaltado = true, atenuado = false)),
                        trazado = trayecto?.puntos.orEmpty(),
                        encuadrarEn = encuadre
                    )
                } else {
                    TrazadoRuta(trayecto?.puntos ?: listOf(inicio, destino))
                }
            }
            if (calculando) item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DatoRuta(stringResource(R.string.ruta_destino), negocio.direccion)
                        DatoRuta(stringResource(R.string.ruta_origen), origen.nombre)
                        if (trayecto != null) {
                            DatoRuta(
                                stringResource(R.string.ruta_distancia),
                                stringResource(R.string.ruta_distancia_km, String.format("%.1f", trayecto.distanciaMetros / 1000))
                            )
                            DatoRuta(
                                stringResource(R.string.ruta_duracion),
                                stringResource(R.string.ruta_duracion_min, (trayecto.duracionSegundos / 60).roundToInt().coerceAtLeast(1))
                            )
                        }
                    }
                }
            }
            item { SeguimientoTrayecto(trayecto, destino, origen.esPosicionActual, posicion?.let { Coordenada(it.latitud, it.longitud) }) }
            if (trayecto != null) {
                item {
                    Aviso(
                        texto = stringResource(if (trayecto.estimado) R.string.ruta_aviso_estimada else R.string.ruta_aviso_mapbox),
                        tipo = TipoAviso.NORMAL
                    )
                }
            }
            item { PermisoSistema() }
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
private fun SeguimientoTrayecto(trayecto: Trayecto?, destino: Coordenada, siguiendo: Boolean, posicion: Coordenada?) {
    val texto = when {
        !siguiendo -> stringResource(R.string.ruta_sin_seguimiento)
        posicion == null -> stringResource(R.string.ruta_buscando_posicion)
        Geo.distanciaMetros(posicion, destino) <= Trayectos.LLEGADA_M -> stringResource(R.string.ruta_llegaste)
        trayecto == null -> stringResource(R.string.ruta_calculando)
        else -> stringResource(R.string.ruta_restante_km, String.format("%.2f", Geo.restanteMetros(posicion, trayecto.puntos) / 1000))
    }
    Aviso(texto = texto, tipo = TipoAviso.NORMAL)
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

/** Respaldo sin Mapbox: proyecta el trazado real (o la línea recta estimada) dentro de la tarjeta. */
@Composable
private fun TrazadoRuta(puntos: List<Coordenada>) {
    val trazo = MaterialTheme.colorScheme.primary
    val inicio = MaterialTheme.colorScheme.tertiary
    Card(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(24.dp)
        ) {
            val minLat = puntos.minOf { it.latitud }
            val maxLat = puntos.maxOf { it.latitud }
            val minLng = puntos.minOf { it.longitud }
            val maxLng = puntos.maxOf { it.longitud }
            val escala = minOf(
                size.width / (maxLng - minLng).coerceAtLeast(1e-6),
                size.height / (maxLat - minLat).coerceAtLeast(1e-6)
            )
            val margenX = (size.width - (maxLng - minLng) * escala).toFloat() / 2
            val margenY = (size.height - (maxLat - minLat) * escala).toFloat() / 2
            fun proyectar(c: Coordenada) = Offset(
                margenX + ((c.longitud - minLng) * escala).toFloat(),
                size.height - margenY - ((c.latitud - minLat) * escala).toFloat()
            )
            val ruta = Path().apply {
                puntos.forEachIndexed { i, c -> proyectar(c).let { if (i == 0) moveTo(it.x, it.y) else lineTo(it.x, it.y) } }
            }
            drawPath(
                path = ruta,
                color = trazo,
                style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            drawCircle(color = inicio, radius = 16f, center = proyectar(puntos.first()))
            drawCircle(color = trazo, radius = 16f, center = proyectar(puntos.last()))
        }
    }
}
