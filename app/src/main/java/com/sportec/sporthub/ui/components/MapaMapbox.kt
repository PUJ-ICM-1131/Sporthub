package com.sportec.sporthub.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.CircleAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolygonAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Coordenada
import com.sportec.sporthub.utils.DireccionesMapbox
import com.sportec.sporthub.utils.Geo
import com.sportec.sporthub.utils.RastreadorUbicacion

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
    encuadrarEn: List<Coordenada> = emptyList()
) {
    val context = LocalContext.current
    val versionPermisos by CambiosPermisos.version.collectAsState()
    val permitido = remember(versionPermisos) { RastreadorUbicacion(context.applicationContext).tienePermiso() }
    val viewport = rememberMapViewportState {
        val (centro, zoom) = Geo.encuadre(encuadrarEn.ifEmpty { listOf(origen) })
        setCameraOptions {
            center(Point.fromLngLat(centro.longitud, centro.latitud))
            zoom(zoom)
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

    MapboxMap(modifier = modifier, mapViewportState = viewport) {
        MapEffect(permitido) { mapView ->
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
        if (!origenEsPosicionActual || !permitido) {
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
