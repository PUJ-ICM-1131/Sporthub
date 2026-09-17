package com.sportec.sporthub.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Chats 1:1, incluida la atención de un establecimiento a sus deportistas (HU-D18, HU-D19, HU-E11). */
object Mensajes {

    private var secuencia = 600
    private fun uid(prefijo: String) = prefijo + (++secuencia)

    private val _chats = MutableStateFlow(DatosMock.chats)
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    fun chatDe(id: String): Chat? = _chats.value.firstOrNull { it.id == id }

    fun listarDe(usuarioId: String): List<Chat> = _chats.value.filter { it.miembros.contains(usuarioId) }

    fun chatConNegocio(usuarioId: String, negocioId: String): Chat {
        val operadorId = Negocios.operadorDe(negocioId)
            ?: throw IllegalArgumentException("El establecimiento no tiene cuenta activa.")
        val existente = _chats.value.firstOrNull { it.miembros.toSet() == setOf(usuarioId, operadorId) }
        if (existente != null) return existente
        val nuevo = Chat(uid("c"), miembros = listOf(usuarioId, operadorId), mensajes = emptyList())
        _chats.value = _chats.value + nuevo
        return nuevo
    }

    fun enviar(chatId: String, autorId: String, texto: String): Chat {
        if (texto.isBlank()) throw IllegalArgumentException("El mensaje no puede estar vacío.")
        val chat = chatDe(chatId) ?: throw IllegalArgumentException("La conversación ya no existe.")
        if (!chat.miembros.contains(autorId)) throw IllegalStateException("No tienes acceso a esta conversación.")
        val actualizado = chat.copy(mensajes = chat.mensajes + Mensaje(uid("m"), autorId, texto))
        _chats.value = _chats.value.map { if (it.id == chatId) actualizado else it }
        return actualizado
    }
}
