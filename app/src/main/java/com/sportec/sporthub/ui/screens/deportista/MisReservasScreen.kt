package com.sportec.sporthub.ui.screens.deportista

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.sportec.sporthub.domain.Negocios
import com.sportec.sporthub.domain.Reserva
import com.sportec.sporthub.domain.Solicitud
import com.sportec.sporthub.navigation.DetalleReserva
import com.sportec.sporthub.navigation.DetalleSolicitud
import com.sportec.sporthub.ui.components.EstadoVacio
import com.sportec.sporthub.ui.components.Etiqueta
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.utils.formatoIntervalo
import com.sportec.sporthub.utils.formatoPesos

@Composable
fun MisReservasScreen(
    usuarioId: String,
    onNavegar: (NavKey) -> Unit,
    viewModel: MisReservasViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(usuarioId) { viewModel.cargar(usuarioId) }

    PantallaBase(titulo = "Mis reservas") { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                PestanaReservas.entries.forEachIndexed { index, pestana ->
                    SegmentedButton(
                        selected = estado.pestana == pestana,
                        onClick = { viewModel.seleccionarPestana(pestana) },
                        shape = SegmentedButtonDefaults.itemShape(index, PestanaReservas.entries.size)
                    ) {
                        Text(
                            when (pestana) {
                                PestanaReservas.SOLICITUDES -> "Solicitudes"
                                PestanaReservas.PROXIMAS -> "Próximas"
                                PestanaReservas.HISTORIAL -> "Historial"
                            }
                        )
                    }
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (estado.pestana) {
                    PestanaReservas.SOLICITUDES -> if (estado.solicitudes.isEmpty()) {
                        item { EstadoVacio(mensaje = "Aún no tienes solicitudes.") }
                    } else {
                        items(estado.solicitudes, key = { it.id }) { solicitud ->
                            TarjetaSolicitud(solicitud) { onNavegar(DetalleSolicitud(solicitud.id)) }
                        }
                    }
                    PestanaReservas.PROXIMAS -> if (estado.proximas.isEmpty()) {
                        item { EstadoVacio(mensaje = "Aquí aparecerán tus próximas reservas.") }
                    } else {
                        items(estado.proximas, key = { it.id }) { reserva ->
                            TarjetaReserva(reserva) { onNavegar(DetalleReserva(reserva.id)) }
                        }
                    }
                    PestanaReservas.HISTORIAL -> if (estado.historial.isEmpty()) {
                        item { EstadoVacio(mensaje = "Aquí verás tus reservas canceladas o finalizadas.") }
                    } else {
                        items(estado.historial, key = { it.id }) { reserva ->
                            TarjetaReserva(reserva) { onNavegar(DetalleReserva(reserva.id)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaSolicitud(solicitud: Solicitud, onClick: () -> Unit) {
    val actividad = Negocios.actividadDe(solicitud.actividadId)
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = actividad?.nombre.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Etiqueta(texto = solicitud.estado.etiqueta)
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

@Composable
private fun TarjetaReserva(reserva: Reserva, onClick: () -> Unit) {
    val actividad = Negocios.actividadDe(reserva.actividadId)
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = actividad?.nombre.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Etiqueta(texto = if (reserva.cerrada) "Cerrada" else reserva.estado.etiqueta)
            }
            Text(
                text = formatoIntervalo(reserva.fecha, reserva.hora, reserva.duracionHoras),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = formatoPesos(reserva.precio), style = MaterialTheme.typography.titleSmall)
        }
    }
}
