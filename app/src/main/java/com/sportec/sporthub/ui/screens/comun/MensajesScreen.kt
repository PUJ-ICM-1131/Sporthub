package com.sportec.sporthub.ui.screens.comun

import com.sportec.sporthub.ui.components.AvatarChat
import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.sportec.sporthub.domain.Comunidades
import com.sportec.sporthub.domain.Cuentas
import com.sportec.sporthub.domain.Mensajes
import com.sportec.sporthub.ui.components.EstadoVacio
import com.sportec.sporthub.ui.components.PantallaBase

@Composable
fun MensajesScreen(
    usuarioId: String,
    onAbrirChat: (String) -> Unit,
    onCrearComunidad: () -> Unit
) {
    val chats by Mensajes.chats.collectAsState()
    val grupos by Comunidades.comunidades.collectAsState()
    val propios = chats.filter { chat ->
        chat.comunidadId?.let { id -> grupos.any { it.id == id && usuarioId in it.miembros } } ?: (usuarioId in chat.miembros)
    }

    PantallaBase(
        titulo = Textos.obtener(R.string.texto_mensajes),
        acciones = {
            IconButton(onClick = onCrearComunidad) {
                Icon(Icons.Filled.GroupAdd, contentDescription = Textos.obtener(R.string.texto_crear_comunidad))
            }
        }
    ) { padding ->
        if (propios.isEmpty()) {
            EstadoVacio(mensaje = Textos.obtener(R.string.texto_todavia_no_tienes_conversaciones), modifier = Modifier.padding(padding))
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
    val cuentas by Cuentas.cuentas.collectAsState()
    val comunidades by Comunidades.comunidades.collectAsState()
    val grupo = comunidades.firstOrNull { it.id == chat.comunidadId }
    val persona = cuentas.firstOrNull { it.id == chat.miembros.firstOrNull { id -> id != usuarioId } }
    val nombre = if (chat.comunidadId != null) {
        grupo?.nombre ?: Textos.obtener(R.string.texto_comunidad)
    } else {
        persona?.let { Cuentas.nombreParaMostrar(it) } ?: Textos.obtener(R.string.texto_conversacion)
    }
    val ultimo = chat.mensajes.lastOrNull()?.texto ?: Textos.obtener(R.string.texto_sin_mensajes_todavia)
    ListItem(
        leadingContent = { AvatarChat(if (chat.comunidadId != null) grupo?.fotoUri else persona?.fotoUri, nombre, chat.comunidadId != null) },
        headlineContent = { Text(nombre) },
        supportingContent = { Text(ultimo, maxLines = 1) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable(onClick = onClick)
    )
}
