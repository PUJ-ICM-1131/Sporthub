package com.sportec.sporthub.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Reseñas de reservas cerradas por el establecimiento (HU-D31, HU-E12). */
object Resenas {

    private var secuencia = 800
    private fun uid(prefijo: String) = prefijo + (++secuencia)

    private val _resenas = MutableStateFlow(DatosMock.resenas)
    val resenas: StateFlow<List<Resena>> = _resenas.asStateFlow()

    fun listarDeNegocio(negocioId: String): List<Resena> {
        val actividadesNegocio = Negocios.actividadesDe(negocioId).map { it.id }.toSet()
        return _resenas.value.filter { Reservas.obtenerReserva(it.reservaId)?.actividadId in actividadesNegocio }
    }

    fun crear(reservaId: String, usuarioId: String, puntaje: Int, comentario: String): Resena {
        val reserva = Reservas.obtenerReserva(reservaId) ?: throw IllegalArgumentException("La reserva ya no existe.")
        if (reserva.usuarioId != usuarioId) throw IllegalStateException("No puedes calificar esta reserva.")
        if (!reserva.cerrada) throw IllegalStateException("La reserva debe estar cerrada por el establecimiento.")
        if (puntaje !in 1..5) throw IllegalArgumentException("La calificación debe estar entre 1 y 5.")
        if (_resenas.value.any { it.reservaId == reservaId }) throw IllegalStateException("Ya calificaste esta reserva.")
        val nueva = Resena(uid("res"), reservaId, usuarioId, puntaje, comentario)
        _resenas.value = _resenas.value + nueva
        return nueva
    }

    fun responder(id: String, respuesta: String, operadorId: String): Resena {
        val resena = _resenas.value.firstOrNull { it.id == id } ?: throw IllegalArgumentException("La reseña ya no existe.")
        val reserva = Reservas.obtenerReserva(resena.reservaId)!!
        val actividad = Negocios.actividadDe(reserva.actividadId)!!
        if (Negocios.operadorDe(actividad.negocioId) != operadorId) throw IllegalStateException("Esta reseña corresponde a otra cuenta de negocio.")
        if (respuesta.isBlank()) throw IllegalArgumentException("La respuesta no puede estar vacía.")
        val actualizada = resena.copy(respuesta = respuesta)
        _resenas.value = _resenas.value.map { if (it.id == id) actualizada else it }
        return actualizada
    }
}
