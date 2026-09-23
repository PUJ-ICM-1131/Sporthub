package com.sportec.sporthub.domain

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object Mensajes {

    private var secuencia = 600
    private fun uid(prefijo: String) = prefijo + (++secuencia)

    private val _chats = MutableStateFlow(DatosMock.chats)
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    fun chatDe(id: String): Chat? = _chats.value.firstOrNull { it.id == id }

    fun esMiembro(chat: Chat, usuarioId: String): Boolean =
        chat.comunidadId?.let { Comunidades.esMiembro(it, usuarioId) } ?: chat.miembros.contains(usuarioId)

    fun listarDe(usuarioId: String): List<Chat> = _chats.value.filter { esMiembro(it, usuarioId) }

    fun crearGrupo(comunidadId: String): Chat {
        val nuevo = Chat(comunidadId, comunidadId = comunidadId, mensajes = emptyList())
        _chats.value = _chats.value + nuevo
        return nuevo
    }

    fun chatEntre(usuarioId: String, otroId: String): Chat {
        val existente = _chats.value.firstOrNull { it.miembros.toSet() == setOf(usuarioId, otroId) }
        if (existente != null) return existente
        val nuevo = Chat(uid("c"), miembros = listOf(usuarioId, otroId), mensajes = emptyList())
        _chats.value = _chats.value + nuevo
        return nuevo
    }

    fun chatConNegocio(usuarioId: String, negocioId: String): Chat {
        val operadorId = Negocios.operadorDe(negocioId)
            ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_el_establecimiento_no_tiene_cuenta_activa))
        val existente = _chats.value.firstOrNull { it.miembros.toSet() == setOf(usuarioId, operadorId) }
        if (existente != null) return existente
        val nuevo = Chat(uid("c"), miembros = listOf(usuarioId, operadorId), mensajes = emptyList())
        _chats.value = _chats.value + nuevo
        return nuevo
    }

    fun enviar(chatId: String, autorId: String, texto: String): Chat {
        if (texto.isBlank()) throw IllegalArgumentException(Textos.obtener(R.string.texto_el_mensaje_no_puede_estar_vacio))
        val chat = chatDe(chatId) ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_la_conversacion_ya_no_existe))
        if (!esMiembro(chat, autorId)) throw IllegalStateException(Textos.obtener(R.string.texto_no_tienes_acceso_a_esta_conversacion))
        val actualizado = chat.copy(mensajes = chat.mensajes + Mensaje(uid("m"), autorId, texto))
        _chats.value = _chats.value.map { if (it.id == chatId) actualizado else it }
        return actualizado
    }
}
