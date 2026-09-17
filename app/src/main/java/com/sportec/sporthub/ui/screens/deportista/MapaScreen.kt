package com.sportec.sporthub.ui.screens.deportista

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
import kotlin.math.max
import kotlin.math.min
import androidx.compose.foundation.Canvas

private val FondoMapa = Color(0xFFEDF2E6)
private val PinActivo = Color(0xFF126B47)
private val PinInactivo = Color(0xFFADB7AC)
private val PuntoCentral = Color(0xFFDF854B)
private val CirculoRango = Color(0xFF126B47)

@Composable
fun MapaScreen(onBack: () -> Unit, onVerServicios: (String) -> Unit, onElegirOrigen: () -> Unit) {
    val negocios by Negocios.negocios.collectAsState()
    val origen by Ubicacion.origen.collectAsState()
    val validados = negocios.filter { it.estado == EstadoNegocio.VALIDADO }
    var seleccionado by rememberSaveable { mutableStateOf<String?>(null) }
    var sinLimite by rememberSaveable { mutableStateOf(false) }

    val distanciaMaxima = remember(validados, origen) {
        validados.maxOfOrNull { Ubicacion.distanciaKm(origen, it.latitud, it.longitud) }?.coerceAtLeast(1.0) ?: 5.0
    }
    var rango by rememberSaveable(distanciaMaxima) { mutableFloatStateOf(distanciaMaxima.toFloat()) }
    val visibles = remember(validados, origen, rango, sinLimite) {
        if (sinLimite) validados else validados.filter { Ubicacion.distanciaKm(origen, it.latitud, it.longitud) <= rango }
    }

    PantallaBase(titulo = "Planes cerca de ti", onBack = onBack) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Origen: ${origen.nombre}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onElegirOrigen) { Text("Elegir origen") }
                }
            }
            item {
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
            item {
                Text(
                    text = if (sinLimite) "Mostrando todos los establecimientos, sin límite de rango." else "Rango de búsqueda: ${"%.1f".format(rango)} km",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (!sinLimite) {
                item {
                    Slider(
                        value = rango,
                        onValueChange = { rango = it },
                        valueRange = 0.5f..distanciaMaxima.toFloat(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item {
                FilterChip(
                    selected = sinLimite,
                    onClick = { sinLimite = !sinLimite },
                    label = { Text("Buscar sin límite de rango") }
                )
            }
            item {
                Text(
                    text = "Verde: dentro del rango · Gris: fuera del rango · Punto naranja: origen. Distancia geográfica, no recorrido por calles.",
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
                        TextButton(onClick = { onVerServicios(elegido.id) }) { Text("Ver servicios") }
                    }
                }
            }
            item {
                Text(
                    text = "${visibles.size} establecimientos disponibles",
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
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(negocio.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(negocio.direccion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = { onVerServicios(negocio.id) }) { Text("Ver servicios") }
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
            .background(FondoMapa, RoundedCornerShape(20.dp))
    ) {
        val anchoPx = maxWidth
        val altoPx = maxHeight
        Text(
            text = "BOGOTÁ",
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
