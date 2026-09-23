package com.sportec.sporthub.ui.screens.admin

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos
import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportec.sporthub.domain.EstadoNegocio
import com.sportec.sporthub.domain.Negocios
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import com.sportec.sporthub.ui.components.EstadoVacio
import com.sportec.sporthub.ui.components.PantallaBase

@Composable
fun ValidacionesScreen(onAbrir: (String) -> Unit) {
    val negocios by Negocios.negocios.collectAsState()
    val pendientes = remember(negocios) { negocios.filter { it.estado == EstadoNegocio.PENDIENTE } }
    val conCambios = remember(negocios) { negocios.filter { it.cambioPendiente != null } }

    PantallaBase(titulo = Textos.obtener(R.string.texto_negocios_por_validar)) { padding ->
        if (pendientes.isEmpty() && conCambios.isEmpty()) {
            EstadoVacio(mensaje = Textos.obtener(R.string.texto_no_hay_negocios_pendientes_de_validacion), modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pendientes, key = { it.id }) { negocio ->
                    ElevatedCard(onClick = { onAbrir(negocio.id) }, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(negocio.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(text = stringResource(R.string.texto_nit_valor_valor, negocio.nit, negocio.direccion),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (conCambios.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.validaciones_cambios_titulo),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                items(conCambios, key = { "cambio-" + it.id }) { negocio ->
                    val cambio = negocio.cambioPendiente
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(negocio.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            if (cambio != null) {
                                DatoCambio(stringResource(R.string.validacion_nombre_actual), negocio.nombre)
                                DatoCambio(stringResource(R.string.validacion_nombre_nuevo), cambio.nombre)
                                DatoCambio(stringResource(R.string.validacion_nit_actual), negocio.nit)
                                DatoCambio(stringResource(R.string.validacion_nit_nuevo), cambio.nit)
                                Button(
                                    onClick = { Negocios.resolverCambioSensible(negocio.id, aprobado = true) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.validacion_aprobar_cambio))
                                }
                                OutlinedButton(
                                    onClick = { Negocios.resolverCambioSensible(negocio.id, aprobado = false) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.validacion_rechazar_cambio))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DatoCambio(etiqueta: String, valor: String) {
    Text(
        text = etiqueta + ": " + valor,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
