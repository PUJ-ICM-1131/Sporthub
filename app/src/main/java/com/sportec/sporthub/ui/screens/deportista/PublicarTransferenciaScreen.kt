package com.sportec.sporthub.ui.screens.deportista

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.sportec.sporthub.ui.components.EstadoError
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.TarjetaHechos
import com.sportec.sporthub.utils.formatoIntervalo
import com.sportec.sporthub.utils.formatoPesos

@Composable
fun PublicarTransferenciaScreen(
    reservaId: String,
    usuarioId: String,
    onBack: () -> Unit,
    onPublicada: (String) -> Unit,
    viewModel: PublicarTransferenciaViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(reservaId) { viewModel.cargar(reservaId) }
    LaunchedEffect(estado.publicada) { estado.publicada?.let(onPublicada) }

    PantallaBase(titulo = "Publicar transferencia", onBack = onBack) { padding ->
        val reserva = estado.reserva
        if (reserva == null) {
            EstadoError(mensaje = estado.error ?: "La reserva ya no existe.", onReintentar = { viewModel.cargar(reservaId) }, modifier = Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Publica tu reserva para que otro deportista pueda tomarla. Sigues siendo titular hasta que el destinatario aprobado pague.",
                    style = MaterialTheme.typography.bodyMedium
                )
                TarjetaHechos(
                    pares = listOf(
                        "Horario" to formatoIntervalo(reserva.fecha, reserva.hora, reserva.duracionHoras),
                        "Pago del destinatario" to formatoPesos(reserva.precio),
                        "Devolución aplicable a ti" to formatoPesos(Math.round(reserva.precio * reserva.politica.refundPercent / 100.0).toInt())
                    )
                )
                if (estado.error != null) {
                    Text(text = estado.error ?: "", color = MaterialTheme.colorScheme.error)
                }
                Button(onClick = { viewModel.publicar(usuarioId) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Publicar reserva")
                }
            }
        }
    }
}
