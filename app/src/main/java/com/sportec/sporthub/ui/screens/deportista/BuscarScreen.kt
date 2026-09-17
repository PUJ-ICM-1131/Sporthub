package com.sportec.sporthub.ui.screens.deportista

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportec.sporthub.ui.components.EstadoVacio
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.TarjetaActividad

@Composable
fun BuscarScreen(
    onBack: () -> Unit,
    onVerEnMapa: () -> Unit,
    onVerActividad: (String) -> Unit,
    viewModel: BuscarViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()

    PantallaBase(titulo = "Encuentra tu plan", onBack = onBack) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = estado.texto,
                    onValueChange = viewModel::onTextoCambiado,
                    placeholder = { Text("Actividad o establecimiento") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onVerEnMapa) { Text("Ver en mapa") }
                }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = estado.categoriaSeleccionada == null,
                            onClick = { viewModel.seleccionarCategoria(null) },
                            label = { Text("Todas") }
                        )
                    }
                    items(estado.categorias, key = { it.nombre }) { categoria ->
                        FilterChip(
                            selected = estado.categoriaSeleccionada == categoria.nombre,
                            onClick = { viewModel.seleccionarCategoria(categoria.nombre) },
                            label = { Text(categoria.nombre) }
                        )
                    }
                }
            }
            item {
                Text(
                    text = "${estado.resultados.size} actividades con disponibilidad",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (estado.resultados.isEmpty()) {
                item { EstadoVacio(mensaje = "No encontramos actividades con esos filtros.") }
            } else {
                items(estado.resultados, key = { it.actividad.id }) { item ->
                    TarjetaActividad(item = item, onClick = { onVerActividad(item.actividad.id) })
                }
            }
        }
    }
}
