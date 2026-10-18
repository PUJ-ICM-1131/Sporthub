package com.sportec.sporthub.ui.screens.deportista

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.FilterChip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.sportec.sporthub.domain.Negocios
import com.sportec.sporthub.domain.Reserva
import com.sportec.sporthub.domain.Reservas
import com.sportec.sporthub.domain.Solicitud
import com.sportec.sporthub.domain.Transferencia
import com.sportec.sporthub.domain.Transferencias
import com.sportec.sporthub.navigation.DetalleReserva
import com.sportec.sporthub.navigation.DetalleSolicitud
import com.sportec.sporthub.navigation.DetalleTransferencia
import com.sportec.sporthub.ui.components.EstadoVacio
import com.sportec.sporthub.ui.components.Etiqueta
import com.sportec.sporthub.ui.components.TonoEtiqueta
import com.sportec.sporthub.ui.components.tono
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

    val solicitudes by Reservas.solicitudes.collectAsState()
    val reservas by Reservas.reservas.collectAsState()
    val transferencias by Transferencias.transferencias.collectAsState()

    LaunchedEffect(usuarioId, solicitudes, reservas, transferencias) { viewModel.cargar(usuarioId) }

    PantallaBase(titulo = Textos.obtener(R.string.texto_mis_reservas)) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(PestanaReservas.entries) { pestana ->
                    FilterChip(
                        selected = estado.pestana == pestana,
                        onClick = { viewModel.seleccionarPestana(pestana) },
                        label = {
                            Text(
                                text = when (pestana) {
                                    PestanaReservas.SOLICITUDES -> stringResource(R.string.texto_solicitudes)
                                    PestanaReservas.PROXIMAS -> stringResource(R.string.texto_proximas)
                                    PestanaReservas.HISTORIAL -> stringResource(R.string.texto_historial)
                                    PestanaReservas.TRANSFERENCIAS -> stringResource(R.string.texto_transferencias)
                                },
                                maxLines = 1
                            )
                        }
                    )
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (estado.pestana) {
                    PestanaReservas.SOLICITUDES -> if (estado.solicitudes.isEmpty()) {
                        item { EstadoVacio(mensaje = Textos.obtener(R.string.texto_aun_no_tienes_solicitudes)) }
                    } else {
                        items(estado.solicitudes, key = { it.id }) { solicitud ->
                            TarjetaSolicitud(solicitud) { onNavegar(DetalleSolicitud(solicitud.id)) }
                        }
                    }
                    PestanaReservas.PROXIMAS -> if (estado.proximas.isEmpty()) {
                        item { EstadoVacio(mensaje = Textos.obtener(R.string.texto_aqui_apareceran_tus_proximas_reservas)) }
                    } else {
                        items(estado.proximas, key = { it.id }) { reserva ->
                            TarjetaReserva(reserva) { onNavegar(DetalleReserva(reserva.id)) }
                        }
                    }
                    PestanaReservas.HISTORIAL -> if (estado.historial.isEmpty()) {
                        item { EstadoVacio(mensaje = Textos.obtener(R.string.texto_aqui_veras_tus_reservas_canceladas_o_finalizadas)) }
                    } else {
                        items(estado.historial, key = { it.id }) { reserva ->
                            TarjetaReserva(reserva) { onNavegar(DetalleReserva(reserva.id)) }
                        }
                    }
                    PestanaReservas.TRANSFERENCIAS -> if (estado.transferencias.isEmpty()) {
                        item { EstadoVacio(mensaje = Textos.obtener(R.string.texto_aqui_veras_las_transferencias_que_publiques_o_en_las)) }
                    } else {
                        items(estado.transferencias, key = { it.id }) { transferencia ->
                            TarjetaTransferenciaReserva(transferencia) { onNavegar(DetalleTransferencia(transferencia.id)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaSolicitud(solicitud: Solicitud, onClick: () -> Unit) {
    val actividades by Negocios.actividades.collectAsState()
    val actividad = actividades.firstOrNull { it.id == solicitud.actividadId }
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        actividad?.let { com.sportec.sporthub.ui.components.IlustracionActividad(it) }
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                Text(
                    text = actividad?.nombre.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
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

@Composable
private fun TarjetaReserva(reserva: Reserva, onClick: () -> Unit) {
    val actividades by Negocios.actividades.collectAsState()
    val actividad = actividades.firstOrNull { it.id == reserva.actividadId }
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        actividad?.let { com.sportec.sporthub.ui.components.IlustracionActividad(it) }
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                Text(
                    text = actividad?.nombre.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Etiqueta(texto = if (reserva.cerrada) Textos.obtener(R.string.texto_cerrada) else reserva.estado.etiqueta, tono = if (reserva.cerrada) TonoEtiqueta.GRIS else reserva.estado.tono)
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

@Composable
private fun TarjetaTransferenciaReserva(transferencia: Transferencia, onClick: () -> Unit) {
    val reserva = Reservas.obtenerReserva(transferencia.reservaId)
    val actividades by Negocios.actividades.collectAsState()
    val actividad = actividades.firstOrNull { it.id == reserva?.actividadId }
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        actividad?.let { com.sportec.sporthub.ui.components.IlustracionActividad(it) }
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                Text(
                    text = actividad?.nombre.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Etiqueta(texto = transferencia.estado.etiqueta, tono = transferencia.estado.tono)
            }
            if (reserva != null) {
                Text(
                    text = formatoIntervalo(reserva.fecha, reserva.hora, reserva.duracionHoras),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(text = formatoPesos(reserva.precio), style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}
