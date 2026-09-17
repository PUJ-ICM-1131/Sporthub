package com.sportec.sporthub.ui.screens.deportista

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportec.sporthub.ui.components.Aviso
import com.sportec.sporthub.ui.components.TipoAviso
import com.sportec.sporthub.ui.components.EstadoError
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.TarjetaHechos
import com.sportec.sporthub.utils.formatoIntervalo
import com.sportec.sporthub.utils.formatoPesos

@Composable
fun ResumenSolicitudScreen(
    actividadId: String,
    fecha: String,
    hora: Int,
    usuarioId: String,
    onBack: () -> Unit,
    onSolicitudEnviada: (String) -> Unit,
    viewModel: ResumenSolicitudViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(actividadId) { viewModel.cargar(actividadId) }

    PantallaBase(titulo = "Resumen de la solicitud", onBack = onBack) { padding ->
        val item = estado.item
        if (item == null) {
            EstadoError(
                mensaje = estado.error ?: "No se pudo cargar el servicio.",
                onReintentar = { viewModel.cargar(actividadId) },
                modifier = Modifier.padding(padding)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TarjetaHechos(
                    pares = listOf(
                        "Servicio" to item.actividad.nombre,
                        "Establecimiento" to item.negocio.nombre,
                        "Horario" to formatoIntervalo(fecha, hora, item.actividad.duracionMinutos / 60),
                        "Total a pagar" to formatoPesos(item.actividad.precio)
                    )
                )
                Text(
                    text = "Esta solicitud entra a revisión del establecimiento. Si hay cupo, la verás como \"En revisión\"; si no, quedará en fila.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (estado.error != null) {
                    Aviso(texto = estado.error ?: "", tipo = TipoAviso.ERROR)
                }
                Button(
                    onClick = { viewModel.enviar(usuarioId, fecha, hora, onSolicitudEnviada) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Enviar solicitud")
                }
            }
        }
    }
}
