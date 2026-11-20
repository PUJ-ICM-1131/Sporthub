package com.sportec.sporthub.domain

import com.sportec.sporthub.data.Escritura
import com.sportec.sporthub.data.Mapeos
import com.sportec.sporthub.data.Nube
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Avisos de cada cuenta. Se guardan en `notificaciones/{id}` con el destinatario en `usuarioId`;
 * al emitirse dentro de [Nube.agrupar] se confirman en el mismo lote que el hecho que los origina.
 */
object Notificaciones {

    private val _notificaciones = MutableStateFlow<List<Notificacion>>(emptyList())
    val notificaciones: StateFlow<List<Notificacion>> = _notificaciones.asStateFlow()

    internal fun cargar(lista: List<Pair<Notificacion, Long>>) {
        _notificaciones.value = lista.sortedByDescending { it.second }.map { it.first }
    }

    internal fun vaciar() {
        _notificaciones.value = emptyList()
    }

    fun evento(usuarioId: String?, mensaje: Int, ruta: String? = null, argumento: String? = null) {
        if (usuarioId == null) return
        publicar(usuarioId, com.sportec.sporthub.utils.Textos.obtener(com.sportec.sporthub.R.string.app_name),
            com.sportec.sporthub.utils.Textos.obtener(mensaje), ruta, argumento)
    }

    fun publicar(usuarioId: String, titulo: String, texto: String, ruta: String? = null, argumento: String? = null) {
        val nueva = Notificacion(java.util.UUID.randomUUID().toString(), usuarioId, titulo, texto, ruta, argumento)
        _notificaciones.value = listOf(nueva) + _notificaciones.value
        Nube.uid()?.let { autor ->
            Nube.escribir(Escritura.Crear("notificaciones/${nueva.id}", Mapeos.mapa(nueva, autor)))
        }
    }

    fun retirar(ruta: String, argumento: String, usuarioId: String) {
        val retiradas = _notificaciones.value.filter { it.usuarioId == usuarioId && it.ruta == ruta && it.argumento == argumento }
        _notificaciones.value = _notificaciones.value - retiradas.toSet()
        retiradas.forEach { Nube.escribir(Escritura.Borrar("notificaciones/${it.id}")) }
    }

    fun marcarLeida(id: String) {
        _notificaciones.value = _notificaciones.value.map { if (it.id == id) it.copy(leida = true) else it }
        Nube.escribir(Escritura.Actualizar("notificaciones/$id", mapOf("leida" to true)))
    }
}
