package com.sportec.sporthub.domain

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
        val reserva = Reservas.obtenerReserva(reservaId) ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_la_reserva_ya_no_existe))
        if (reserva.usuarioId != usuarioId) throw IllegalStateException(Textos.obtener(R.string.texto_no_puedes_calificar_esta_reserva))
        if (!reserva.cerrada) throw IllegalStateException(Textos.obtener(R.string.texto_la_reserva_debe_estar_cerrada_por_el_establecimiento))
        if (puntaje !in 1..5) throw IllegalArgumentException(Textos.obtener(R.string.texto_la_calificacion_debe_estar_entre_1_y_5))
        if (_resenas.value.any { it.reservaId == reservaId }) throw IllegalStateException(Textos.obtener(R.string.texto_ya_calificaste_esta_reserva))
        val nueva = Resena(uid("res"), reservaId, usuarioId, puntaje, comentario)
        _resenas.value = _resenas.value + nueva
        return nueva
    }

    fun responder(id: String, respuesta: String, operadorId: String): Resena {
        val resena = _resenas.value.firstOrNull { it.id == id } ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_la_resena_ya_no_existe))
        val reserva = Reservas.obtenerReserva(resena.reservaId)!!
        val actividad = Negocios.actividadDe(reserva.actividadId)!!
        if (respuesta.isBlank()) throw IllegalArgumentException(Textos.obtener(R.string.texto_la_respuesta_no_puede_estar_vacia))
        val actualizada = resena.copy(respuesta = respuesta)
        _resenas.value = _resenas.value.map { if (it.id == id) actualizada else it }
        return actualizada
    }
}
