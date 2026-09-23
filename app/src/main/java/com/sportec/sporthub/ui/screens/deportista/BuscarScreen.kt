package com.sportec.sporthub.ui.screens.deportista

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos
import androidx.compose.ui.res.stringResource

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

    PantallaBase(titulo = Textos.obtener(R.string.texto_encuentra_tu_plan), onBack = onBack) { padding ->
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
                    placeholder = { Text(stringResource(R.string.texto_actividad_o_establecimiento)) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onVerEnMapa) { Text(stringResource(R.string.texto_ver_en_mapa)) }
                }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = estado.categoriaSeleccionada == null,
                            onClick = { viewModel.seleccionarCategoria(null) },
                            label = { Text(stringResource(R.string.texto_todas)) }
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
                Text(text = stringResource(R.string.texto_valor_actividades_con_disponibilidad, estado.resultados.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (estado.resultados.isEmpty()) {
                item { EstadoVacio(mensaje = Textos.obtener(R.string.texto_no_encontramos_actividades_con_esos_filtros)) }
            } else {
                items(estado.resultados, key = { it.actividad.id }) { item ->
                    TarjetaActividad(item = item, onClick = { onVerActividad(item.actividad.id) })
                }
            }
        }
    }
}
