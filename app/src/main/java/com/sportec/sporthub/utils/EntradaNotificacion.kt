package com.sportec.sporthub.utils

import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow

data class AperturaNotificacion(val usuarioId: String, val avisoId: String?)

object EntradaNotificacion {
    const val USUARIO = "usuario_notificacion"
    const val AVISO = "id_notificacion"
    val pendiente = MutableStateFlow<AperturaNotificacion?>(null)

    fun recibir(intent: Intent) {
        val usuario = intent.getStringExtra(USUARIO)?.takeIf { it.isNotBlank() } ?: return
        pendiente.value = AperturaNotificacion(usuario, intent.getStringExtra(AVISO))
        intent.removeExtra(USUARIO)
        intent.removeExtra(AVISO)
    }
}
