package com.sportec.sporthub.ui.screens.deportista

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos
import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.sportec.sporthub.ui.components.Aviso
import com.sportec.sporthub.ui.components.TipoAviso
import com.sportec.sporthub.ui.components.EstadoError
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.TarjetaHechos
import com.sportec.sporthub.utils.formatoPesos

@Composable
fun CancelarReservaScreen(
    reservaId: String,
    usuarioId: String,
    onBack: () -> Unit,
    onCancelada: () -> Unit,
    viewModel: CancelarReservaViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()
    var motivo by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(reservaId) { viewModel.cargar(reservaId) }
    LaunchedEffect(estado.cancelada) { if (estado.cancelada) onCancelada() }

    PantallaBase(titulo = Textos.obtener(R.string.texto_cancelar_reserva), onBack = onBack) { padding ->
        val reserva = estado.reserva
        if (reserva == null) {
            EstadoError(mensaje = Textos.obtener(R.string.texto_no_se_pudo_cargar_la_reserva), onReintentar = { viewModel.cargar(reservaId) }, modifier = Modifier.padding(padding))
        } else {
            val devolucion = Math.round(reserva.precio * reserva.politica.refundPercent / 100.0).toInt()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TarjetaHechos(
                    pares = listOf(
                        Textos.obtener(R.string.texto_pagado) to formatoPesos(reserva.precio),
                        Textos.obtener(R.string.texto_devolucion_segun_condiciones) to formatoPesos(devolucion),
                        Textos.obtener(R.string.texto_plazo_de_devolucion) to Textos.obtener(R.string.texto_valor_h, reserva.politica.refundHours)
                    )
                )
                OutlinedTextField(
                    value = motivo,
                    onValueChange = { motivo = it },
                    label = { Text(stringResource(R.string.texto_motivo_opcional)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(text = stringResource(R.string.texto_cancelar_no_significa_que_la_devolucion_ya_se_realizo),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (estado.error != null) {
                    Aviso(texto = estado.error ?: "", tipo = TipoAviso.ERROR)
                }
                Button(
                    onClick = { viewModel.confirmar(motivo, usuarioId) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.texto_confirmar_cancelacion))
                }
            }
        }
    }
}
