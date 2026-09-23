package com.sportec.sporthub.ui.screens.deportista

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import com.sportec.sporthub.domain.ActividadConNegocio
import com.sportec.sporthub.domain.Negocios
import com.sportec.sporthub.domain.Resenas
import com.sportec.sporthub.ui.components.EstadoError
import com.sportec.sporthub.ui.components.EstadoVacio
import com.sportec.sporthub.ui.components.TarjetaActividad
import com.sportec.sporthub.ui.components.TarjetaHechos
import com.sportec.sporthub.ui.components.PantallaBase

@Composable
fun DetalleEstablecimientoScreen(
    negocioId: String,
    onBack: () -> Unit,
    onVerServicio: (String) -> Unit,
    onEnviarMensaje: () -> Unit,
    onComoLlegar: () -> Unit
) {
    val negocios by Negocios.negocios.collectAsState()
    val actividades by Negocios.actividades.collectAsState()
    val resenas by Resenas.resenas.collectAsState()
    val negocio = negocios.firstOrNull { it.id == negocioId }
    val servicios = remember(actividades, negocioId) { actividades.filter { it.negocioId == negocioId } }
    val resenasNegocio = remember(resenas, negocioId) { Resenas.listarDeNegocio(negocioId) }
    val promedio = resenasNegocio.map { it.puntaje }.average().takeIf { !it.isNaN() }

    PantallaBase(titulo = negocio?.nombre ?: Textos.obtener(R.string.texto_establecimiento), onBack = onBack) { padding ->
        if (negocio == null) {
            EstadoError(mensaje = Textos.obtener(R.string.texto_el_establecimiento_ya_no_existe), onReintentar = {}, modifier = Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (negocio.descripcion.isNotBlank()) {
                    Text(negocio.descripcion, style = MaterialTheme.typography.bodyLarge)
                }
                TarjetaHechos(
                    pares = buildList {
                        add(Textos.obtener(R.string.texto_direccion) to negocio.direccion)
                        if (negocio.contacto.isNotBlank()) add(Textos.obtener(R.string.texto_contacto) to negocio.contacto)
                        add(Textos.obtener(R.string.texto_calificacion) to (promedio?.let { Textos.obtener(R.string.formato_promedio_resenas, "%.1f".format(it), resenasNegocio.size) } ?: Textos.obtener(R.string.texto_sin_resenas_todavia)))
                    }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onEnviarMensaje, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.texto_enviar_mensaje))
                    }
                    OutlinedButton(onClick = onComoLlegar, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.texto_como_llegar))
                    }
                }
                Text(stringResource(R.string.texto_servicios), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (servicios.isEmpty()) {
                    EstadoVacio(mensaje = Textos.obtener(R.string.texto_este_establecimiento_todavia_no_publica_servicios))
                } else {
                    servicios.forEach { actividad ->
                        val item = ActividadConNegocio(actividad, negocio)
                        TarjetaActividad(item = item, onClick = { onVerServicio(actividad.id) })
                    }
                }
            }
        }
    }
}
