package com.sportec.sporthub.ui.screens.establecimiento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportec.sporthub.domain.Negocios
import com.sportec.sporthub.ui.components.EstadoVacio
import com.sportec.sporthub.ui.components.Etiqueta
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.utils.formatoDuracion
import com.sportec.sporthub.utils.formatoPesos

@Composable
fun ServiciosScreen(
    negocioId: String,
    onNuevoServicio: () -> Unit,
    onEditarServicio: (String) -> Unit
) {
    val actividades by Negocios.actividades.collectAsState()
    val propias = actividades.filter { it.negocioId == negocioId }

    PantallaBase(
        titulo = "Servicios",
        acciones = {
            IconButton(onClick = onNuevoServicio) {
                Icon(Icons.Filled.Add, contentDescription = "Nuevo servicio")
            }
        }
    ) { padding ->
        if (propias.isEmpty()) {
            EstadoVacio(mensaje = "Aún no tienes servicios publicados.", modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(propias, key = { it.id }) { actividad ->
                    ElevatedCard(onClick = { onEditarServicio(actividad.id) }, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = actividad.nombre,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(text = formatoPesos(actividad.precio), style = MaterialTheme.typography.titleSmall)
                            }
                            Text(
                                text = "${actividad.categoria} · ${formatoDuracion(actividad.duracionMinutos)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Etiqueta(texto = actividad.tipo.etiqueta)
                        }
                    }
                }
            }
        }
    }
}
