package com.sportec.sporthub.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.sportec.sporthub.R
import com.sportec.sporthub.domain.Notificaciones

@Composable
fun CampanaNotificaciones(usuarioId: String?, onClick: () -> Unit) {
    val avisos by Notificaciones.notificaciones.collectAsState()
    val pendientes = avisos.any { it.usuarioId == usuarioId && !it.leida }
    IconButton(onClick = onClick) {
        BadgedBox(badge = { if (pendientes) Badge() }) {
            Icon(Icons.Filled.Notifications, contentDescription = stringResource(
                if (pendientes) R.string.notificaciones_sin_leer else R.string.texto_notificaciones
            ))
        }
    }
}
