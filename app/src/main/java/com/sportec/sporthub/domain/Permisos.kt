package com.sportec.sporthub.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object Permisos {

    const val UBICACION = "ubicacion"
    const val CAMARA = "camara"
    const val CONTACTOS = "contactos"
    const val NOTIFICACIONES = "notificaciones"

    private val _concedidos = MutableStateFlow<Set<String>>(emptySet())
    val concedidos: StateFlow<Set<String>> = _concedidos.asStateFlow()

    fun concedido(tipo: String): Boolean = tipo in _concedidos.value

    fun conceder(tipo: String) {
        _concedidos.value = _concedidos.value + tipo
    }

    fun rechazar(tipo: String) {
        _concedidos.value = _concedidos.value - tipo
    }
}
