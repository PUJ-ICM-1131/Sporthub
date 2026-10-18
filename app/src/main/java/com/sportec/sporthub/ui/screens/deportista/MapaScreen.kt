package com.sportec.sporthub.ui.screens.deportista

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos
import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportec.sporthub.domain.EstadoNegocio
import com.sportec.sporthub.domain.Negocio
import com.sportec.sporthub.domain.Negocios
import com.sportec.sporthub.domain.Origen
import com.sportec.sporthub.domain.Ubicacion
import com.sportec.sporthub.ui.components.Etiqueta
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.TarjetaTint
import com.sportec.sporthub.ui.components.Aviso
import com.sportec.sporthub.ui.components.MapaMapbox
import com.sportec.sporthub.ui.components.MarcadorMapa
import com.sportec.sporthub.ui.components.PermisoSistema
import com.sportec.sporthub.ui.components.SeguimientoUbicacion
import com.sportec.sporthub.ui.components.TipoAviso
import com.sportec.sporthub.ui.components.mapboxDisponible
import com.sportec.sporthub.utils.Coordenada
import androidx.compose.ui.draw.clip
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import androidx.compose.foundation.Canvas

private val FondoMapa = Color(0xFFEDF2E6)
private val PinActivo = Color(0xFF126B47)
private val PinInactivo = Color(0xFFADB7AC)
private val PuntoCentral = Color(0xFFDF854B)
private val CirculoRango = Color(0xFF126B47)

