package com.sportec.sporthub.ui.screens.deportista

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportec.sporthub.ui.components.Aviso
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.TipoAviso

@Composable
fun ResenaScreen(
    reservaId: String,
    usuarioId: String,
    onBack: () -> Unit,
    viewModel: ResenaViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(estado.enviada) { if (estado.enviada) onBack() }

    PantallaBase(titulo = "Calificar", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("¿Cómo estuvo tu experiencia?")
            Row {
                (1..5).forEach { valor ->
                    IconButton(onClick = { viewModel.onPuntajeCambiado(valor) }) {
                        Icon(
                            imageVector = if (valor <= estado.puntaje) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = "$valor estrellas",
                            tint = Color(0xFFDF854B)
                        )
                    }
                }
            }
            OutlinedTextField(
                value = estado.comentario,
                onValueChange = viewModel::onComentarioCambiado,
                label = { Text("Comentario (opcional)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            if (estado.error != null) {
                Aviso(texto = estado.error ?: "", tipo = TipoAviso.ERROR)
            }
            Button(onClick = { viewModel.enviar(reservaId, usuarioId) }, modifier = Modifier.fillMaxWidth()) {
                Text("Enviar calificación")
            }
        }
    }
}
