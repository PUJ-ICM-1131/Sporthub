package com.sportec.sporthub.ui.screens.deportista

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportec.sporthub.ui.components.EstadoError
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.utils.formatoDiaCorto
import com.sportec.sporthub.utils.formatoHora

@Composable
fun HorariosScreen(
    actividadId: String,
    onBack: () -> Unit,
    onSolicitar: (fecha: String, hora: Int) -> Unit,
    viewModel: HorariosViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(actividadId) { viewModel.cargar(actividadId) }

    PantallaBase(titulo = "Horarios y cupos", onBack = onBack) { padding ->
        when {
            estado.error != null -> EstadoError(
                mensaje = estado.error ?: "",
                onReintentar = { viewModel.cargar(actividadId) },
                modifier = Modifier.padding(padding)
            )
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = estado.actividad?.nombre.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(text = "Elige una fecha", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(estado.fechas, key = { it }) { fecha ->
                        FilterChip(
                            selected = fecha == estado.fechaSeleccionada,
                            onClick = { viewModel.seleccionarFecha(fecha) },
                            label = { Text(formatoDiaCorto(fecha)) }
                        )
                    }
                }
                Text(text = "Elige un horario", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(estado.franjas, key = { it.hora }) { franja ->
                        FilterChip(
                            selected = franja.hora == estado.horaSeleccionada,
                            enabled = franja.cuposLibres > 0,
                            onClick = { viewModel.seleccionarHora(franja.hora) },
                            label = {
                                Text(
                                    if (franja.cuposLibres > 0) {
                                        "${formatoHora(franja.hora)} · ${franja.cuposLibres} cupo${if (franja.cuposLibres != 1) "s" else ""}"
                                    } else {
                                        "${formatoHora(franja.hora)} · Sin cupo"
                                    }
                                )
                            }
                        )
                    }
                }
                Button(
                    onClick = { onSolicitar(estado.fechaSeleccionada, estado.horaSeleccionada!!) },
                    enabled = estado.horaSeleccionada != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Solicitar este horario")
                }
            }
        }
    }
}
