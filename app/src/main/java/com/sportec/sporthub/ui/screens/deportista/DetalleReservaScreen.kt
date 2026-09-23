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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportec.sporthub.domain.EstadoReserva
import com.sportec.sporthub.ui.components.EstadoError
import com.sportec.sporthub.ui.components.Etiqueta
import com.sportec.sporthub.ui.components.TonoEtiqueta
import com.sportec.sporthub.ui.components.tono
import com.sportec.sporthub.ui.components.TarjetaHechos
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.utils.formatoIntervalo
import com.sportec.sporthub.utils.formatoPesos

@Composable
fun DetalleReservaScreen(
    reservaId: String,
    usuarioId: String,
    onBack: () -> Unit,
    onCancelar: (String) -> Unit,
    onPublicarTransferencia: (String) -> Unit,
    onVerTransferencia: (String) -> Unit,
    onCalificar: (String) -> Unit,
    viewModel: DetalleReservaViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(reservaId) { viewModel.cargar(reservaId) }

    PantallaBase(titulo = Textos.obtener(R.string.texto_reserva), onBack = onBack) { padding ->
        when {
            estado.reserva == null || estado.item == null -> EstadoError(
                mensaje = estado.error ?: Textos.obtener(R.string.texto_no_se_pudo_cargar_la_reserva),
                onReintentar = { viewModel.cargar(reservaId) },
                modifier = Modifier.padding(padding)
            )
            else -> {
                val reserva = estado.reserva!!
                val item = estado.item!!
                val esDueno = reserva.usuarioId == usuarioId
                val esCancelable = esDueno && reserva.estado == EstadoReserva.CONFIRMADA && !reserva.cerrada

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Etiqueta(texto = if (reserva.cerrada) Textos.obtener(R.string.texto_cerrada_por_el_establecimiento) else reserva.estado.etiqueta, tono = if (reserva.cerrada) TonoEtiqueta.GRIS else reserva.estado.tono)
                    TarjetaHechos(
                        pares = listOf(
                            Textos.obtener(R.string.texto_servicio) to item.actividad.nombre,
                            Textos.obtener(R.string.texto_establecimiento) to item.negocio.nombre,
                            Textos.obtener(R.string.texto_horario) to formatoIntervalo(reserva.fecha, reserva.hora, reserva.duracionHoras),
                            Textos.obtener(R.string.texto_total_pagado) to formatoPesos(reserva.precio),
                            Textos.obtener(R.string.texto_codigo) to reserva.id.uppercase()
                        )
                    )
                    if (esCancelable && reserva.politica.transferable && estado.transferenciaId == null) {
                        OutlinedButton(onClick = { onPublicarTransferencia(reserva.id) }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.texto_publicar_transferencia))
                        }
                    }
                    if (esCancelable) {
                        OutlinedButton(onClick = { onCancelar(reserva.id) }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.texto_cancelar_reserva))
                        }
                    }
                    if (esDueno && estado.transferenciaId != null) {
                        OutlinedButton(onClick = { onVerTransferencia(estado.transferenciaId!!) }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.texto_ver_transferencias_de_esta_reserva))
                        }
                    }
                    if (esDueno && reserva.cerrada && !estado.yaCalificada) {
                        OutlinedButton(onClick = { onCalificar(reserva.id) }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.texto_calificar))
                        }
                    }
                }
            }
        }
    }
}
