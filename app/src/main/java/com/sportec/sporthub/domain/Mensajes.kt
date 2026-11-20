package com.sportec.sporthub.domain

import com.sportec.sporthub.R
import com.sportec.sporthub.data.Escritura
import com.sportec.sporthub.data.Mapeos
import com.sportec.sporthub.data.Nube
import com.sportec.sporthub.utils.Textos

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Chats en `chats/{id}` y mensajes en `chats/{id}/mensajes/{id}`.
 * El chat entre dos personas tiene un identificador determinista (uids ordenados), de modo que
 * no se duplica aunque ambas lo abran a la vez. El chat de una comunidad usa el id de la comunidad.
 */
object Mensajes {

    private fun uid(prefijo: String) = prefijo + java.util.UUID.randomUUID().toString()

    private var metadatos: List<Chat> = emptyList()
    private var mensajesPorChat: Map<String, List<Mensaje>> = emptyMap()

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private fun recomponer() {
        _chats.value = metadatos.map { it.copy(mensajes = mensajesPorChat[it.id].orEmpty()) }
    }

    internal fun cargarChats(lista: List<Chat>) {
        // Conserva los chats creados localmente que el servidor todavía no confirmó.
        val pendientes = metadatos.filter { local -> lista.none { it.id == local.id } && local.id in creadosLocalmente }
        metadatos = lista + pendientes
        recomponer()
    }

    internal fun cargarMensajes(chatId: String, lista: List<Mensaje>) {
        mensajesPorChat = mensajesPorChat + (chatId to lista)
        creadosLocalmente.remove(chatId)
        recomponer()
    }

    internal fun vaciar() {
        metadatos = emptyList(); mensajesPorChat = emptyMap(); creadosLocalmente.clear(); recomponer()
    }

    private val creadosLocalmente = mutableSetOf<String>()

    fun chatDe(id: String): Chat? = _chats.value.firstOrNull { it.id == id }

    fun esMiembro(chat: Chat, usuarioId: String): Boolean =
        chat.comunidadId?.let { Comunidades.esMiembro(it, usuarioId) } ?: chat.miembros.contains(usuarioId)

    fun listarDe(usuarioId: String): List<Chat> = _chats.value.filter { esMiembro(it, usuarioId) }

    private fun agregarLocal(chat: Chat) {
        metadatos = metadatos + chat
        creadosLocalmente.add(chat.id)
        recomponer()
    }

    fun idEntre(a: String, b: String): String = listOf(a, b).sorted().joinToString("_")

    /** [persistir] solo es true al crear la comunidad; al aceptar una invitación el chat ya existe en Firestore. */
    fun crearGrupo(comunidadId: String, persistir: Boolean = true): Chat {
        _chats.value.firstOrNull { it.comunidadId == comunidadId }?.let { return it }
        val nuevo = Chat(comunidadId, comunidadId = comunidadId, mensajes = emptyList())
        agregarLocal(nuevo)
        if (persistir) {
            val miembros = Comunidades.comunidadDe(comunidadId)?.miembros.orEmpty()
            Nube.escribir(Escritura.Mezclar("chats/$comunidadId", Mapeos.mapaChat(nuevo, miembros)))
        }
        return nuevo
    }

    fun chatEntre(usuarioId: String, otroId: String): Chat {
        val id = idEntre(usuarioId, otroId)
        chatDe(id)?.let { return it }
        _chats.value.firstOrNull { it.comunidadId == null && it.miembros.toSet() == setOf(usuarioId, otroId) }?.let { return it }
        val nuevo = Chat(id, miembros = listOf(usuarioId, otroId), mensajes = emptyList())
        agregarLocal(nuevo)
        Nube.escribir(Escritura.Mezclar("chats/$id", Mapeos.mapaChat(nuevo, nuevo.miembros)))
        return nuevo
    }

    fun chatConNegocio(usuarioId: String, negocioId: String): Chat {
        val operadorId = Negocios.operadorDe(negocioId)
            ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_el_establecimiento_no_tiene_cuenta_activa))
        return chatEntre(usuarioId, operadorId)
    }

    fun enviar(chatId: String, autorId: String, texto: String): Chat = Nube.agrupar {
        if (texto.isBlank()) throw IllegalArgumentException(Textos.obtener(R.string.texto_el_mensaje_no_puede_estar_vacio))
        val chat = chatDe(chatId) ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_la_conversacion_ya_no_existe))
        if (!esMiembro(chat, autorId)) throw IllegalStateException(Textos.obtener(R.string.texto_no_tienes_acceso_a_esta_conversacion))
        val mensaje = Mensaje(uid("m"), autorId, texto)
        mensajesPorChat = mensajesPorChat + (chatId to (mensajesPorChat[chatId].orEmpty() + mensaje))
        recomponer()
        Nube.escribir(Escritura.Crear("chats/$chatId/mensajes/${mensaje.id}", Mapeos.mapa(mensaje)))
        val destinatarios = chat.comunidadId?.let { Comunidades.comunidadDe(it)?.miembros } ?: chat.miembros
        destinatarios.orEmpty().filter { it != autorId }.forEach {
            Notificaciones.evento(it, R.string.aviso_mensaje_nuevo, "chat", chatId)
        }
        chatDe(chatId)!!
    }
}
