package com.sportec.sporthub.ui.screens.comun

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sportec.sporthub.domain.Reportes
import com.sportec.sporthub.ui.components.Aviso
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.TipoAviso

@Composable
fun ReportarScreen(objetivo: String, autorId: String, onBack: () -> Unit, onEnviado: () -> Unit) {
    var motivo by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    PantallaBase(titulo = "Reportar incidencia", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Cuéntanos qué pasó. Un administrador lo revisará.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = motivo,
                onValueChange = { motivo = it; error = null },
                label = { Text("Motivo") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
            if (error != null) {
                Aviso(texto = error ?: "", tipo = TipoAviso.ERROR)
            }
            Button(
                onClick = {
                    try {
                        Reportes.crear(autorId, objetivo, motivo.trim())
                        onEnviado()
                    } catch (e: Exception) {
                        error = e.message ?: "No se pudo enviar el reporte."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enviar reporte")
            }
        }
    }
}
