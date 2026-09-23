package com.sportec.sporthub.ui.screens.establecimiento

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
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportec.sporthub.domain.Cuenta
import com.sportec.sporthub.domain.EstadoReembolso
import com.sportec.sporthub.domain.Reservas
import com.sportec.sporthub.domain.Rol
import com.sportec.sporthub.ui.components.EstadoVacio
import com.sportec.sporthub.ui.components.Etiqueta
import com.sportec.sporthub.ui.components.tono
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.utils.formatoPesos

@Composable
fun ReembolsosScreen(cuenta: Cuenta?) {
    val reembolsos by Reservas.reembolsos.collectAsState()
    val esNegocio = cuenta?.rol == Rol.ESTABLECIMIENTO
    val propios = remember(reembolsos, cuenta) {
        if (esNegocio) Reservas.listarReembolsosNegocio(cuenta.negocioId.orEmpty())
        else Reservas.listarReembolsosUsuario(cuenta?.id.orEmpty())
    }

    PantallaBase(titulo = Textos.obtener(R.string.texto_reembolsos)) { padding ->
        if (propios.isEmpty()) {
            EstadoVacio(mensaje = Textos.obtener(R.string.texto_no_tienes_devoluciones_por_consultar), modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(propios, key = { it.id }) { reembolso ->
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = formatoPesos(reembolso.monto),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                Etiqueta(texto = reembolso.estado.etiqueta, tono = reembolso.estado.tono)
                            }
                            Text(text = stringResource(R.string.texto_motivo_valor, reembolso.causa),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (esNegocio && reembolso.estado == EstadoReembolso.PENDIENTE) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { Reservas.confirmarReembolso(reembolso.id, cuenta.id, true) },
                                        modifier = Modifier.weight(1f)
                                    ) { Text(stringResource(R.string.texto_realizada)) }
                                    OutlinedButton(
                                        onClick = { Reservas.confirmarReembolso(reembolso.id, cuenta.id, false) },
                                        modifier = Modifier.weight(1f)
                                    ) { Text(stringResource(R.string.texto_fallida)) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
