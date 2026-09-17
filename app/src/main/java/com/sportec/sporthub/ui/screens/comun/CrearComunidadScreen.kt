package com.sportec.sporthub.ui.screens.comun

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportec.sporthub.ui.components.Aviso
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.TipoAviso

@Composable
fun CrearComunidadScreen(
    creadorId: String,
    onBack: () -> Unit,
    onCreada: (String) -> Unit,
    viewModel: CrearComunidadViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(estado.creadaId) { estado.creadaId?.let(onCreada) }

    PantallaBase(titulo = "Crear comunidad", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = estado.nombre,
                onValueChange = viewModel::onNombreCambiado,
                label = { Text("Nombre de la comunidad") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.descripcion,
                onValueChange = viewModel::onDescripcionCambiada,
                label = { Text("Descripción") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            if (estado.error != null) {
                Aviso(texto = estado.error ?: "", tipo = TipoAviso.ERROR)
            }
            Button(onClick = { viewModel.crear(creadorId) }, modifier = Modifier.fillMaxWidth()) {
                Text("Crear comunidad")
            }
        }
    }
}
