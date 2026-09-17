package com.sportec.sporthub.ui.screens.comun

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.sportec.sporthub.domain.Notificaciones
import com.sportec.sporthub.domain.Notificacion
import com.sportec.sporthub.navigation.DetalleReserva
import com.sportec.sporthub.navigation.DetalleSolicitud
import com.sportec.sporthub.navigation.DetalleTransferencia
import com.sportec.sporthub.ui.components.EstadoVacio
import com.sportec.sporthub.ui.components.PantallaBase

@Composable
fun NotificacionesScreen(
    usuarioId: String,
    onBack: () -> Unit,
    onNavegar: (NavKey) -> Unit
) {
    val notificaciones by Notificaciones.notificaciones.collectAsState()
    val propias = notificaciones.filter { it.usuarioId == usuarioId }

    PantallaBase(titulo = "Notificaciones", onBack = onBack) { padding ->
        if (propias.isEmpty()) {
            EstadoVacio(mensaje = "Por ahora estás al día.", modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(propias, key = { it.id }) { notificacion ->
                    ItemNotificacion(notificacion) {
                        Notificaciones.marcarLeida(notificacion.id)
                        val destino = when (notificacion.ruta) {
                            "request" -> notificacion.argumento?.let { DetalleSolicitud(it) }
                            "reservation" -> notificacion.argumento?.let { DetalleReserva(it) }
                            "transfer" -> notificacion.argumento?.let { DetalleTransferencia(it) }
                            else -> null
                        }
                        destino?.let(onNavegar)
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun ItemNotificacion(notificacion: Notificacion, onClick: () -> Unit) {
    ListItem(
        headlineContent = {
            Text(
                text = notificacion.titulo,
                fontWeight = if (notificacion.leida) FontWeight.Normal else FontWeight.SemiBold
            )
        },
        supportingContent = { Text(notificacion.texto) },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        color = if (notificacion.leida) Color.Transparent else MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable(onClick = onClick)
    )
}
