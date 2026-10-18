package com.sportec.sporthub.ui.screens.acceso

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos
import androidx.compose.ui.res.stringResource

import com.sportec.sporthub.utils.Validaciones
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sportec.sporthub.ui.components.PantallaBase

@Composable
fun RecuperarScreen(
    onBack: () -> Unit,
    onEnlaceEnviado: (String) -> Unit,
    viewModel: RecuperarViewModel = viewModel()
) {
    var email by rememberSaveable { mutableStateOf("") }
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(estado.enviadoA) {
        val destino = estado.enviadoA
        if (destino != null) {
            viewModel.envioAtendido()
            onEnlaceEnviado(destino)
        }
    }

    PantallaBase(titulo = Textos.obtener(R.string.texto_recuperar_acceso), onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = stringResource(R.string.texto_te_enviaremos_un_enlace_de_recuperacion_a_tu_correo),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; viewModel.limpiarError() },
                label = { Text(stringResource(R.string.texto_correo_electronico)) },
                modifier = Modifier.fillMaxWidth()
            )
            val mensajeError = estado.error
            if (mensajeError != null) {
                Text(
                    text = mensajeError,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Button(
                onClick = { viewModel.enviar(email) },
                enabled = Validaciones.correo(email) && !estado.cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.texto_enviar_enlace))
            }
        }
    }
}
