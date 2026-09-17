package com.sportec.sporthub.domain

import kotlin.math.cos
import kotlin.math.sqrt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Origen(val nombre: String, val latitud: Double, val longitud: Double)

/** Origen de búsqueda elegido por el usuario, usado por el mapa y la brújula (HU-D02, HU-D05, HU-D27, HU-D28). */
object Ubicacion {

    val PRESETS = listOf(
        Origen("Centro de Bogotá", 4.6486, -74.0813),
        Origen("Chapinero", 4.6486, -74.0631),
        Origen("Usaquén", 4.6947, -74.0303),
        Origen("Kennedy", 4.6280, -74.1571),
        Origen("Suba", 4.7420, -74.0930)
    )

    private val _origen = MutableStateFlow(PRESETS.first())
    val origen: StateFlow<Origen> = _origen.asStateFlow()

    fun elegir(origen: Origen) {
        _origen.value = origen
    }

    /** Distancia aproximada en km (proyección equirrectangular; suficiente a escala de una ciudad). */
    fun distanciaKm(origen: Origen, lat: Double, lng: Double): Double {
        val latMedRad = Math.toRadians((origen.latitud + lat) / 2)
        val dx = (lng - origen.longitud) * cos(latMedRad)
        val dy = lat - origen.latitud
        return sqrt(dx * dx + dy * dy) * 111.32
    }

    /** Rumbo en grados (0 = norte, 90 = este) desde el origen hacia el punto dado. */
    fun rumboGrados(origen: Origen, lat: Double, lng: Double): Double {
        val latMedRad = Math.toRadians((origen.latitud + lat) / 2)
        val dx = (lng - origen.longitud) * cos(latMedRad)
        val dy = lat - origen.latitud
        val grados = Math.toDegrees(Math.atan2(dx, dy))
        return (grados + 360) % 360
    }
}
