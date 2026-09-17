package com.sportec.sporthub.ui.screens.establecimiento

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
import androidx.compose.material3.OutlinedButton
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
import com.sportec.sporthub.navigation.Agenda
import com.sportec.sporthub.navigation.DetalleSolicitud
import com.sportec.sporthub.navigation.DetalleTransferencia
import com.sportec.sporthub.ui.components.EstadoVacio
import com.sportec.sporthub.ui.components.Etiqueta
import com.sportec.sporthub.ui.components.tono
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.utils.formatoIntervalo
import com.sportec.sporthub.utils.formatoPesos

@Composable
fun GestionScreen(
    negocioId: String,
    onNavegar: (NavKey) -> Unit,
    viewModel: GestionViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(negocioId) { viewModel.cargar(negocioId) }

    PantallaBase(titulo = "Gestión") { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedButton(onClick = { onNavegar(Agenda) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Ver agenda")
                }
            }
            item {
                Text(
                    text = "Solicitudes por revisar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (estado.solicitudes.isEmpty()) {
                item { EstadoVacio(mensaje = "No hay solicitudes pendientes.") }
            } else {
                items(estado.solicitudes, key = { it.id }) { solicitud ->
                    val actividad = Negocios.actividadDe(solicitud.actividadId)
                    ElevatedCard(onClick = { onNavegar(DetalleSolicitud(solicitud.id)) }, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = actividad?.nombre.orEmpty(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
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
            }
            if (estado.transferencias.isNotEmpty()) {
                item {
                    Text(
                        text = "Transferencias por aprobar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(estado.transferencias, key = { it.id }) { transferencia ->
                    ElevatedCard(onClick = { onNavegar(DetalleTransferencia(transferencia.id)) }, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Interesado esperando revisión", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}
