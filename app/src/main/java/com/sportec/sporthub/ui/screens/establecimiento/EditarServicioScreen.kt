package com.sportec.sporthub.ui.screens.establecimiento

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos
import androidx.compose.ui.res.stringResource

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
    onGuardado: () -> Unit,
    viewModel: EditarServicioViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()
    var menuCategoria by remember { mutableStateOf(false) }
    var menuApertura by remember { mutableStateOf(false) }
    var menuCierre by remember { mutableStateOf(false) }

    LaunchedEffect(servicioId) { viewModel.cargar(servicioId) }
    LaunchedEffect(estado.guardado) { if (estado.guardado != null) onGuardado() }

    PantallaBase(
        titulo = if (estado.esNuevo) Textos.obtener(R.string.texto_nuevo_servicio) else Textos.obtener(R.string.texto_editar_servicio),
        onBack = onBack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TituloBloque(stringResource(R.string.servicio_bloque_datos))

            SelectorFoto(fotoUri = estado.fotoUri, onFotoSeleccionada = viewModel::onFotoCambiada)
            OutlinedTextField(
                value = estado.nombre,
                onValueChange = viewModel::onNombreCambiado,
                label = { Text(stringResource(R.string.texto_nombre)) },
                modifier = Modifier.fillMaxWidth()
            )
            ExposedDropdownMenuBox(expanded = menuCategoria, onExpandedChange = { menuCategoria = it }) {
                OutlinedTextField(
                    value = estado.categoria,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.texto_categoria)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuCategoria) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(expanded = menuCategoria, onDismissRequest = { menuCategoria = false }) {
                    viewModel.categorias.forEach { categoria ->
                        DropdownMenuItem(
                            text = { Text(categoria.nombre) },
                            onClick = {
                                viewModel.onCategoriaCambiada(categoria.nombre)
                                menuCategoria = false
                            }
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = estado.tipo == TipoActividad.ESPACIO,
                    onClick = { viewModel.onTipoCambiado(TipoActividad.ESPACIO) },
                    label = { Text(stringResource(R.string.texto_espacio_completo)) }
                )
                FilterChip(
                    selected = estado.tipo == TipoActividad.CLASE,
                    onClick = { viewModel.onTipoCambiado(TipoActividad.CLASE) },
                    label = { Text(stringResource(R.string.texto_clase_grupal)) }
                )
            }
            OutlinedTextField(
                value = estado.precio,
                onValueChange = viewModel::onPrecioCambiado,
                label = { Text(stringResource(R.string.texto_precio)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.duracionMinutos,
                onValueChange = viewModel::onDuracionCambiada,
                label = { Text(stringResource(R.string.texto_duracion_minutos)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.descripcion,
                onValueChange = viewModel::onDescripcionCambiada,
                label = { Text(stringResource(R.string.texto_descripcion)) },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()
            TituloBloque(stringResource(R.string.servicio_bloque_horario))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = menuApertura,
                    onExpandedChange = { menuApertura = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = estado.horaApertura.toString(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.servicio_hora_apertura)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuApertura) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = menuApertura, onDismissRequest = { menuApertura = false }) {
                        viewModel.horas.forEach { hora ->
                            DropdownMenuItem(
                                text = { Text(hora.toString()) },
                                onClick = {
                                    viewModel.onHoraAperturaCambiada(hora)
                                    menuApertura = false
                                }
                            )
                        }
                    }
                }
                ExposedDropdownMenuBox(
                    expanded = menuCierre,
                    onExpandedChange = { menuCierre = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = estado.horaCierre.toString(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.servicio_hora_cierre)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuCierre) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = menuCierre, onDismissRequest = { menuCierre = false }) {
                        viewModel.horas.forEach { hora ->
                            DropdownMenuItem(
                                text = { Text(hora.toString()) },
                                onClick = {
                                    viewModel.onHoraCierreCambiada(hora)
                                    menuCierre = false
                                }
                            )
                        }
                    }
                }
            }
            OutlinedTextField(
                value = estado.capacidad,
                onValueChange = viewModel::onCapacidadCambiada,
                label = { Text(stringResource(R.string.texto_capacidad)) },
                enabled = estado.tipo == TipoActividad.CLASE,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = stringResource(R.string.servicio_horario_resumen, estado.horaApertura, estado.horaCierre),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()
            TituloBloque(stringResource(R.string.servicio_bloque_condiciones))

            OutlinedTextField(
                value = estado.payMinutes,
                onValueChange = viewModel::onPayMinutesCambiado,
                label = { Text(stringResource(R.string.texto_plazo_de_pago_minutos)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.cancelHours,
                onValueChange = viewModel::onCancelHoursCambiado,
                label = { Text(stringResource(R.string.texto_anticipacion_para_cancelar_horas)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.refundPercent,
                onValueChange = viewModel::onRefundPercentCambiado,
                label = { Text(stringResource(R.string.texto_porcentaje_de_devolucion)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.refundHours,
                onValueChange = viewModel::onRefundHoursCambiado,
                label = { Text(stringResource(R.string.texto_plazo_de_devolucion_horas)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(R.string.texto_permitir_transferencias), modifier = Modifier.weight(1f))
                Switch(checked = estado.transferable, onCheckedChange = viewModel::onTransferableCambiado)
            }
            OutlinedTextField(
                value = estado.extra,
                onValueChange = viewModel::onExtraCambiado,
                label = { Text(stringResource(R.string.texto_otras_condiciones)) },
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
                Text(stringResource(R.string.servicio_guardar_todo))
            }
            Text(
                text = stringResource(R.string.texto_las_solicitudes_existentes_conservan_su_version_original),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TituloBloque(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
}
