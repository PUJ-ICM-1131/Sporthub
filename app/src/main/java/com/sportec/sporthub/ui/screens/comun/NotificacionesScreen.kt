package com.sportec.sporthub.ui.screens.comun

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos

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
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.TextButton
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import com.sportec.sporthub.domain.Comunidades
import com.sportec.sporthub.navigation.Comunidad
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
import com.sportec.sporthub.ui.components.PermisoNotificaciones

@Composable
fun NotificacionesScreen(
    usuarioId: String,
    onBack: () -> Unit,
    onNavegar: (NavKey) -> Unit
) {
    val notificaciones by Notificaciones.notificaciones.collectAsState()
    val invitaciones by Comunidades.invitaciones.collectAsState()
    val propias = notificaciones.filter { it.usuarioId == usuarioId }
    var error by remember { mutableStateOf<String?>(null) }
    PantallaBase(titulo = Textos.obtener(R.string.texto_notificaciones), onBack = onBack) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp)) {
            item {
                Text(stringResource(R.string.avisos_explicacion))
            }
            item { PermisoNotificaciones() }
            if (error != null) item { Text(error.orEmpty(), color = MaterialTheme.colorScheme.error) }
            if (propias.isEmpty()) item { Text(stringResource(R.string.texto_por_ahora_estas_al_dia)) }
            items(propias, key = { it.id }) { aviso ->
                ItemNotificacion(aviso) {
                    Notificaciones.marcarLeida(aviso.id)
                    val destino = when (aviso.ruta) {
                        "request" -> aviso.argumento?.let { DetalleSolicitud(it) }
                        "reservation" -> aviso.argumento?.let { DetalleReserva(it) }
                        "transfer" -> aviso.argumento?.let { DetalleTransferencia(it) }
                        else -> null
                    }
                    destino?.let(onNavegar)
                }
                val invitacion = invitaciones.firstOrNull { it.id == aviso.argumento && it.puedeResponder(usuarioId) }
                if (aviso.ruta == "invitation" && invitacion != null) {
                    Row {
                        listOf(true, false).forEach { aceptar ->
                            TextButton(onClick = {
                                try {
                                    Comunidades.responderInvitacion(invitacion.id, usuarioId, aceptar)
                                    error = null
                                    if (aceptar) onNavegar(Comunidad(invitacion.comunidadId))
                                } catch (e: IllegalArgumentException) { error = e.message }
                            }) { Text(stringResource(if (aceptar) R.string.invitacion_aceptar else R.string.invitacion_rechazar)) }
                        }
                    }
                }
                HorizontalDivider()
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
