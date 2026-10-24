package com.sportec.sporthub.ui.screens.deportista

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos
import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.height
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.sportec.sporthub.domain.Reservas
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
    onAdjuntarFoto: (String) -> Unit,
    viewModel: DetalleReservaViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()
    var mostrarLlegada by rememberSaveable(reservaId, usuarioId) { mutableStateOf(false) }
    var errorLlegada by rememberSaveable(reservaId, usuarioId) { mutableStateOf<String?>(null) }

    val opiniones by com.sportec.sporthub.domain.Resenas.resenas.collectAsState()
    val reservas by com.sportec.sporthub.domain.Reservas.reservas.collectAsState()
    LaunchedEffect(reservaId, opiniones, reservas) { viewModel.cargar(reservaId) }

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
                    if (esCancelable) {
                        OutlinedButton(onClick = { mostrarLlegada = !mostrarLlegada }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(if (mostrarLlegada) R.string.llegada_ocultar_opciones else R.string.texto_registrar_llegada))
                        }
                        if (reserva.llegada) Text(stringResource(R.string.checkin_registrada))
                        AnimatedVisibility(visible = mostrarLlegada) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(enabled = !reserva.llegada, onClick = {
                                    try {
                                        Reservas.registrarLlegada(reserva.id, usuarioId)
                                        errorLlegada = null
                                    } catch (e: Exception) {
                                        errorLlegada = e.message ?: Textos.obtener(R.string.texto_no_se_pudo_completar_la_accion)
                                    }
                                }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.checkin_registrar)) }
                                OutlinedButton(onClick = { onAdjuntarFoto(reserva.id) }, modifier = Modifier.fillMaxWidth()) {
                                    Text(stringResource(R.string.checkin_foto))
                                }
                                reserva.fotoLlegada?.let { uri ->
                                    AsyncImage(model = uri, contentDescription = stringResource(R.string.foto_llegada_titulo),
                                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(180.dp))
                                }
                                errorLlegada?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                            }
                        }
                    }
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
                    if (esDueno && (reserva.estado == EstadoReserva.CONFIRMADA || reserva.cerrada)) {
                        val opinion = opiniones.firstOrNull { it.reservaId == reserva.id }
                        if (opinion != null) {
                            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.resena_publicada, opinion.puntaje),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        for (valor in 1..5) {
                                            Icon(
                                                imageVector = if (valor <= opinion.puntaje) Icons.Filled.Star else Icons.Filled.StarBorder,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    if (opinion.comentario.isNotBlank()) {
                                        Text(
                                            text = opinion.comentario,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            OutlinedButton(onClick = { onCalificar(reserva.id) }, enabled = reserva.cerrada,
                                modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.texto_calificar)) }
                            if (!reserva.cerrada) Text(stringResource(R.string.resena_disponible_al_cierre))
                        }
                    }
                }
            }
        }
    }
}
