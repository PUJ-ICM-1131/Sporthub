package com.sportec.sporthub.ui.screens.deportista

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos
import androidx.compose.ui.res.stringResource

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
import com.sportec.sporthub.domain.Transferencias
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

    val transferencias by Transferencias.transferencias.collectAsState()

    LaunchedEffect(transferenciaId, transferencias) { viewModel.cargar(transferenciaId) }

    PantallaBase(titulo = Textos.obtener(R.string.texto_transferencia), onBack = onBack) { padding ->
        val transferencia = estado.transferencia
        val reserva = estado.reserva
        val item = estado.item
        if (transferencia == null || reserva == null || item == null) {
            EstadoError(
                mensaje = estado.error ?: Textos.obtener(R.string.texto_la_publicacion_ya_no_existe),
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
                        Textos.obtener(R.string.texto_servicio) to item.actividad.nombre,
                        Textos.obtener(R.string.texto_establecimiento) to item.negocio.nombre,
                        Textos.obtener(R.string.texto_horario) to formatoIntervalo(reserva.fecha, reserva.hora, reserva.duracionHoras),
                        Textos.obtener(R.string.texto_importe) to formatoPesos(reserva.precio)
                    )
                )
                if (estado.error != null) {
                    Aviso(texto = estado.error ?: "", tipo = TipoAviso.ERROR)
                }

                if (esOperador) {
                    val candidato = transferencia.postulantes.firstOrNull { it.estado == EstadoPostulante.REVISION }
                    if (candidato != null) {
                        Button(onClick = { viewModel.aprobar(cuenta.id) }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.texto_aprobar_y_habilitar_pago))
                        }
                        OutlinedButton(
                            onClick = { motivo = ""; dialogoRechazo = candidato.id },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.texto_rechazar_con_motivo))
                        }
                    } else {
                        Text(stringResource(R.string.texto_no_hay_interesados_por_revisar), style = MaterialTheme.typography.bodyMedium)
                    }
                } else if (esPropietario) {
                    if (vigente) {
                        Text(text = stringResource(R.string.texto_valor_interesados_activos, transferencia.postulantes.count { it.estado in setOf(EstadoPostulante.FILA, EstadoPostulante.REVISION, EstadoPostulante.PAGO) }),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        OutlinedButton(onClick = { viewModel.retirar(cuenta.id) }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.texto_retirar_publicacion))
                        }
                    }
                } else if (interes != null) {
                    when (interes.estado) {
                        EstadoPostulante.PAGO -> Button(onClick = { onPagar(transferenciaId) }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.texto_pagar_transferencia))
                        }
                        EstadoPostulante.ATENDIDA -> Text(stringResource(R.string.texto_ya_completaste_esta_transferencia), style = MaterialTheme.typography.bodyMedium)
                        else -> Text(stringResource(R.string.texto_tu_interes_esta_valor, interes.estado.etiqueta.lowercase()), style = MaterialTheme.typography.bodyMedium)
                    }
                } else if (vigente && cuenta?.rol == Rol.DEPORTISTA) {
                    Button(onClick = { viewModel.unirse(cuenta.id) }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.texto_aceptar_tomar_esta_reserva))
                    }
                }
            }
        }
    }

    if (dialogoRechazo != null) {
        AlertDialog(
            onDismissRequest = { dialogoRechazo = null },
            title = { Text(stringResource(R.string.texto_rechazar_interesado)) },
            text = {
                OutlinedTextField(
                    value = motivo,
                    onValueChange = { motivo = it },
                    label = { Text(stringResource(R.string.texto_motivo)) },
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
                ) { Text(stringResource(R.string.texto_confirmar)) }
            },
            dismissButton = {
                TextButton(onClick = { dialogoRechazo = null }) { Text(stringResource(R.string.texto_cancelar)) }
            }
        )
    }
}
