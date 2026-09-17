package com.sportec.sporthub.ui.screens.deportista

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
        titulo = "Explorar",
        acciones = {
            IconButton(onClick = { onNavegar(Notificaciones) }) {
                Icon(Icons.Filled.Notifications, contentDescription = "Notificaciones")
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
                    Text(
                        text = "Hola, $nombre",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "¿Cómo quieres\nmoverte hoy?",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            item {
                TarjetaHero(
                    titulo = "Menos rutina.\nMás movimiento.",
                    subtitulo = "Encuentra tu espacio o tu próxima clase.",
                    pillTexto = "TIEMPO PARA TI"
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Encuentra tu actividad",
                        style = MaterialTheme.typography.titleLarge
                    )
                    TextButton(onClick = { onNavegar(Buscar) }) { Text("Ver todas") }
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
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Planes para esta semana",
                        style = MaterialTheme.typography.titleLarge
                    )
                    TextButton(onClick = { onNavegar(Mapa) }) { Text("Mapa") }
                }
            }
            if (estado.actividades.isEmpty()) {
                item { EstadoVacio(mensaje = "No hay servicios publicados en esta categoría.") }
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
                    Text(
                        text = "Un plan que cambia de manos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Encuentra reservas publicadas para transferencia. El negocio debe aprobarte antes del pago.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = { onNavegar(Transferencias) }) { Text("Explorar transferencias") }
                }
            }
        }
    }
}
