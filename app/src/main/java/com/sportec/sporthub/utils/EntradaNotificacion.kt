package com.sportec.sporthub.utils

import kotlinx.coroutines.flow.MutableStateFlow

object EntradaNotificacion {
    const val USUARIO = "usuario_notificacion"
    val destinatario = MutableStateFlow<String?>(null)
}
