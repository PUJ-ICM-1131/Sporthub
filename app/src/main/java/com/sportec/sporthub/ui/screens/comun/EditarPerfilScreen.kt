package com.sportec.sporthub.ui.screens.comun

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportec.sporthub.domain.Cuenta
import com.sportec.sporthub.ui.components.Aviso
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.SelectorFoto
import com.sportec.sporthub.ui.components.TipoAviso

@Composable
fun EditarPerfilScreen(
    cuenta: Cuenta?,
    onBack: () -> Unit,
    onGuardado: (Cuenta) -> Unit,
    viewModel: EditarPerfilViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(cuenta?.id) { cuenta?.let(viewModel::cargar) }
    LaunchedEffect(estado.guardada) {
        estado.guardada?.let {
            onGuardado(it)
            onBack()
        }
    }

    PantallaBase(titulo = "Editar perfil", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SelectorFoto(
                fotoUri = estado.fotoUri,
                onFotoSeleccionada = viewModel::onFotoCambiada,
                forma = CircleShape,
                alto = 120.dp,
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally)
            )
            OutlinedTextField(
                value = estado.nombre,
                onValueChange = viewModel::onNombreCambiado,
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.telefono,
                onValueChange = viewModel::onTelefonoCambiado,
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth()
            )
            if (estado.error != null) {
                Aviso(texto = estado.error ?: "", tipo = TipoAviso.ERROR)
            }
            Button(
                onClick = { cuenta?.let(viewModel::guardar) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar cambios")
            }
        }
    }
}
