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
import com.sportec.sporthub.domain.Cuenta
import com.sportec.sporthub.domain.EstadoSolicitud
import com.sportec.sporthub.domain.Rol
import com.sportec.sporthub.ui.components.Aviso
import com.sportec.sporthub.ui.components.TipoAviso
import com.sportec.sporthub.ui.components.EstadoError
import com.sportec.sporthub.ui.components.Etiqueta
import com.sportec.sporthub.ui.components.tono
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.TarjetaHechos
import com.sportec.sporthub.utils.formatoHoraMillis
import com.sportec.sporthub.utils.formatoIntervalo
import com.sportec.sporthub.utils.formatoPesos

private val ACTIVOS = setOf(EstadoSolicitud.FILA, EstadoSolicitud.REVISION, EstadoSolicitud.PAGO)

@Composable
fun DetalleSolicitudScreen(
    solicitudId: String,
    cuenta: Cuenta?,
    onBack: () -> Unit,
    onPagar: (String) -> Unit,
    viewModel: DetalleSolicitudViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()
    var dialogoAbierto by rememberSaveable { mutableStateOf<String?>(null) }
    var motivo by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(solicitudId) { viewModel.cargar(solicitudId) }

    PantallaBase(titulo = Textos.obtener(R.string.texto_solicitud), onBack = onBack) { padding ->
        when {
            estado.solicitud == null || estado.item == null -> EstadoError(
                mensaje = estado.error ?: Textos.obtener(R.string.texto_no_se_pudo_cargar_la_solicitud),
                onReintentar = { viewModel.cargar(solicitudId) },
                modifier = Modifier.padding(padding)
            )
            else -> {
                val solicitud = estado.solicitud!!
                val item = estado.item!!
                val esDueno = cuenta?.id == solicitud.usuarioId
                val esOperador = cuenta?.rol == Rol.ESTABLECIMIENTO && cuenta.negocioId == item.negocio.id

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Etiqueta(texto = solicitud.estado.etiqueta, tono = solicitud.estado.tono)
                    TarjetaHechos(
                        pares = buildList {
                            add(Textos.obtener(R.string.texto_servicio) to item.actividad.nombre)
                            add(Textos.obtener(R.string.texto_establecimiento) to item.negocio.nombre)
                            add(Textos.obtener(R.string.texto_horario) to formatoIntervalo(solicitud.fecha, solicitud.hora, solicitud.duracionHoras))
                            add(Textos.obtener(R.string.texto_total) to formatoPesos(solicitud.precio))
                            solicitud.deadline?.let { add(Textos.obtener(R.string.texto_vence) to formatoHoraMillis(it)) }
                        }
                    )
                    if (estado.error != null) {
                        Aviso(texto = estado.error ?: "", tipo = TipoAviso.ERROR)
                    }

                    if (esDueno) {
                        if (solicitud.estado == EstadoSolicitud.PAGO) {
                            Button(onClick = { onPagar(solicitud.id) }, modifier = Modifier.fillMaxWidth()) {
                                Text(stringResource(R.string.texto_pagar))
                            }
                        }
                        if (solicitud.estado in ACTIVOS) {
                            OutlinedButton(
                                onClick = { motivo = ""; dialogoAbierto = "retirar" },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.texto_retirar_solicitud))
                            }
                        }
                    }

                    if (esOperador) {
                        if (solicitud.estado == EstadoSolicitud.REVISION) {
                            Button(
                                onClick = { viewModel.aprobar(cuenta.id) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.texto_aprobar_y_habilitar_pago))
                            }
                        }
                        if (solicitud.estado == EstadoSolicitud.FILA || solicitud.estado == EstadoSolicitud.REVISION) {
                            OutlinedButton(
                                onClick = { motivo = ""; dialogoAbierto = "rechazar" },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.texto_rechazar_con_motivo))
                            }
                        }
                    }
                }
            }
        }
    }

    if (dialogoAbierto != null) {
        val esRechazo = dialogoAbierto == "rechazar"
        AlertDialog(
            onDismissRequest = { dialogoAbierto = null },
            title = { Text(if (esRechazo) Textos.obtener(R.string.texto_rechazar_solicitud) else Textos.obtener(R.string.texto_retirar_solicitud)) },
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
                        if (esRechazo) viewModel.rechazar(motivo, cuenta?.id.orEmpty())
                        else viewModel.retirar(motivo, cuenta?.id.orEmpty())
                        dialogoAbierto = null
                    }
                ) { Text(stringResource(R.string.texto_confirmar)) }
            },
            dismissButton = {
                TextButton(onClick = { dialogoAbierto = null }) { Text(stringResource(R.string.texto_cancelar)) }
            }
        )
    }
}