@Composable
fun MapaScreen(
    onBack: () -> Unit,
    onVerServicios: (String) -> Unit,
    onComoLlegar: (String) -> Unit
) {
    SeguimientoUbicacion()
    val negocios by Negocios.negocios.collectAsState()
    val origen by Ubicacion.origen.collectAsState()
    val origenDefinido by Ubicacion.origenDefinido.collectAsState()
    val posicion by Ubicacion.posicionActual.collectAsState()
    val validados = negocios.filter { it.estado == EstadoNegocio.VALIDADO }
    var seleccionado by rememberSaveable { mutableStateOf<String?>(null) }
    var sinLimite by rememberSaveable { mutableStateOf(false) }
    val conMapbox = mapboxDisponible()

    val distancias = remember(validados, origen) {
        validados.associate { it.id to Ubicacion.distanciaKm(origen, it.latitud, it.longitud) }
    }
    val distanciaMaxima = remember(distancias) { ceil(distancias.values.maxOrNull()?.coerceAtLeast(1.0) ?: 5.0) }
    var rangoElegido by rememberSaveable { mutableFloatStateOf(-1f) }
    val rango = if (rangoElegido < 0f) distanciaMaxima.toFloat() else rangoElegido.coerceIn(0.5f, distanciaMaxima.toFloat())
    val visibles = remember(validados, distancias, rango, sinLimite) {
        validados.filter { sinLimite || (distancias[it.id] ?: 0.0) <= rango }.sortedBy { distancias[it.id] }
    }
    val encuadre = remember(validados.size, origen.esPosicionActual, origen.nombre) {
        validados.map { Coordenada(it.latitud, it.longitud) } + Coordenada(origen.latitud, origen.longitud)
    }

    PantallaBase(titulo = Textos.obtener(R.string.texto_planes_cerca_de_ti), onBack = onBack) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = if (origenDefinido) stringResource(R.string.texto_origen_valor, origen.nombre) else stringResource(R.string.ruta_marcar_origen),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item { PermisoSistema() }
            item {
                if (conMapbox) {
                    MapaMapbox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(20.dp)),
                        origen = Coordenada(origen.latitud, origen.longitud),
                        origenEsPosicionActual = origen.esPosicionActual,
                        marcadores = validados.map {
                            MarcadorMapa(it.id, Coordenada(it.latitud, it.longitud), resaltado = it.id == seleccionado, atenuado = visibles.none { v -> v.id == it.id })
                        },
                        onSeleccionar = { seleccionado = it },
                        radioKm = if (sinLimite || !origenDefinido) null else rango.toDouble(),
                        encuadrarEn = encuadre,
                        mostrarOrigen = origenDefinido,
                        onMarcarOrigen = { punto: Coordenada -> Ubicacion.marcarOrigen(punto.latitud, punto.longitud) }.takeIf { posicion == null }
                    )
                } else {
                    MapaEstatico(
                        negocios = validados,
                        origen = origen,
                        visibles = visibles.map { it.id }.toSet(),
                        rango = rango,
                        distanciaMaxima = distanciaMaxima,
                        sinLimite = sinLimite,
                        seleccionado = seleccionado,
                        onSeleccionar = { seleccionado = it }
                    )
                }
            }
            if (!conMapbox) {
                item { Aviso(texto = stringResource(R.string.mapa_sin_token), tipo = TipoAviso.NORMAL) }
            }
            item {
                Text(
                    text = if (sinLimite) Textos.obtener(R.string.texto_mostrando_todos_los_establecimientos_sin_limite_de_rango) else Textos.obtener(R.string.texto_rango_de_busqueda_valor_km, "%.1f".format(rango)),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (!sinLimite) {
                item {
                    Slider(
                        value = rango,
                        onValueChange = { rangoElegido = it },
                        valueRange = 0.5f..distanciaMaxima.toFloat(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item {
                FilterChip(
                    selected = sinLimite,
                    onClick = { sinLimite = !sinLimite },
                    label = { Text(stringResource(R.string.texto_buscar_sin_limite_de_rango)) }
                )
            }
            item {
                Text(text = stringResource(if (conMapbox) R.string.mapa_leyenda_mapbox else R.string.texto_verde_dentro_del_rango_gris_fuera_del_rango_punto),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val elegido = validados.firstOrNull { it.id == seleccionado }
            if (elegido != null) {
                item {
                    TarjetaTint {
                        Text(elegido.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(elegido.direccion, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(stringResource(R.string.mapa_distancia_km, "%.1f".format(distancias[elegido.id] ?: 0.0)), style = MaterialTheme.typography.bodySmall)
                        Row {
                            TextButton(onClick = { onVerServicios(elegido.id) }) { Text(stringResource(R.string.texto_ver_servicios)) }
                            TextButton(onClick = { onComoLlegar(elegido.id) }) { Text(stringResource(R.string.texto_como_llegar)) }
                        }
                    }
                }
            }
            item {
                Text(text = stringResource(R.string.texto_valor_establecimientos_disponibles, visibles.size),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            items(visibles, key = { it.id }) { negocio ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { seleccionado = negocio.id }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(negocio.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(negocio.direccion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(stringResource(R.string.mapa_distancia_km, "%.1f".format(distancias[negocio.id] ?: 0.0)),
                            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    TextButton(onClick = { onComoLlegar(negocio.id) }) { Text(stringResource(R.string.texto_como_llegar)) }
                }
            }
        }
    }
}

@Composable
private fun MapaEstatico(
    negocios: List<Negocio>,
    origen: Origen,
    visibles: Set<String>,
    rango: Float,
    distanciaMaxima: Double,
    sinLimite: Boolean,
    seleccionado: String?,
    onSeleccionar: (String) -> Unit
) {
    val minLat = remember(negocios, origen) { (negocios.map { it.latitud } + origen.latitud).min() - 0.02 }
    val maxLat = remember(negocios, origen) { (negocios.map { it.latitud } + origen.latitud).max() + 0.02 }
    val minLng = remember(negocios, origen) { (negocios.map { it.longitud } + origen.longitud).min() - 0.02 }
    val maxLng = remember(negocios, origen) { (negocios.map { it.longitud } + origen.longitud).max() + 0.02 }
    val rangoLat = max(maxLat - minLat, 0.01)
    val rangoLng = max(maxLng - minLng, 0.01)

    fun fx(lng: Double) = ((lng - minLng) / rangoLng).toFloat().coerceIn(0.08f, 0.92f)
    fun fy(lat: Double) = (1f - ((lat - minLat) / rangoLat).toFloat()).coerceIn(0.08f, 0.92f)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(FondoMapa)
    ) {
        val anchoPx = maxWidth
        val altoPx = maxHeight
        Text(text = stringResource(R.string.texto_bogota),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        )
        if (!sinLimite) {
            val origenXFrac = fx(origen.longitud)
            val origenYFrac = fy(origen.latitud)
            val radioFraccion = (rango / distanciaMaxima.toFloat()).coerceIn(0f, 1f)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centro = Offset(size.width * origenXFrac, size.height * origenYFrac)
                val radioMax = min(size.width, size.height) * 0.42f
                drawCircle(
                    color = CirculoRango,
                    radius = radioMax * radioFraccion,
                    center = centro,
                    alpha = 0.35f,
                    style = Stroke(width = 3f)
                )
                drawCircle(
                    color = CirculoRango,
                    radius = radioMax * radioFraccion,
                    center = centro,
                    alpha = 0.08f
                )
            }
        }
        Box(
            modifier = Modifier
                .offset(x = anchoPx * fx(origen.longitud) - 6.dp, y = altoPx * fy(origen.latitud) - 6.dp)
                .size(12.dp)
                .background(PuntoCentral, CircleShape)
        )
        negocios.forEach { negocio ->
            val activo = negocio.id == seleccionado
            val visible = negocio.id in visibles
            Box(
                modifier = Modifier
                    .offset(x = anchoPx * fx(negocio.longitud) - 16.dp, y = altoPx * fy(negocio.latitud) - 16.dp)
                    .size(32.dp)
                    .clickable { onSeleccionar(negocio.id) }
                    .background(if (activo) PinActivo else if (visible) PinActivo.copy(alpha = 0.7f) else PinInactivo, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = negocio.id.removePrefix("b"),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
