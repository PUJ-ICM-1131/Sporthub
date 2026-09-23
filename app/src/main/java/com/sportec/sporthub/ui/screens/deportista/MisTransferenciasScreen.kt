package com.sportec.sporthub.ui.screens.deportista

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos
import androidx.compose.ui.res.stringResource

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
import com.sportec.sporthub.ui.components.Etiqueta
import com.sportec.sporthub.ui.components.EstadoVacio
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.tono
import com.sportec.sporthub.utils.formatoIntervalo
import com.sportec.sporthub.utils.formatoPesos

@Composable
fun MisTransferenciasScreen(
    usuarioId: String,
    onBack: () -> Unit,
    onAbrir: (String) -> Unit
) {
    val transferencias by Transferencias.transferencias.collectAsState()
    val mias = remember(transferencias, usuarioId) { Transferencias.listarDeUsuario(usuarioId) }

    PantallaBase(titulo = Textos.obtener(R.string.mis_transferencias_titulo), onBack = onBack) { padding ->
        if (mias.isEmpty()) {
            EstadoVacio(
                mensaje = stringResource(R.string.mis_transferencias_vacio),
                modifier = Modifier.padding(padding)
            )
            return@PantallaBase
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(mias, key = { it.id }) { transferencia ->
                val reserva = Reservas.obtenerReserva(transferencia.reservaId)
                val actividad = reserva?.let { Negocios.actividadDe(it.actividadId) }
                ElevatedCard(onClick = { onAbrir(transferencia.id) }, modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
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
