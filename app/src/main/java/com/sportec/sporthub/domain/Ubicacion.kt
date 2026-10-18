package com.sportec.sporthub.domain

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos

import kotlin.math.cos
import kotlin.math.sqrt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Origen(val nombre: String, val latitud: Double, val longitud: Double)

data class PosicionActual(
    val latitud: Double,
    val longitud: Double,
    val instante: Long,
    val precisionMetros: Float? = null
) {
    fun esFiable(ahora: Long): Boolean =
        latitud.isFinite() && longitud.isFinite() &&
            latitud in -90.0..90.0 && longitud in -180.0..180.0 &&
            instante > 0 && instante <= ahora && ahora - instante <= 30000L &&
            precisionMetros != null && precisionMetros.isFinite() &&
            precisionMetros > 0f && precisionMetros <= 50f
}

object Ubicacion {
    private val _posicionActual = MutableStateFlow<PosicionActual?>(null)
    val posicionActual: StateFlow<PosicionActual?> = _posicionActual.asStateFlow()

    fun actualizarPosicion(latitud: Double, longitud: Double, instante: Long = System.currentTimeMillis(), precisionMetros: Float? = null) {
        if (latitud.isFinite() && longitud.isFinite() && latitud in -90.0..90.0 && longitud in -180.0..180.0) {
            if (instante > System.currentTimeMillis() || instante <= 0L) return
            val anterior = _posicionActual.value
            if (anterior != null && instante < anterior.instante) return
            _posicionActual.value = PosicionActual(latitud, longitud, instante, precisionMetros)
        }
    }

    fun borrarPosicion() {
        _posicionActual.value = null
    }


    val PRESETS = listOf(
        Origen(Textos.obtener(R.string.texto_centro_de_bogota), 4.6486, -74.0813),
        Origen(Textos.obtener(R.string.texto_chapinero), 4.6486, -74.0631),
        Origen(Textos.obtener(R.string.texto_usaquen), 4.6947, -74.0303),
        Origen(Textos.obtener(R.string.texto_kennedy), 4.6280, -74.1571),
        Origen(Textos.obtener(R.string.texto_suba), 4.7420, -74.0930)
    )

    private val _origen = MutableStateFlow(PRESETS.first())
    val origen: StateFlow<Origen> = _origen.asStateFlow()

    fun elegir(origen: Origen) {
        _origen.value = origen
    }

    fun distanciaKm(origen: Origen, lat: Double, lng: Double): Double {
        val latMedRad = Math.toRadians((origen.latitud + lat) / 2)
        val dx = (lng - origen.longitud) * cos(latMedRad)
        val dy = lat - origen.latitud
        return sqrt(dx * dx + dy * dy) * 111.32
    }

    fun rumboGrados(origen: Origen, lat: Double, lng: Double): Double {
        val latMedRad = Math.toRadians((origen.latitud + lat) / 2)
        val dx = (lng - origen.longitud) * cos(latMedRad)
        val dy = lat - origen.latitud
        val grados = Math.toDegrees(Math.atan2(dx, dy))
        return (grados + 360) % 360
    }
}
