package com.sportec.sporthub.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object Notificaciones {

    private val _notificaciones = MutableStateFlow(DatosMock.notificaciones)
    val notificaciones: StateFlow<List<Notificacion>> = _notificaciones.asStateFlow()

    fun marcarLeida(id: String) {
        _notificaciones.value = _notificaciones.value.map { if (it.id == id) it.copy(leida = true) else it }
    }
}
