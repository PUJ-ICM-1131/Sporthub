package com.sportec.sporthub.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object Notificaciones {

    private val _notificaciones = MutableStateFlow(DatosMock.notificaciones)
    val notificaciones: StateFlow<List<Notificacion>> = _notificaciones.asStateFlow()

    fun evento(usuarioId: String?, mensaje: Int, ruta: String? = null, argumento: String? = null) {
        if (usuarioId == null) return
        publicar(usuarioId, com.sportec.sporthub.utils.Textos.obtener(com.sportec.sporthub.R.string.app_name),
            com.sportec.sporthub.utils.Textos.obtener(mensaje), ruta, argumento)
    }

    fun publicar(usuarioId: String, titulo: String, texto: String, ruta: String? = null, argumento: String? = null) {
        val nueva = Notificacion(java.util.UUID.randomUUID().toString(), usuarioId, titulo, texto, ruta, argumento)
        _notificaciones.value = listOf(nueva) + _notificaciones.value
    }

    fun retirar(ruta: String, argumento: String, usuarioId: String) {
        _notificaciones.value = _notificaciones.value.filterNot { it.usuarioId == usuarioId && it.ruta == ruta && it.argumento == argumento }
    }

    fun marcarLeida(id: String) {
        _notificaciones.value = _notificaciones.value.map { if (it.id == id) it.copy(leida = true) else it }
    }
}
