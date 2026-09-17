package com.sportec.sporthub.ui.screens.deportista

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportec.sporthub.domain.Cuenta
import com.sportec.sporthub.domain.EstadoPostulante
import com.sportec.sporthub.domain.EstadoTransferencia
import com.sportec.sporthub.domain.Rol
import com.sportec.sporthub.ui.components.Aviso
import com.sportec.sporthub.ui.components.TipoAviso
import com.sportec.sporthub.ui.components.EstadoError
import com.sportec.sporthub.ui.components.Etiqueta
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.TarjetaHechos
import com.sportec.sporthub.ui.components.tono
import com.sportec.sporthub.utils.formatoIntervalo
import com.sportec.sporthub.utils.formatoPesos

@Composable
fun DetalleTransferenciaScreen(
    transferenciaId: String,
    cuenta: Cuenta?,
    onBack: () -> Unit,
    onPagar: (String) -> Unit,
    viewModel: DetalleTransferenciaViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()
    var dialogoRechazo by rememberSaveable { mutableStateOf<String?>(null) }
    var motivo by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(transferenciaId) { viewModel.cargar(transferenciaId) }

    PantallaBase(titulo = "Transferencia", onBack = onBack) { padding ->
        val transferencia = estado.transferencia
        val reserva = estado.reserva
        val item = estado.item
        if (transferencia == null || reserva == null || item == null) {
            EstadoError(
                mensaje = estado.error ?: "La publicación ya no existe.",
                onReintentar = { viewModel.cargar(transferenciaId) },
                modifier = Modifier.padding(padding)
            )
        } else {
            val esPropietario = cuenta?.id == transferencia.propietarioId
            val esOperador = cuenta?.rol == Rol.ESTABLECIMIENTO && cuenta.negocioId == item.negocio.id
            val interes = transferencia.postulantes.lastOrNull { it.usuarioId == cuenta?.id }
            val vigente = transferencia.estado in setOf(EstadoTransferencia.PUBLICADA, EstadoTransferencia.TRAMITE)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Etiqueta(texto = transferencia.estado.etiqueta, tono = transferencia.estado.tono)
                TarjetaHechos(
                    pares = listOf(
                        "Servicio" to item.actividad.nombre,
                        "Establecimiento" to item.negocio.nombre,
                        "Horario" to formatoIntervalo(reserva.fecha, reserva.hora, reserva.duracionHoras),
                        "Importe" to formatoPesos(reserva.precio)
                    )
                )
                if (estado.error != null) {
                    Aviso(texto = estado.error ?: "", tipo = TipoAviso.ERROR)
                }

                if (esOperador) {
                    val candidato = transferencia.postulantes.firstOrNull { it.estado == EstadoPostulante.REVISION }
                    if (candidato != null) {
                        Button(onClick = { viewModel.aprobar(cuenta.id) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Aprobar y habilitar pago")
                        }
                        OutlinedButton(
                            onClick = { motivo = ""; dialogoRechazo = candidato.id },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Rechazar con motivo")
                        }
                    } else {
                        Text("No hay interesados por revisar.", style = MaterialTheme.typography.bodyMedium)
                    }
                } else if (esPropietario) {
                    if (vigente) {
                        Text(
                            text = "${transferencia.postulantes.count { it.estado in setOf(EstadoPostulante.FILA, EstadoPostulante.REVISION, EstadoPostulante.PAGO) }} interesados activos.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        OutlinedButton(onClick = { viewModel.retirar(cuenta.id) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Retirar publicación")
                        }
                    }
                } else if (interes != null) {
                    when (interes.estado) {
                        EstadoPostulante.PAGO -> Button(onClick = { onPagar(transferenciaId) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Pagar transferencia")
                        }
                        EstadoPostulante.ATENDIDA -> Text("Ya completaste esta transferencia.", style = MaterialTheme.typography.bodyMedium)
                        else -> Text("Tu interés está ${interes.estado.name.lowercase()}.", style = MaterialTheme.typography.bodyMedium)
                    }
                } else if (vigente && cuenta?.rol == Rol.DEPORTISTA) {
                    Button(onClick = { viewModel.unirse(cuenta.id) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Aceptar tomar esta reserva")
                    }
                }
            }
        }
    }

    if (dialogoRechazo != null) {
        AlertDialog(
            onDismissRequest = { dialogoRechazo = null },
            title = { Text("Rechazar interesado") },
            text = {
                OutlinedTextField(
                    value = motivo,
                    onValueChange = { motivo = it },
                    label = { Text("Motivo") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    enabled = motivo.isNotBlank(),
                    onClick = {
                        viewModel.rechazar(dialogoRechazo!!, motivo, cuenta?.id.orEmpty())
                        dialogoRechazo = null
                    }
                ) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { dialogoRechazo = null }) { Text("Cancelar") }
            }
        )
    }
}
