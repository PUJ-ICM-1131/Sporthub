package com.sportec.sporthub.ui.screens.establecimiento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import com.sportec.sporthub.domain.EstadoNegocio
import com.sportec.sporthub.domain.Negocios
import com.sportec.sporthub.ui.components.EstadoError
import com.sportec.sporthub.ui.components.Etiqueta
import com.sportec.sporthub.ui.components.tono
import com.sportec.sporthub.ui.components.PantallaBase

@Composable
fun ValidacionNegocioScreen(negocioId: String, operadorId: String) {
    val negocios by Negocios.negocios.collectAsState()
    val negocio = remember(negocios, negocioId) { negocios.firstOrNull { it.id == negocioId } }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    PantallaBase(titulo = "Estado de validación") { padding ->
        if (negocio == null) {
            EstadoError(mensaje = "El negocio ya no existe.", onReintentar = {}, modifier = Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Etiqueta(texto = negocio.estado.etiqueta, tono = negocio.estado.tono)
                Text(text = negocio.nombre, style = MaterialTheme.typography.titleMedium)
                Text(text = "NIT ${negocio.nit}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (negocio.estado != EstadoNegocio.VALIDADO) {
                    Text(
                        text = "Verifica que el nombre y el NIT estén completos y vuelve a enviar tu registro a revisión.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (error != null) {
                        Text(text = error ?: "", color = MaterialTheme.colorScheme.error)
                    }
                    Button(
                        onClick = {
                            try {
                                Negocios.solicitarValidacion(negocioId, operadorId)
                                error = null
                            } catch (e: Exception) {
                                error = e.message
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reenviar a revisión")
                    }
                }
            }
        }
    }
}
