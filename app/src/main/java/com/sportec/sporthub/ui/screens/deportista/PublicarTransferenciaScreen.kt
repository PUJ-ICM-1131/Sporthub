package com.sportec.sporthub.ui.screens.deportista

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos
import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportec.sporthub.ui.components.Aviso
import com.sportec.sporthub.ui.components.TipoAviso
import com.sportec.sporthub.ui.components.EstadoError
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.TarjetaHechos
import com.sportec.sporthub.utils.formatoIntervalo
import com.sportec.sporthub.utils.formatoPesos

@Composable
fun PublicarTransferenciaScreen(
    reservaId: String,
    usuarioId: String,
    onBack: () -> Unit,
    onPublicada: (String) -> Unit,
    viewModel: PublicarTransferenciaViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(reservaId) { viewModel.cargar(reservaId) }
    LaunchedEffect(estado.publicada) { estado.publicada?.let(onPublicada) }

    PantallaBase(titulo = Textos.obtener(R.string.texto_publicar_transferencia), onBack = onBack) { padding ->
        val reserva = estado.reserva
        if (reserva == null) {
            EstadoError(mensaje = estado.error ?: Textos.obtener(R.string.texto_la_reserva_ya_no_existe), onReintentar = { viewModel.cargar(reservaId) }, modifier = Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = stringResource(R.string.texto_publica_tu_reserva_para_que_otro_deportista_pueda_tomarla),
                    style = MaterialTheme.typography.bodyMedium
                )
                TarjetaHechos(
                    pares = listOf(
                        Textos.obtener(R.string.texto_horario) to formatoIntervalo(reserva.fecha, reserva.hora, reserva.duracionHoras),
                        Textos.obtener(R.string.texto_pago_del_destinatario) to formatoPesos(reserva.precio),
                        Textos.obtener(R.string.texto_devolucion_aplicable_a_ti) to formatoPesos(Math.round(reserva.precio * reserva.politica.refundPercent / 100.0).toInt())
                    )
                )
                if (estado.error != null) {
                    Aviso(texto = estado.error ?: "", tipo = TipoAviso.ERROR)
                }
                Button(onClick = { viewModel.publicar(usuarioId) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.texto_publicar_reserva))
                }
            }
        }
    }
}
