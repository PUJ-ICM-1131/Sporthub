package com.sportec.sporthub.ui.screens.deportista

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos
import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.sportec.sporthub.navigation.Buscar
import com.sportec.sporthub.navigation.DetalleActividad
import com.sportec.sporthub.navigation.Mapa
import com.sportec.sporthub.navigation.Notificaciones
import com.sportec.sporthub.navigation.Transferencias
import com.sportec.sporthub.ui.components.EstadoVacio
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.TarjetaActividad
import com.sportec.sporthub.ui.components.TarjetaHero
import com.sportec.sporthub.ui.components.TarjetaTint

@Composable
fun ExplorarScreen(
    nombre: String,
    onNavegar: (NavKey) -> Unit,
    viewModel: ExplorarViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()

    PantallaBase(
        titulo = Textos.obtener(R.string.texto_explorar),
        acciones = {
            IconButton(onClick = { onNavegar(Notificaciones) }) {
                Icon(Icons.Filled.Notifications, contentDescription = Textos.obtener(R.string.texto_notificaciones))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = stringResource(R.string.texto_hola_valor, nombre),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(text = stringResource(R.string.texto_como_quieres_moverte_hoy),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            item {
                TarjetaHero(
                    titulo = Textos.obtener(R.string.texto_menos_rutina_mas_movimiento),
                    subtitulo = Textos.obtener(R.string.texto_encuentra_tu_espacio_o_tu_proxima_clase),
                    pillTexto = Textos.obtener(R.string.texto_tiempo_para_ti)
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = stringResource(R.string.texto_encuentra_tu_actividad),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { onNavegar(Buscar) }) { Text(stringResource(R.string.texto_ver_todas), maxLines = 1, softWrap = false) }
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = stringResource(R.string.texto_planes_para_esta_semana),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { onNavegar(Mapa) }) { Text(stringResource(R.string.texto_mapa), maxLines = 1, softWrap = false) }
                }
            }
            if (estado.actividades.isEmpty()) {
                item { EstadoVacio(mensaje = Textos.obtener(R.string.texto_no_hay_servicios_publicados_en_esta_categoria)) }
            } else {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                        items(estado.actividades, key = { it.actividad.id }) { item ->
                            Box(modifier = Modifier.width(254.dp)) {
                                TarjetaActividad(
                                    item = item,
                                    onClick = { onNavegar(DetalleActividad(item.actividad.id)) }
                                )
                            }
                        }
                    }
                }
            }
            item {
                TarjetaTint {
                    Text(text = stringResource(R.string.texto_un_plan_que_cambia_de_manos),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(text = stringResource(R.string.texto_encuentra_reservas_publicadas_para_transferencia_el_negocio_debe_aprobarte),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = { onNavegar(Transferencias) }) { Text(stringResource(R.string.texto_explorar_transferencias)) }
                }
            }
        }
    }
}
