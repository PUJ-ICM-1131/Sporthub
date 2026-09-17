package com.sportec.sporthub.ui.screens.establecimiento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportec.sporthub.ui.components.PantallaBase

@Composable
fun CondicionesScreen(
    actividadId: String,
    operadorId: String,
    onBack: () -> Unit,
    viewModel: CondicionesViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(actividadId) { viewModel.cargar(actividadId) }
    LaunchedEffect(estado.guardado) { if (estado.guardado) onBack() }

    PantallaBase(titulo = "Condiciones", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = estado.payMinutes,
                onValueChange = viewModel::onPayMinutesCambiado,
                label = { Text("Plazo de pago (minutos)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.cancelHours,
                onValueChange = viewModel::onCancelHoursCambiado,
                label = { Text("Anticipación para cancelar (horas)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.refundPercent,
                onValueChange = viewModel::onRefundPercentCambiado,
                label = { Text("Porcentaje de devolución") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.refundHours,
                onValueChange = viewModel::onRefundHoursCambiado,
                label = { Text("Plazo de devolución (horas)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Permitir transferencias", modifier = Modifier.weight(1f))
                Switch(checked = estado.transferable, onCheckedChange = viewModel::onTransferableCambiado)
            }
            OutlinedTextField(
                value = estado.extra,
                onValueChange = viewModel::onExtraCambiado,
                label = { Text("Otras condiciones") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            if (estado.error != null) {
                Text(text = estado.error ?: "", color = MaterialTheme.colorScheme.error)
            }
            Button(onClick = { viewModel.guardar(operadorId) }, modifier = Modifier.fillMaxWidth()) {
                Text("Guardar nueva versión")
            }
            Text(
                text = "Las solicitudes existentes conservan su versión original.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
