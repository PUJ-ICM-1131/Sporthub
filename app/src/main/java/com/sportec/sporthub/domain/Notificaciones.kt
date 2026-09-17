package com.sportec.sporthub.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Bandeja de notificaciones compartida por todos los motores de dominio. */
object Notificaciones {

    private var secuencia = 500
    private fun uid() = "n" + (++secuencia)

    private val _notificaciones = MutableStateFlow(DatosMock.notificaciones)
    val notificaciones: StateFlow<List<Notificacion>> = _notificaciones.asStateFlow()

    fun enviar(usuarioId: String?, titulo: String, texto: String, ruta: String? = null, argumento: String? = null) {
        if (usuarioId == null) return
        _notificaciones.value = _notificaciones.value + Notificacion(uid(), usuarioId, titulo, texto, ruta, argumento)
    }

    fun marcarLeida(id: String) {
        _notificaciones.value = _notificaciones.value.map { if (it.id == id) it.copy(leida = true) else it }
    }
}
