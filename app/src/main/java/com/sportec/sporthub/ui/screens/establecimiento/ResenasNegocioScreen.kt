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
import com.sportec.sporthub.domain.Cuentas
import com.sportec.sporthub.domain.Resenas
import com.sportec.sporthub.ui.components.EstadoVacio
import com.sportec.sporthub.ui.components.PantallaBase

@Composable
fun ResenasNegocioScreen(negocioId: String, onResponder: (String) -> Unit) {
    val resenas by Resenas.resenas.collectAsState()
    val propias = remember(resenas, negocioId) { Resenas.listarDeNegocio(negocioId) }

    PantallaBase(titulo = Textos.obtener(R.string.texto_resenas)) { padding ->
        if (propias.isEmpty()) {
            EstadoVacio(mensaje = Textos.obtener(R.string.texto_las_resenas_de_reservas_cerradas_apareceran_aqui), modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(propias, key = { it.id }) { resena ->
                    val autor = Cuentas.porId(resena.usuarioId)?.nombre ?: Textos.obtener(R.string.texto_deportista)
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = stringResource(R.string.formato_autor_puntaje, autor, resena.puntaje), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(text = resena.comentario, style = MaterialTheme.typography.bodyMedium)
                            if (resena.respuesta != null) {
                                Text(text = stringResource(R.string.texto_tu_respuesta_valor, resena.respuesta),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                OutlinedButton(onClick = { onResponder(resena.id) }, modifier = Modifier.fillMaxWidth()) {
                                    Text(stringResource(R.string.texto_responder))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
