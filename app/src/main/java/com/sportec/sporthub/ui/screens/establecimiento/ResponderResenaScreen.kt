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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sportec.sporthub.domain.Resenas
import com.sportec.sporthub.ui.components.Aviso
import com.sportec.sporthub.ui.components.TipoAviso
import com.sportec.sporthub.ui.components.EstadoError
import com.sportec.sporthub.ui.components.PantallaBase

@Composable
fun ResponderResenaScreen(
    resenaId: String,
    operadorId: String,
    onBack: () -> Unit
) {
    val resenas by Resenas.resenas.collectAsState()
    val resena = remember(resenas, resenaId) { resenas.firstOrNull { it.id == resenaId } }
    var respuesta by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    PantallaBase(titulo = "Responder reseña", onBack = onBack) { padding ->
        if (resena == null) {
            EstadoError(mensaje = "La reseña ya no existe.", onReintentar = {}, modifier = Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = resena.comentario, style = MaterialTheme.typography.bodyLarge)
                OutlinedTextField(
                    value = respuesta,
                    onValueChange = { respuesta = it },
                    label = { Text("Respuesta del establecimiento") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Aviso(texto = error ?: "", tipo = TipoAviso.ERROR)
                }
                Button(
                    onClick = {
                        try {
                            Resenas.responder(resenaId, respuesta, operadorId)
                            onBack()
                        } catch (e: Exception) {
                            error = e.message
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Publicar respuesta")
                }
            }
        }
    }
}
