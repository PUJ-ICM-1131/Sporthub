package com.sportec.sporthub.ui.screens.establecimiento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportec.sporthub.domain.Cuenta
import com.sportec.sporthub.domain.TipoActividad
import com.sportec.sporthub.ui.components.Aviso
import com.sportec.sporthub.ui.components.TipoAviso
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.SelectorFoto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarServicioScreen(
    servicioId: String,
    cuenta: Cuenta?,
    onBack: () -> Unit,
    onVerHorarios: (String) -> Unit,
    onVerCondiciones: (String) -> Unit,
    viewModel: EditarServicioViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()
    var menuAbierto by remember { mutableStateOf(false) }

    LaunchedEffect(servicioId) { viewModel.cargar(servicioId) }

    PantallaBase(titulo = if (estado.esNuevo) "Nuevo servicio" else "Editar servicio", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SelectorFoto(fotoUri = estado.fotoUri, onFotoSeleccionada = viewModel::onFotoCambiada)
            OutlinedTextField(
                value = estado.nombre,
                onValueChange = viewModel::onNombreCambiado,
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
            ExposedDropdownMenuBox(expanded = menuAbierto, onExpandedChange = { menuAbierto = it }) {
                OutlinedTextField(
                    value = estado.categoria,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuAbierto) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                    viewModel.categorias.forEach { categoria ->
                        DropdownMenuItem(
                            text = { Text(categoria.nombre) },
                            onClick = {
                                viewModel.onCategoriaCambiada(categoria.nombre)
                                menuAbierto = false
                            }
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = estado.tipo == TipoActividad.ESPACIO,
                    onClick = { viewModel.onTipoCambiado(TipoActividad.ESPACIO) },
                    label = { Text("Espacio completo") }
                )
                FilterChip(
                    selected = estado.tipo == TipoActividad.CLASE,
                    onClick = { viewModel.onTipoCambiado(TipoActividad.CLASE) },
                    label = { Text("Clase grupal") }
                )
            }
            OutlinedTextField(
                value = estado.precio,
                onValueChange = viewModel::onPrecioCambiado,
                label = { Text("Precio") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.duracionMinutos,
                onValueChange = viewModel::onDuracionCambiada,
                label = { Text("Duración (minutos)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.capacidad,
                onValueChange = viewModel::onCapacidadCambiada,
                label = { Text("Capacidad") },
                enabled = estado.tipo == TipoActividad.CLASE,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.descripcion,
                onValueChange = viewModel::onDescripcionCambiada,
                label = { Text("Descripción") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            if (estado.error != null) {
                Aviso(texto = estado.error ?: "", tipo = TipoAviso.ERROR)
            }
            Button(
                onClick = { viewModel.guardar(cuenta?.negocioId.orEmpty(), cuenta?.id.orEmpty()) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar")
            }
            val guardado = estado.guardado
            if (guardado != null) {
                OutlinedButton(onClick = { onVerHorarios(guardado.id) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Horarios y capacidad")
                }
                OutlinedButton(onClick = { onVerCondiciones(guardado.id) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Condiciones")
                }
            }
        }
    }
}
