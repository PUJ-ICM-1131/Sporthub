package com.sportec.sporthub.ui.screens.comun

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sportec.sporthub.domain.Chat
import com.sportec.sporthub.domain.DatosMock
import com.sportec.sporthub.domain.Mensajes
import com.sportec.sporthub.ui.components.EstadoVacio
import com.sportec.sporthub.ui.components.PantallaBase

@Composable
fun MensajesScreen(
    usuarioId: String,
    onAbrirChat: (String) -> Unit
) {
    val chats by Mensajes.chats.collectAsState()
    val propios = chats.filter { it.miembros.contains(usuarioId) }

    PantallaBase(titulo = "Mensajes") { padding ->
        if (propios.isEmpty()) {
            EstadoVacio(mensaje = "Todavía no tienes conversaciones.", modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(propios, key = { it.id }) { chat ->
                    ItemChat(chat, usuarioId) { onAbrirChat(chat.id) }
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun ItemChat(chat: Chat, usuarioId: String, onClick: () -> Unit) {
    val otroId = chat.miembros.firstOrNull { it != usuarioId }
    val nombre = DatosMock.cuentas.firstOrNull { it.id == otroId }?.nombre ?: "Conversación"
    val ultimo = chat.mensajes.lastOrNull()?.texto ?: "Sin mensajes todavía"
    ListItem(
        headlineContent = { Text(nombre) },
        supportingContent = { Text(ultimo, maxLines = 1) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable(onClick = onClick)
    )
}
