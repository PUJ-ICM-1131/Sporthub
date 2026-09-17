package com.sportec.sporthub.ui.screens.establecimiento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportec.sporthub.domain.EstadoPago
import com.sportec.sporthub.domain.Negocios
import com.sportec.sporthub.domain.Reservas
import com.sportec.sporthub.ui.components.EstadoVacio
import com.sportec.sporthub.ui.components.Etiqueta
import com.sportec.sporthub.ui.components.tono
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.utils.formatoPesos

@Composable
fun PagosScreen(negocioId: String) {
    val pagos by Reservas.pagos.collectAsState()
    val propios = remember(pagos, negocioId) { Reservas.listarPagosNegocio(negocioId) }
    val servicios = remember(negocioId) { Negocios.actividadesDe(negocioId) }
    var servicioFiltro by rememberSaveable { mutableStateOf<String?>(null) }

    fun actividadDe(pago: com.sportec.sporthub.domain.Pago) =
        pago.reservaId?.let { Reservas.obtenerReserva(it) }?.let { Negocios.actividadDe(it.actividadId) }

    val filtrados = remember(propios, servicioFiltro) {
        if (servicioFiltro == null) propios else propios.filter { actividadDe(it)?.id == servicioFiltro }
    }

    PantallaBase(titulo = "Pagos") { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (servicios.size > 1) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = servicioFiltro == null,
                            onClick = { servicioFiltro = null },
                            label = { Text("Todos los servicios") }
                        )
                    }
                    items(servicios, key = { it.id }) { servicio ->
                        FilterChip(
                            selected = servicioFiltro == servicio.id,
                            onClick = { servicioFiltro = servicio.id },
                            label = { Text(servicio.nombre) }
                        )
                    }
                }
            }
            if (filtrados.isEmpty()) {
                EstadoVacio(mensaje = "No hay cobros registrados.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtrados, key = { it.id }) { pago ->
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = formatoPesos(pago.monto),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Etiqueta(texto = if (pago.estado == EstadoPago.EXITOSO) "Exitoso" else "Fallido", tono = pago.estado.tono)
                                }
                                Text(
                                    text = actividadDe(pago)?.nombre ?: pago.medio,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = pago.medio,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(text = "Operación ${pago.id.uppercase()}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
