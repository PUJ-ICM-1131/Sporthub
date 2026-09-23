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
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sportec.sporthub.domain.Comunidades
import com.sportec.sporthub.domain.Cuentas
import com.sportec.sporthub.domain.Rol
import com.sportec.sporthub.ui.components.Aviso
import com.sportec.sporthub.ui.components.EstadoVacio
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.TipoAviso

@Composable
fun InvitarScreen(
    comunidadId: String,
    usuarioId: String,
    onBack: () -> Unit
) {
    val comunidades by Comunidades.comunidades.collectAsState()
    val cuentas by Cuentas.cuentas.collectAsState()
    val comunidad = comunidades.firstOrNull { it.id == comunidadId }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    PantallaBase(titulo = Textos.obtener(R.string.invitar_titulo), onBack = onBack) { padding ->
        if (comunidad == null) {
            EstadoVacio(
                mensaje = stringResource(R.string.comunidad_no_encontrada),
                modifier = Modifier.padding(padding)
            )
            return@PantallaBase
        }
        val candidatos = cuentas.filter { it.rol == Rol.DEPORTISTA && it.id !in comunidad.miembros }
        if (candidatos.isEmpty()) {
            EstadoVacio(
                mensaje = stringResource(R.string.invitar_vacio),
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
                    text = stringResource(R.string.invitar_detalle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (error != null) {
                item { Aviso(texto = error ?: "", tipo = TipoAviso.ERROR) }
            }
            items(candidatos, key = { it.id }) { candidato ->
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text(Cuentas.nombreParaMostrar(candidato)) },
                        supportingContent = { Text(candidato.email) },
                        trailingContent = {
                            TextButton(
                                onClick = {
                                    try {
                                        Comunidades.invitar(comunidadId, candidato.id, usuarioId)
                                        error = null
                                    } catch (e: Exception) {
                                        error = e.message
                                    }
                                }
                            ) {
                                Text(stringResource(R.string.invitar_accion))
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }
    }
}
