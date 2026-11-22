package com.sportec.sporthub.utils

import com.sportec.sporthub.data.Encuentro
import com.sportec.sporthub.data.OrigenEncuentro
import com.sportec.sporthub.data.PosicionCompartida
import com.sportec.sporthub.domain.PosicionActual

/** Última posición propia enviada al encuentro. */
data class Publicacion(val posicion: Coordenada, val instante: Long)

enum class EstadoPosicion { EN_VIVO, DESACTUALIZADA, VENCIDA }

/**
 * Reglas de la pantalla «Encontrarnos» que no dependen de Android ni de Firebase.
 * Se mantienen aquí para poder probarlas con pruebas unitarias.
 */
object LogicaEncuentro {
    /** Una lectura GPS más antigua que esto no se envía como posición en vivo. */
    const val EDAD_MAXIMA_LECTURA_MS = 120_000L
    /** Aunque la persona no se mueva, se renueva la posición para que siga «en vivo» (vigencia de 60 s). */
    const val RENOVACION_MS = 15_000L
    /** Movimiento que justifica enviar antes de [RENOVACION_MS]. */
    const val DESPLAZAMIENTO_MINIMO_M = 10.0
    /** Separación mínima entre dos envíos, para no saturar Firestore. */
    const val INTERVALO_MINIMO_MS = 3_000L

    /** El encuentro de un chat de comunidad usa la comunidad: así el acceso sigue a sus miembros actuales. */
    fun tipoOrigen(esComunidad: Boolean): OrigenEncuentro =
        if (esComunidad) OrigenEncuentro.COMUNIDAD else OrigenEncuentro.CHAT

    /** La lectura del dispositivo sirve para compartir: es reciente y la ubicación del teléfono está activa. */
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

    /**
     * Posiciones que se muestran: solo participantes actuales distintos de quien mira y aún vigentes.
     * El filtro por participantes es una segunda barrera; la primera son las reglas de Firestore.
     */
    fun visibles(
        posiciones: List<PosicionCompartida>,
        participantes: Collection<String>,
        usuarioId: String,
        ahora: Long
    ): List<PosicionCompartida> = posiciones.filter {
        it.uid != usuarioId && it.uid in participantes && estado(it, ahora) != EstadoPosicion.VENCIDA
    }

    /** Igual que en las reglas: quien abrió el encuentro o el administrador de la comunidad. */
    fun puedeFinalizar(encuentro: Encuentro, usuarioId: String, adminComunidad: String?): Boolean =
        encuentro.creadorId == usuarioId ||
            (encuentro.origenTipo == OrigenEncuentro.COMUNIDAD && adminComunidad == usuarioId)

    /** Segundos transcurridos desde la última actualización, para «hace N s / min». */
    fun antiguedadSegundos(posicion: PosicionCompartida, ahora: Long): Long =
        ((ahora - posicion.actualizadaEn) / 1000).coerceAtLeast(0)
}
