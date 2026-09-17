package com.sportec.sporthub.ui.screens.establecimiento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.utils.formatoDiaCorto
import com.sportec.sporthub.utils.formatoHora

@Composable
fun HorariosServicioScreen(
    actividadId: String,
    operadorId: String,
    onBack: () -> Unit,
    viewModel: HorariosServicioViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(actividadId) { viewModel.cargar(actividadId) }

    PantallaBase(titulo = "Horarios y capacidad", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = estado.actividad?.nombre.orEmpty(), style = MaterialTheme.typography.titleMedium)
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
            Text(
                text = "Toca un horario libre para bloquearlo; toca uno bloqueado para liberarlo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(estado.franjas, key = { it.hora }) { franja ->
                    val bloqueado = franja.hora in estado.bloqueados
                    val comprometido = !bloqueado && franja.cuposLibres < (estado.actividad?.capacidad ?: 0)
                    FilterChip(
                        selected = bloqueado,
                        enabled = !comprometido,
                        onClick = { viewModel.alternarBloqueo(operadorId, franja.hora) },
                        label = {
                            Text(
                                when {
                                    bloqueado -> "${formatoHora(franja.hora)} · Bloqueado"
                                    comprometido -> "${formatoHora(franja.hora)} · Comprometido"
                                    else -> "${formatoHora(franja.hora)} · Libre"
                                }
                            )
                        }
                    )
                }
            }
            if (estado.error != null) {
                Text(text = estado.error ?: "", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
