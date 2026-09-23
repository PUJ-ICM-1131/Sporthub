package com.sportec.sporthub.ui.screens.deportista

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos
import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.sportec.sporthub.domain.Catalogo
import com.sportec.sporthub.domain.CriteriosBusqueda
import com.sportec.sporthub.domain.Filtros
import com.sportec.sporthub.ui.components.Aviso
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.TipoAviso

@Composable
fun FiltrosScreen(onBack: () -> Unit, onAplicados: () -> Unit) {
    val actuales by Filtros.criterios.collectAsState()
    val categorias = Catalogo.explorar(null).categorias

    var categoria by rememberSaveable { mutableStateOf(actuales.categoria) }
    var precio by rememberSaveable { mutableStateOf(actuales.precioMaximo?.toString().orEmpty()) }
    var radio by rememberSaveable { mutableStateOf(actuales.radioKm) }

    PantallaBase(titulo = Textos.obtener(R.string.filtros_titulo), onBack = onBack) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.filtros_categoria),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = categoria == null,
                            onClick = { categoria = null },
                            label = { Text(stringResource(R.string.filtros_todas)) }
                        )
                    }
                    categorias.forEach { item ->
                        FilterChip(
                            selected = categoria == item.nombre,
                            onClick = { categoria = item.nombre },
                            label = { Text(item.nombre) }
                        )
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = precio,
                    onValueChange = { nuevo -> precio = nuevo.filter { it.isDigit() } },
                    label = { Text(stringResource(R.string.filtros_precio_maximo)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.filtros_radio),
                        style = MaterialTheme.typography.titleSmall
                    )
                    FilterChip(
                        selected = radio == null,
                        onClick = { radio = null },
                        label = { Text(stringResource(R.string.filtros_sin_limite)) }
                    )
                    Filtros.RADIOS.forEach { km ->
                        FilterChip(
                            selected = radio == km,
                            onClick = { radio = km },
                            label = { Text(stringResource(R.string.filtros_radio_km, km)) }
                        )
                    }
                }
            }
            item { Aviso(texto = stringResource(R.string.filtros_aviso), tipo = TipoAviso.NORMAL) }
            item {
                Button(
                    onClick = {
                        Filtros.aplicar(
                            CriteriosBusqueda(
                                categoria = categoria,
                                precioMaximo = precio.toIntOrNull(),
                                radioKm = radio
                            )
                        )
                        onAplicados()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.filtros_aplicar))
                }
            }
            item {
                OutlinedButton(
                    onClick = {
                        Filtros.limpiar()
                        categoria = null
                        precio = ""
                        radio = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.filtros_limpiar))
                }
            }
        }
    }
}
