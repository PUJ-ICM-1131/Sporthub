package com.sportec.sporthub.ui.screens.comun

import com.sportec.sporthub.ui.components.AvatarChat
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
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportec.sporthub.domain.Comunidades
import com.sportec.sporthub.domain.Cuentas
import com.sportec.sporthub.ui.components.EstadoVacio
import com.sportec.sporthub.ui.components.PantallaBase

@Composable
fun ComunidadScreen(
    comunidadId: String,
    usuarioId: String,
    onBack: () -> Unit,
    onAbrirChat: () -> Unit,
    onEditar: () -> Unit,
    onInvitar: () -> Unit,
    onCeder: () -> Unit
) {
    val comunidades by Comunidades.comunidades.collectAsState()
    val cuentas by Cuentas.cuentas.collectAsState()
    val comunidad = comunidades.firstOrNull { it.id == comunidadId }

    PantallaBase(titulo = Textos.obtener(R.string.comunidad_titulo), onBack = onBack) { padding ->
        if (comunidad == null || usuarioId !in comunidad.miembros) {
            EstadoVacio(
                mensaje = stringResource(R.string.comunidad_no_encontrada),
                modifier = Modifier.padding(padding)
            )
            return@PantallaBase
        }
        val esAdmin = comunidad.adminId == usuarioId
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { AvatarChat(comunidad.fotoUri, comunidad.nombre, true, 96.dp) }
            item {
                Text(
                    text = comunidad.nombre,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            item {
                Text(
                    text = comunidad.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                OutlinedButton(onClick = onAbrirChat, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.comunidad_abrir_chat))
                }
            }
            item {
                OutlinedButton(onClick = onInvitar, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.comunidad_invitar))
                }
            }
            if (esAdmin) {
                item {
                    OutlinedButton(onClick = onEditar, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.comunidad_editar))
                    }
                }
                item {
                    OutlinedButton(onClick = onCeder, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.comunidad_ceder))
                    }
                }
            }
            item {
                Text(
                    text = stringResource(R.string.comunidad_miembros),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(comunidad.miembros) { miembroId ->
                val cuenta = cuentas.firstOrNull { it.id == miembroId }
                ListItem(
                    leadingContent = { AvatarChat(cuenta?.fotoUri, cuenta?.nombre.orEmpty()) },
                    headlineContent = { Text(cuenta?.let { Cuentas.nombreParaMostrar(it) }.orEmpty()) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }
    }
}
