package com.sportec.sporthub.ui.screens.deportista

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import com.sportec.sporthub.ui.components.Etiqueta
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.TarjetaHechos
import com.sportec.sporthub.utils.formatoDuracion
import com.sportec.sporthub.utils.formatoPesos

@Composable
fun DetalleActividadScreen(
    actividadId: String,
    onBack: () -> Unit,
    onVerHorarios: () -> Unit,
    viewModel: DetalleActividadViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(actividadId) { viewModel.cargar(actividadId) }

    PantallaBase(titulo = estado.item?.actividad?.nombre ?: "Detalle del servicio", onBack = onBack) { padding ->
        when {
            estado.error != null -> EstadoError(
                mensaje = estado.error ?: "",
                onReintentar = { viewModel.cargar(actividadId) },
                modifier = Modifier.padding(padding)
            )
            estado.item != null -> {
                val item = estado.item!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Etiqueta(texto = item.actividad.categoria)
                        Etiqueta(
                            texto = item.actividad.tipo.etiqueta,
                            contenedor = MaterialTheme.colorScheme.secondaryContainer,
                            contenido = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Text(
                        text = formatoPesos(item.actividad.precio),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(text = item.actividad.descripcion, style = MaterialTheme.typography.bodyLarge)
                    TarjetaHechos(
                        pares = listOf(
                            "Establecimiento" to item.negocio.nombre,
                            "Dirección" to item.negocio.direccion,
                            "Duración" to formatoDuracion(item.actividad.duracionMinutos),
                            "Capacidad" to if (item.actividad.tipo.etiqueta == "Espacio completo") "Espacio exclusivo" else "${item.actividad.capacidad} cupos por sesión"
                        )
                    )
                    TarjetaHechos(
                        pares = listOf(
                            "Plazo de pago" to "${item.actividad.politica.payMinutes} min desde la aprobación",
                            "Cancelar hasta" to "${item.actividad.politica.cancelHours} h antes del inicio",
                            "Devolución aplicable" to "${item.actividad.politica.refundPercent} %",
                            "Transferencia" to if (item.actividad.politica.transferable) "Permitida" else "No permitida"
                        )
                    )
                    Button(onClick = onVerHorarios, modifier = Modifier.fillMaxWidth()) {
                        Text("Ver horarios")
                    }
                }
            }
        }
    }
}
