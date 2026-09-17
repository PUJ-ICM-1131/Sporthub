package com.sportec.sporthub.ui.screens.establecimiento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.sportec.sporthub.domain.EstadoReserva
import com.sportec.sporthub.domain.Negocios
import com.sportec.sporthub.domain.Reservas
import com.sportec.sporthub.navigation.Agenda
import com.sportec.sporthub.navigation.DetalleSolicitud
import com.sportec.sporthub.navigation.DetalleTransferencia
import com.sportec.sporthub.ui.components.EstadoVacio
import com.sportec.sporthub.ui.components.Etiqueta
import com.sportec.sporthub.ui.components.TarjetaTint
import com.sportec.sporthub.ui.components.tono
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.utils.formatoIntervalo
import com.sportec.sporthub.utils.formatoPesos

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionScreen(
    negocioId: String,
    onNavegar: (NavKey) -> Unit,
    viewModel: GestionViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()
    val reservas by Reservas.reservas.collectAsState()
    val serviciosNegocio = remember(negocioId) { Negocios.actividadesDe(negocioId) }
    val aprobadas = remember(reservas, negocioId) {
        Reservas.listarReservasNegocio(negocioId).filter { it.estado == EstadoReserva.CONFIRMADA }
    }
    var servicioFiltro by rememberSaveable { mutableStateOf<String?>(null) }
    var fechaFiltro by rememberSaveable { mutableStateOf("") }
    var menuServicioAbierto by remember { mutableStateOf(false) }
    val filtradas = aprobadas.filter {
        (servicioFiltro == null || it.actividadId == servicioFiltro) && (fechaFiltro.isBlank() || it.fecha == fechaFiltro)
    }

    LaunchedEffect(negocioId) { viewModel.cargar(negocioId) }

    PantallaBase(titulo = "Gestión") { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Ingresos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            item {
                TarjetaTint {
                    Text("Total de reservas aprobadas", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = formatoPesos(aprobadas.sumOf { it.precio }),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text("${aprobadas.size} reservas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item {
                ExposedDropdownMenuBox(expanded = menuServicioAbierto, onExpandedChange = { menuServicioAbierto = it }) {
                    OutlinedTextField(
                        value = serviciosNegocio.firstOrNull { it.id == servicioFiltro }?.nombre ?: "Todos los servicios",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Filtrar por servicio") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuServicioAbierto) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = menuServicioAbierto, onDismissRequest = { menuServicioAbierto = false }) {
                        DropdownMenuItem(text = { Text("Todos los servicios") }, onClick = { servicioFiltro = null; menuServicioAbierto = false })
                        serviciosNegocio.forEach { servicio ->
                            DropdownMenuItem(text = { Text(servicio.nombre) }, onClick = { servicioFiltro = servicio.id; menuServicioAbierto = false })
                        }
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = fechaFiltro,
                    onValueChange = { fechaFiltro = it },
                    label = { Text("Filtrar por fecha (AAAA-MM-DD)") },
                    placeholder = { Text("2026-09-22") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (servicioFiltro != null || fechaFiltro.isNotBlank()) {
                item {
                    TarjetaTint {
                        Text("Pagos filtrados", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = formatoPesos(filtradas.sumOf { it.precio }),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text("${filtradas.size} reservas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item {
                OutlinedButton(onClick = { onNavegar(Agenda) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Ver agenda")
                }
            }
            item {
                Text(
                    text = "Solicitudes por revisar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (estado.solicitudes.isEmpty()) {
                item { EstadoVacio(mensaje = "No hay solicitudes pendientes.") }
            } else {
                items(estado.solicitudes, key = { it.id }) { solicitud ->
                    val actividad = Negocios.actividadDe(solicitud.actividadId)
                    ElevatedCard(onClick = { onNavegar(DetalleSolicitud(solicitud.id)) }, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = actividad?.nombre.orEmpty(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                Etiqueta(texto = solicitud.estado.etiqueta, tono = solicitud.estado.tono)
                            }
                            Text(
                                text = formatoIntervalo(solicitud.fecha, solicitud.hora, solicitud.duracionHoras),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(text = formatoPesos(solicitud.precio), style = MaterialTheme.typography.titleSmall)
                        }
                    }
                }
            }
            if (estado.transferencias.isNotEmpty()) {
                item {
                    Text(
                        text = "Transferencias por aprobar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(estado.transferencias, key = { it.id }) { transferencia ->
                    ElevatedCard(onClick = { onNavegar(DetalleTransferencia(transferencia.id)) }, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Interesado esperando revisión", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}
