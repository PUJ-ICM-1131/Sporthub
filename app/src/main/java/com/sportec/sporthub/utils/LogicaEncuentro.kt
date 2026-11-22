package com.sportec.sporthub.utils

import com.sportec.sporthub.data.Encuentro
import com.sportec.sporthub.data.OrigenEncuentro
import com.sportec.sporthub.data.PosicionCompartida
import com.sportec.sporthub.domain.PosicionActual

data class Publicacion(val posicion: Coordenada, val instante: Long)

enum class EstadoPosicion { EN_VIVO, DESACTUALIZADA, VENCIDA }

object LogicaEncuentro {

    const val EDAD_MAXIMA_LECTURA_MS = 120_000L

    const val RENOVACION_MS = 15_000L

    const val DESPLAZAMIENTO_MINIMO_M = 10.0

    const val INTERVALO_MINIMO_MS = 3_000L

    fun tipoOrigen(esComunidad: Boolean): OrigenEncuentro =
        if (esComunidad) OrigenEncuentro.COMUNIDAD else OrigenEncuentro.CHAT

    fun lecturaUtil(posicion: PosicionActual?, ahora: Long, ubicacionActiva: Boolean): Boolean =
        ubicacionActiva && posicion != null && posicion.instante <= ahora &&
            ahora - posicion.instante <= EDAD_MAXIMA_LECTURA_MS

    fun debePublicar(
        ultima: Publicacion?,
        posicion: PosicionActual?,
        ahora: Long,
        ubicacionActiva: Boolean
    ): Boolean {
        if (!lecturaUtil(posicion, ahora, ubicacionActiva)) return false
        if (ultima == null) return true
        val transcurrido = ahora - ultima.instante
        if (transcurrido < INTERVALO_MINIMO_MS) return false
        if (transcurrido >= RENOVACION_MS) return true
        val actual = Coordenada(posicion!!.latitud, posicion.longitud)
        return Geo.distanciaMetros(ultima.posicion, actual) >= DESPLAZAMIENTO_MINIMO_M
    }

    fun estado(posicion: PosicionCompartida, ahora: Long): EstadoPosicion = when {
        posicion.enVivo(ahora) -> EstadoPosicion.EN_VIVO
        posicion.vigente(ahora) -> EstadoPosicion.DESACTUALIZADA
        else -> EstadoPosicion.VENCIDA
    }

    fun visibles(
        posiciones: List<PosicionCompartida>,
        participantes: Collection<String>,
        usuarioId: String,
        ahora: Long
    ): List<PosicionCompartida> = posiciones.filter {
        it.uid != usuarioId && it.uid in participantes && estado(it, ahora) != EstadoPosicion.VENCIDA
    }

    fun puedeFinalizar(encuentro: Encuentro, usuarioId: String, adminComunidad: String?): Boolean =
        encuentro.creadorId == usuarioId ||
            (encuentro.origenTipo == OrigenEncuentro.COMUNIDAD && adminComunidad == usuarioId)

    fun antiguedadSegundos(posicion: PosicionCompartida, ahora: Long): Long =
        ((ahora - posicion.actualizadaEn) / 1000).coerceAtLeast(0)
}
