package com.sportec.sporthub.ui.screens.establecimiento

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportec.sporthub.domain.Negocios
import com.sportec.sporthub.domain.Reserva
import com.sportec.sporthub.domain.Reservas
import com.sportec.sporthub.ui.components.Aviso
import com.sportec.sporthub.ui.components.TipoAviso
import com.sportec.sporthub.ui.components.EstadoVacio
import com.sportec.sporthub.ui.components.Etiqueta
import com.sportec.sporthub.ui.components.TonoEtiqueta
import com.sportec.sporthub.utils.formatoIntervalo
import com.sportec.sporthub.ui.components.PantallaBase

@Composable
fun AgendaScreen(
    negocioId: String,
    operadorId: String,
    viewModel: AgendaViewModel = viewModel()
) {
    val reservas by viewModel.reservas.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(negocioId) { viewModel.cargar(negocioId) }

    PantallaBase(titulo = "Agenda") { padding ->
        if (reservas.isEmpty()) {
            EstadoVacio(mensaje = "Todavía no hay reservas.", modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (error != null) {
                    item { Aviso(texto = error ?: "", tipo = TipoAviso.ERROR) }
                }
                items(reservas, key = { it.id }) { reserva ->
                    TarjetaAgenda(reserva, operadorId, viewModel)
                }
            }
        }
    }
}

@Composable
private fun TarjetaAgenda(reserva: Reserva, operadorId: String, viewModel: AgendaViewModel) {
    val actividad = Negocios.actividadDe(reserva.actividadId)
    val ahora = Reservas.ahora()
    val inicio = Reservas.inicioMillis(reserva.fecha, reserva.hora)
    val fin = Reservas.finMillis(reserva.fecha, reserva.hora, reserva.duracionHoras)
    val fase: String
    val tonoFase: TonoEtiqueta
    when {
        reserva.cerrada -> { fase = "Cerrada por el establecimiento"; tonoFase = TonoEtiqueta.GRIS }
        ahora < inicio -> { fase = "Programada"; tonoFase = TonoEtiqueta.VERDE }
        ahora < fin -> { fase = "En horario"; tonoFase = TonoEtiqueta.VERDE }
        else -> { fase = "Horario finalizado · pendiente de cierre"; tonoFase = TonoEtiqueta.AMBAR }
    }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = actividad?.nombre.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Etiqueta(texto = fase, tono = tonoFase)
            }
            Text(
                text = formatoIntervalo(reserva.fecha, reserva.hora, reserva.duracionHoras),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!reserva.cerrada && ahora in inicio until fin) {
                OutlinedButton(onClick = { viewModel.enviarAvisoSalida(reserva.id, operadorId) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Enviar aviso de salida")
                }
            }
            if (!reserva.cerrada && ahora >= fin) {
                Button(onClick = { viewModel.confirmarCierre(reserva.id, operadorId) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Confirmar cierre")
                }
            }
        }
    }
}
