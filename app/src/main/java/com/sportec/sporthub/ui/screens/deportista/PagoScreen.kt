package com.sportec.sporthub.ui.screens.deportista

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportec.sporthub.ui.components.Aviso
import com.sportec.sporthub.ui.components.TipoAviso
import com.sportec.sporthub.ui.components.EstadoError
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.TarjetaHechos
import com.sportec.sporthub.utils.formatoPesos

private val MEDIOS_DEMO = listOf("Tarjeta ·•• 1234", "Billetera ·•• 4567", "Transferencia ·•• 1122")

@Composable
fun PagoScreen(
    solicitudId: String,
    usuarioId: String,
    onBack: () -> Unit,
    onPagoExitoso: (reservaId: String) -> Unit,
    viewModel: PagoViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()
    var medio by rememberSaveable { mutableStateOf(MEDIOS_DEMO.first()) }

    LaunchedEffect(solicitudId) { viewModel.cargar(solicitudId) }

    PantallaBase(titulo = "Pago simulado", onBack = onBack) { padding ->
        val monto = estado.monto
        if (monto == null) {
            EstadoError(
                mensaje = estado.error ?: "No se pudo cargar la solicitud.",
                onReintentar = { viewModel.cargar(solicitudId) },
                modifier = Modifier.padding(padding)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TarjetaHechos(pares = listOf("Total a pagar" to formatoPesos(monto)))
                Text(text = "Medio de pago (simulado)", style = MaterialTheme.typography.labelLarge)
                MEDIOS_DEMO.forEach { opcion ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = medio == opcion, onClick = { medio = opcion })
                        Text(text = opcion)
                    }
                }
                if (estado.error != null) {
                    Aviso(texto = estado.error ?: "", tipo = TipoAviso.ERROR)
                }
                Button(
                    onClick = { viewModel.pagar(usuarioId, medio, onPagoExitoso) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Pagar")
                }
            }
        }
    }
}
