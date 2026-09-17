package com.sportec.sporthub.ui.screens.comun

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sportec.sporthub.domain.DatosMock
import com.sportec.sporthub.domain.Mensaje
import com.sportec.sporthub.domain.Mensajes
import com.sportec.sporthub.ui.components.EstadoError
import com.sportec.sporthub.ui.components.PantallaBase

@Composable
fun ChatScreen(
    chatId: String,
    usuarioId: String,
    onBack: () -> Unit
) {
    val chats by Mensajes.chats.collectAsState()
    val chat = chats.firstOrNull { it.id == chatId }
    var texto by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    val otroId = chat?.miembros?.firstOrNull { it != usuarioId }
    val titulo = DatosMock.cuentas.firstOrNull { it.id == otroId }?.nombre ?: "Conversación"

    PantallaBase(titulo = titulo, onBack = onBack) { padding ->
        if (chat == null) {
            EstadoError(mensaje = "La conversación ya no existe.", onReintentar = {}, modifier = Modifier.padding(padding))
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chat.mensajes, key = { it.id }) { mensaje ->
                        BurbujaMensaje(mensaje, esPropio = mensaje.autorId == usuarioId)
                    }
                }
                if (error != null) {
                    Text(text = error ?: "", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = texto,
                        onValueChange = { texto = it },
                        label = { Text("Mensaje") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        try {
                            Mensajes.enviar(chatId, usuarioId, texto)
                            texto = ""
                            error = null
                        } catch (e: Exception) {
                            error = e.message
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
                    }
                }
            }
        }
    }
}

@Composable
private fun BurbujaMensaje(mensaje: Mensaje, esPropio: Boolean) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (esPropio) Arrangement.End else Arrangement.Start) {
        Surface(
            color = if (esPropio) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = mensaje.texto, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
        }
    }
}
