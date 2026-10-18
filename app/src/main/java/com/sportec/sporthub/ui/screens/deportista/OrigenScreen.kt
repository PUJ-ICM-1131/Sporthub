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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportec.sporthub.domain.Ubicacion
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.PermisoSistema
import com.sportec.sporthub.ui.components.SeguimientoUbicacion

@Composable
fun OrigenScreen(onBack: () -> Unit) {
    SeguimientoUbicacion()
    val origenActual by Ubicacion.origen.collectAsState()
    val siguiendo by Ubicacion.usarPosicionActual.collectAsState()
    val posicion by Ubicacion.posicionActual.collectAsState()

    PantallaBase(titulo = Textos.obtener(R.string.texto_origen_de_busqueda), onBack = onBack) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(text = stringResource(R.string.texto_elige_el_punto_desde_donde_se_calcula_la_distancia),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                Card(
                    onClick = { Ubicacion.seguirPosicionActual() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = siguiendo, onClick = { Ubicacion.seguirPosicionActual() })
                        Column {
                            Text(stringResource(R.string.brujula_posicion_actual), fontWeight = FontWeight.Medium)
                            val detalle = posicion?.precisionMetros?.let { stringResource(R.string.origen_precision_m, it.toInt()) }
                                ?: stringResource(if (posicion == null) R.string.origen_buscando_posicion else R.string.origen_posicion_sin_precision)
                            Text(detalle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            item { PermisoSistema() }
            items(Ubicacion.PRESETS) { preset ->
                Card(
                    onClick = { Ubicacion.elegir(preset) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = !siguiendo && preset == origenActual, onClick = { Ubicacion.elegir(preset) })
                        Text(preset.nombre, fontWeight = FontWeight.Medium)
                    }
                }
            }
            if (siguiendo && posicion == null) {
                item {
                    Text(stringResource(R.string.origen_respaldo, origenActual.nombre),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
