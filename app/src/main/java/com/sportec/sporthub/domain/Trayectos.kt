package com.sportec.sporthub.domain

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Coordenada
import com.sportec.sporthub.utils.Geo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ModoViaje(val perfilMapbox: String, val etiqueta: Int, val velocidadKmH: Double) {
    CAMINANDO("walking", R.string.ruta_modo_caminando, 4.8),
    BICICLETA("cycling", R.string.ruta_modo_bicicleta, 15.0),
    CARRO("driving-traffic", R.string.ruta_modo_carro, 22.0)
}

data class Trayecto(
    val negocioId: String,
    val modo: ModoViaje,
    val inicio: Coordenada,
    val puntos: List<Coordenada>,
    val distanciaMetros: Double,
    val duracionSegundos: Double,
    val estimado: Boolean
)

/** Conserva la ruta vigente para que sobreviva a la navegación (por ejemplo, ir a la brújula y volver). */
object Trayectos {
    const val DESVIO_MAXIMO_M = 60.0
    const val LLEGADA_M = 30.0

    private val _actual = MutableStateFlow<Trayecto?>(null)
    val actual: StateFlow<Trayecto?> = _actual.asStateFlow()

    private val _modo = MutableStateFlow(ModoViaje.CAMINANDO)
    val modo: StateFlow<ModoViaje> = _modo.asStateFlow()

    fun elegirModo(modo: ModoViaje) {
        _modo.value = modo
    }

    fun guardar(trayecto: Trayecto) {
        _actual.value = trayecto
    }

    fun olvidar() {
        _actual.value = null
    }

    /**
     * La ruta guardada sirve mientras el destino y el modo coincidan y el origen siga sobre el trazado.
     * Una ruta estimada es una línea recta que no sigue las calles, así que se compara con su punto de partida.
     */
    fun vigente(negocioId: String, modo: ModoViaje, origen: Coordenada, trayecto: Trayecto? = _actual.value): Trayecto? =
        trayecto?.takeIf {
            val desvio = if (it.estimado) Geo.distanciaMetros(origen, it.inicio) else Geo.distanciaAlTrazado(origen, it.puntos)
            it.negocioId == negocioId && it.modo == modo && desvio <= DESVIO_MAXIMO_M
        }

    fun estimado(negocioId: String, modo: ModoViaje, origen: Coordenada, destino: Coordenada): Trayecto {
        val metros = Geo.distanciaMetros(origen, destino)
        return Trayecto(negocioId, modo, origen, listOf(origen, destino), metros, metros / 1000 / modo.velocidadKmH * 3600, estimado = true)
    }
}
