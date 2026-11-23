package com.sportec.sporthub.ui.screens.comun

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos
import androidx.compose.ui.res.stringResource

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.sportec.sporthub.domain.Comunidades
import com.sportec.sporthub.domain.Cuentas
import com.sportec.sporthub.domain.Rol
import com.sportec.sporthub.ui.components.Aviso
import com.sportec.sporthub.ui.components.EstadoVacio
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.TipoAviso
import com.sportec.sporthub.utils.Agenda
import com.sportec.sporthub.utils.CoincidenciaContactos
import com.sportec.sporthub.utils.CuentaContactable
import kotlinx.coroutines.launch
import com.sportec.sporthub.domain.EstadoInvitacion

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun InvitarScreen(
    comunidadId: String,
    usuarioId: String,
    onBack: () -> Unit
) {
    val invitaciones by Comunidades.invitaciones.collectAsState()
    val comunidades by Comunidades.comunidades.collectAsState()
    val cuentas by Cuentas.cuentas.collectAsState()
    val comunidad = comunidades.firstOrNull { it.id == comunidadId }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val alcance = rememberCoroutineScope()

    var coincidencias by remember { mutableStateOf<Map<String, String>?>(null) }
    var buscando by remember { mutableStateOf(false) }
    var soloContactos by rememberSaveable { mutableStateOf(false) }
    var avisoContactos by remember { mutableStateOf<String?>(null) }

    fun revisarAgenda() {
        if (buscando) return
        buscando = true
        avisoContactos = null
        alcance.launch {
            try {
                val agenda = Agenda.leer(context)
                coincidencias = CoincidenciaContactos.emparejar(
                    Cuentas.todas().map { CuentaContactable(it.id, it.email, it.telefono) },
                    agenda
                )
            } catch (e: Exception) {
                avisoContactos = Textos.obtener(R.string.invitar_contactos_error)
            } finally {
                buscando = false
            }
        }
    }

    val permisoContactos = rememberPermissionState(Manifest.permission.READ_CONTACTS) { concedido ->
        if (concedido) revisarAgenda() else avisoContactos = Textos.obtener(R.string.invitar_contactos_permiso)
    }

    fun buscarEntreContactos() {
        if (permisoContactos.status.isGranted) revisarAgenda() else permisoContactos.launchPermissionRequest()
    }

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
        val enAgenda = coincidencias.orEmpty()
        val filtrados = if (soloContactos) candidatos.filter { it.id in enAgenda } else candidatos

        val ordenados = filtrados.sortedWith(
            compareBy({ it.id !in enAgenda }, { enAgenda[it.id] ?: Cuentas.nombreParaMostrar(it) })
        )

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
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = { buscarEntreContactos() }, enabled = !buscando) {
                        if (buscando) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(stringResource(R.string.invitar_buscar_contactos))
                    }
                }
            }
            if (coincidencias == null) {
                item {
                    Text(
                        text = stringResource(R.string.invitar_contactos_explicacion),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            avisoContactos?.let { item { Aviso(texto = it, tipo = TipoAviso.ADVERTENCIA) } }
            coincidencias?.let { encontradas ->
                val cuantas = candidatos.count { it.id in encontradas }
                if (cuantas == 0) {
                    item { Aviso(texto = stringResource(R.string.invitar_contactos_sin_coincidencias), tipo = TipoAviso.NORMAL) }
                } else {
                    item {
                        Text(
                            text = stringResource(R.string.invitar_contactos_coinciden, cuantas),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    item {
                        FilterChip(
                            selected = soloContactos,
                            onClick = { soloContactos = !soloContactos },
                            label = { Text(stringResource(R.string.invitar_solo_contactos)) }
                        )
                    }
                }
            }
            if (error != null) {
                item { Aviso(texto = error ?: "", tipo = TipoAviso.ERROR) }
            }
            items(ordenados, key = { it.id }) { candidato ->
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text(Cuentas.nombreParaMostrar(candidato)) },
                        supportingContent = {
                            val guardado = enAgenda[candidato.id]
                            Text(
                                if (guardado != null) stringResource(R.string.invitar_contacto_guardado, guardado)
                                else candidato.email
                            )
                        },
                        trailingContent = {
                            val pendiente = invitaciones.any { it.comunidadId == comunidadId && it.invitadoId == candidato.id && it.estado == EstadoInvitacion.PENDIENTE }
                            TextButton(
                                enabled = !pendiente,
                                onClick = {
                                    try {
                                        Comunidades.invitar(comunidadId, candidato.id, usuarioId)
                                        error = null
                                    } catch (e: Exception) {
                                        error = e.message
                                    }
                                }
                            ) {
                                Text(stringResource(if (pendiente) R.string.invitacion_pendiente else R.string.invitar_accion))
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }
    }
}
