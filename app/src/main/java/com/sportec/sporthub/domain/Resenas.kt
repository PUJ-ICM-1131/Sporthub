package com.sportec.sporthub.domain

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos

import com.sportec.sporthub.data.Escritura
import com.sportec.sporthub.data.Mapeos
import com.sportec.sporthub.data.Nube
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object Resenas {

    private val _resenas = MutableStateFlow<List<Resena>>(emptyList())
    val resenas: StateFlow<List<Resena>> = _resenas.asStateFlow()

    private var negocioPorResena: Map<String, String> = emptyMap()

    internal fun cargar(lista: List<Mapeos.ResenaLeida>) {
        negocioPorResena = lista.associate { it.resena.id to it.negocioId }
        _resenas.value = lista.map { it.resena }
    }

    internal fun vaciar() { _resenas.value = emptyList(); negocioPorResena = emptyMap() }

    fun listarDeNegocio(negocioId: String): List<Resena> =
        _resenas.value.filter { negocioPorResena[it.id] == negocioId }

    fun crear(reservaId: String, usuarioId: String, puntaje: Int, comentario: String): Resena {
        val reserva = Reservas.obtenerReserva(reservaId) ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_la_reserva_ya_no_existe))
        if (reserva.usuarioId != usuarioId) throw IllegalStateException(Textos.obtener(R.string.texto_no_puedes_calificar_esta_reserva))
        if (!reserva.cerrada) throw IllegalStateException(Textos.obtener(R.string.texto_la_reserva_debe_estar_cerrada_por_el_establecimiento))
        if (puntaje !in 1..5) throw IllegalArgumentException(Textos.obtener(R.string.texto_la_calificacion_debe_estar_entre_1_y_5))
        if (_resenas.value.any { it.reservaId == reservaId }) throw IllegalStateException(Textos.obtener(R.string.texto_ya_calificaste_esta_reserva))

        val nueva = Resena(reservaId, reservaId, usuarioId, puntaje, comentario)
        val negocioId = Negocios.actividadDe(reserva.actividadId)?.negocioId.orEmpty()
        negocioPorResena = negocioPorResena + (nueva.id to negocioId)
        _resenas.value = _resenas.value + nueva
        Nube.escribir(Escritura.Crear("resenas/${nueva.id}", Mapeos.mapa(nueva, negocioId)))
        return nueva
    }

    fun responder(id: String, respuesta: String, operadorId: String): Resena {
        val resena = _resenas.value.firstOrNull { it.id == id } ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_la_resena_ya_no_existe))
        val reserva = Reservas.obtenerReserva(resena.reservaId)!!
        val actividad = Negocios.actividadDe(reserva.actividadId)!!
        if (respuesta.isBlank()) throw IllegalArgumentException(Textos.obtener(R.string.texto_la_respuesta_no_puede_estar_vacia))
        val actualizada = resena.copy(respuesta = respuesta)
        _resenas.value = _resenas.value.map { if (it.id == id) actualizada else it }
        Nube.escribir(Escritura.Actualizar("resenas/$id", mapOf("respuesta" to respuesta)))
        return actualizada
    }
}
