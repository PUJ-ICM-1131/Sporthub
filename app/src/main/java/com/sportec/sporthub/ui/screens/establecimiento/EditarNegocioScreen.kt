package com.sportec.sporthub.ui.screens.establecimiento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportec.sporthub.ui.components.PantallaBase

@Composable
fun EditarNegocioScreen(
    negocioId: String,
    operadorId: String,
    onBack: () -> Unit,
    viewModel: EditarNegocioViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(negocioId) { viewModel.cargar(negocioId) }
    LaunchedEffect(estado.guardado) { if (estado.guardado) onBack() }

    PantallaBase(titulo = "Ficha del negocio", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = estado.descripcion,
                onValueChange = viewModel::onDescripcionCambiada,
                label = { Text("Descripción") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.contacto,
                onValueChange = viewModel::onContactoCambiado,
                label = { Text("Contacto") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.direccion,
                onValueChange = viewModel::onDireccionCambiada,
                label = { Text("Dirección") },
                modifier = Modifier.fillMaxWidth()
            )
            if (estado.error != null) {
                Text(text = estado.error ?: "", color = MaterialTheme.colorScheme.error)
            }
            Button(onClick = { viewModel.guardar(negocioId, operadorId) }, modifier = Modifier.fillMaxWidth()) {
                Text("Guardar cambios")
            }
        }
    }
}
