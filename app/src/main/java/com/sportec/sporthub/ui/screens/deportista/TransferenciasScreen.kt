package com.sportec.sporthub.ui.screens.deportista

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportec.sporthub.domain.Negocios
import com.sportec.sporthub.domain.Reservas
import com.sportec.sporthub.domain.Transferencias
import com.sportec.sporthub.ui.components.EstadoVacio
import com.sportec.sporthub.ui.components.Etiqueta
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.tono
import com.sportec.sporthub.utils.formatoIntervalo
import com.sportec.sporthub.utils.formatoPesos

@Composable
fun TransferenciasScreen(onBack: () -> Unit, onAbrir: (String) -> Unit) {
    val transferencias by Transferencias.transferencias.collectAsState()
    val publicadas = remember(transferencias) { Transferencias.listarPublicadas() }

    PantallaBase(titulo = "Reservas transferibles", onBack = onBack) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text(
                text = "Toma una reserva publicada. Primero requiere aprobación del negocio.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
            if (publicadas.isEmpty()) {
                EstadoVacio(mensaje = "No hay reservas publicadas con estos filtros.")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(publicadas, key = { it.id }) { transferencia ->
                        val reserva = Reservas.obtenerReserva(transferencia.reservaId)
                        val actividad = reserva?.let { Negocios.actividadDe(it.actividadId) }
                        ElevatedCard(onClick = { onAbrir(transferencia.id) }, modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = actividad?.nombre.orEmpty(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
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
                }
            }
        }
    }
}
