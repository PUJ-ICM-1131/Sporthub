package com.sportec.sporthub.ui.screens.comun

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos
import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sportec.sporthub.domain.Cuentas
import com.sportec.sporthub.domain.Rol
import com.sportec.sporthub.ui.components.EstadoVacio
import com.sportec.sporthub.ui.components.PantallaBase

@Composable
fun ContactosScreen(
    usuarioId: String,
    onBack: () -> Unit,
    onAbrirChat: (String) -> Unit
) {
    val cuentas by Cuentas.cuentas.collectAsState()
    val contactos = cuentas.filter { it.id != usuarioId && it.rol == Rol.DEPORTISTA }

    PantallaBase(titulo = Textos.obtener(R.string.contactos_titulo), onBack = onBack) { padding ->
        if (contactos.isEmpty()) {
            EstadoVacio(
                mensaje = stringResource(R.string.contactos_vacio),
                modifier = Modifier.padding(padding)
            )
            return@PantallaBase
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.contactos_detalle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(contactos, key = { it.id }) { contacto ->
                Card(onClick = { onAbrirChat(contacto.id) }, modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text(Cuentas.nombreParaMostrar(contacto)) },
                        supportingContent = { Text(contacto.email) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }
    }
}
