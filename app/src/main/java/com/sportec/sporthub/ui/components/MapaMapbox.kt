package com.sportec.sporthub.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import com.mapbox.maps.plugin.gestures.gestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.CircleAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolygonAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.extension.compose.style.standard.MapboxStandardStyle
import com.mapbox.maps.extension.compose.style.standard.rememberStandardStyleState
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Coordenada
import com.sportec.sporthub.utils.DireccionesMapbox
import com.sportec.sporthub.utils.Geo

data class MarcadorMapa(val id: String, val posicion: Coordenada, val resaltado: Boolean, val atenuado: Boolean)

private val ColorMarcador = Color(0xFF126B47)
private val ColorAtenuado = Color(0xFFADB7AC)
private val ColorOrigen = Color(0xFFDF854B)

@Composable
fun mapboxDisponible(): Boolean = DireccionesMapbox.tokenValido(LocalContext.current.getString(R.string.mapbox_access_token))

@Composable
fun MapaMapbox(
    modifier: Modifier,
    origen: Coordenada,
    origenEsPosicionActual: Boolean,
    marcadores: List<MarcadorMapa> = emptyList(),
    onSeleccionar: (String) -> Unit = {},
    radioKm: Double? = null,
    trazado: List<Coordenada> = emptyList(),
    encuadrarEn: List<Coordenada> = emptyList(),
    mostrarOrigen: Boolean = true,
    onMarcarOrigen: ((Coordenada) -> Unit)? = null,
    onInteraccion: (Boolean) -> Unit = {}
) {
    var ampliado by rememberSaveable { mutableStateOf(false) }
    val contenido: @Composable (Modifier) -> Unit = { m ->
        ContenidoMapa(m, origen, origenEsPosicionActual, marcadores, onSeleccionar, radioKm, trazado, encuadrarEn, mostrarOrigen, onMarcarOrigen)
    }
    val interaccionActual by rememberUpdatedState(onInteraccion)
    DisposableEffect(Unit) { onDispose { interaccionActual(false) } }
    Box(modifier = modifier.pointerInput(Unit) {
        try {
            awaitPointerEventScope {
                while (true) {
                    val evento = awaitPointerEvent(PointerEventPass.Initial)
                    interaccionActual(evento.changes.any { it.pressed })
                }
            }
        } finally { interaccionActual(false) }
    }) {
        contenido(Modifier.fillMaxSize())
        FilledTonalIconButton(
            onClick = { ampliado = true },
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
        ) { Icon(Icons.Filled.Fullscreen, contentDescription = stringResource(R.string.mapa_ampliar)) }
    }
    if (ampliado) {
        Dialog(
            onDismissRequest = { ampliado = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                contenido(Modifier.fillMaxSize())
                FilledTonalIconButton(
                    onClick = { ampliado = false },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                ) { Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.mapa_cerrar)) }
            }
        }
    }
}

/**
 * Contenido del mapa: cámara, puck del usuario cuando hay permiso (Accompanist), radio de búsqueda,
 * trazado de la ruta, marcadores y la pulsación larga del estilo estándar para marcar el origen.
 */
@OptIn(ExperimentalPermissionsApi::class, MapboxExperimental::class)
@Composable
private fun ContenidoMapa(
    modifier: Modifier,
    origen: Coordenada,
    origenEsPosicionActual: Boolean,
    marcadores: List<MarcadorMapa>,
    onSeleccionar: (String) -> Unit,
    radioKm: Double?,
    trazado: List<Coordenada>,
    encuadrarEn: List<Coordenada>,
    mostrarOrigen: Boolean,
    onMarcarOrigen: ((Coordenada) -> Unit)?
) {
    val permisos = rememberPermisosUbicacion()
    val permitido = permisos.permissions.any { it.status.isGranted }
    val viewport = rememberMapViewportState {
        val (centro, zoom) = Geo.encuadre(encuadrarEn.ifEmpty { listOf(origen) })
        setCameraOptions {
            center(Point.fromLngLat(centro.longitud, centro.latitud))
            zoom(zoom)
        }
    }
    val marcarActual by rememberUpdatedState(onMarcarOrigen)
    val estiloEstandar = rememberStandardStyleState {
        interactionsState.onMapLongClicked { contexto ->
            val punto = contexto.coordinateInfo.coordinate
            marcarActual?.invoke(Coordenada(punto.latitude(), punto.longitude()))
            marcarActual != null
        }
    }
    LaunchedEffect(encuadrarEn) {
        if (encuadrarEn.isEmpty()) return@LaunchedEffect
        val (centro, zoom) = Geo.encuadre(encuadrarEn)
        viewport.easeTo(
            com.mapbox.maps.CameraOptions.Builder()
                .center(Point.fromLngLat(centro.longitud, centro.latitud))
                .zoom(zoom)
                .build()
        )
    }

    MapboxMap(
        modifier = modifier,
        mapViewportState = viewport,
        style = { MapboxStandardStyle(standardStyleState = estiloEstandar) }
    ) {
        MapEffect(permitido) { mapView ->
            mapView.gestures.updateSettings {
                scrollEnabled = true
                pinchToZoomEnabled = true
                rotateEnabled = true
                pitchEnabled = true
                doubleTapToZoomInEnabled = true
            }
            mapView.location.updateSettings {
                enabled = permitido
                if (permitido) {
                    locationPuck = createDefault2DPuck(withBearing = true)
                    puckBearingEnabled = true
                }
            }
        }
        if (radioKm != null) {
            val borde = Geo.circulo(origen, radioKm).map { it.punto() }
            PolygonAnnotation(points = listOf(borde)) {
                fillColor = ColorMarcador
                fillOpacity = 0.12
            }
            PolylineAnnotation(points = borde) {
                lineColor = ColorMarcador
                lineWidth = 2.5
                lineOpacity = 0.8
            }
        }
        if (trazado.size >= 2) {
            PolylineAnnotation(points = trazado.map { it.punto() }) {
                lineColor = ColorMarcador
                lineWidth = 6.0
                lineJoin = LineJoin.ROUND
            }
        }
        if (mostrarOrigen && (!origenEsPosicionActual || !permitido)) {
            CircleAnnotation(point = origen.punto()) {
                circleRadius = 7.0
                circleColor = ColorOrigen
                circleStrokeColor = Color.White
                circleStrokeWidth = 2.0
            }
        }
        marcadores.forEach { marcador ->
            CircleAnnotation(point = marcador.posicion.punto(), onClick = { onSeleccionar(marcador.id); true }) {
                circleRadius = if (marcador.resaltado) 12.0 else 9.0
                circleColor = if (marcador.atenuado) ColorAtenuado else ColorMarcador
                circleStrokeColor = Color.White
                circleStrokeWidth = 2.0
            }
        }
    }
}

private fun Coordenada.punto(): Point = Point.fromLngLat(longitud, latitud)
