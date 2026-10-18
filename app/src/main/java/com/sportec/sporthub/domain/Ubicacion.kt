package com.sportec.sporthub.domain

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos

import kotlin.math.cos
import kotlin.math.sqrt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Origen(val nombre: String, val latitud: Double, val longitud: Double, val esPosicionActual: Boolean = false)

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

/**
 * Origen de distancias y rutas: la posición real del dispositivo cuando hay permiso y lectura;
 * si no la hay, el punto que la persona marca en el mapa. Mientras no exista ninguno de los dos,
 * [origenDefinido] es falso y se usa un punto de respaldo solo para ordenar listados.
 */
object Ubicacion {
    private val _posicionActual = MutableStateFlow<PosicionActual?>(null)
    val posicionActual: StateFlow<PosicionActual?> = _posicionActual.asStateFlow()

    private val respaldo = Origen(Textos.obtener(R.string.texto_centro_de_bogota), 4.6486, -74.0813)
    private var marcado: Origen? = null

    private val _origen = MutableStateFlow(respaldo)
    val origen: StateFlow<Origen> = _origen.asStateFlow()

    private val _origenDefinido = MutableStateFlow(false)
    val origenDefinido: StateFlow<Boolean> = _origenDefinido.asStateFlow()

    private val _destino = MutableStateFlow<String?>(null)
    val destino: StateFlow<String?> = _destino.asStateFlow()

    fun actualizarPosicion(latitud: Double, longitud: Double, instante: Long = System.currentTimeMillis(), precisionMetros: Float? = null) {
        if (latitud.isFinite() && longitud.isFinite() && latitud in -90.0..90.0 && longitud in -180.0..180.0) {
            if (instante > System.currentTimeMillis() || instante <= 0L) return
            val anterior = _posicionActual.value
            if (anterior != null && instante < anterior.instante) return
            _posicionActual.value = PosicionActual(latitud, longitud, instante, precisionMetros)
            _origen.value = Origen(Textos.obtener(R.string.brujula_posicion_actual), latitud, longitud, esPosicionActual = true)
            _origenDefinido.value = true
        }
    }

    fun borrarPosicion() {
        _posicionActual.value = null
        _origen.value = marcado ?: respaldo
        _origenDefinido.value = marcado != null
    }

    fun marcarOrigen(latitud: Double, longitud: Double) {
        if (_posicionActual.value != null) return
        val punto = Origen(Textos.obtener(R.string.origen_punto_marcado), latitud, longitud)
        marcado = punto
        _origen.value = punto
        _origenDefinido.value = true
    }

    fun fijarDestino(negocioId: String?) {
        _destino.value = negocioId
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
