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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportec.sporthub.ui.components.Aviso
import com.sportec.sporthub.ui.components.TipoAviso
import com.sportec.sporthub.ui.components.EstadoError
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.TarjetaHechos
import com.sportec.sporthub.utils.formatoPesos

private val MEDIOS_DEMO = listOf(Textos.obtener(R.string.texto_tarjeta_1234), Textos.obtener(R.string.texto_billetera_4567), Textos.obtener(R.string.texto_transferencia_1122))

@Composable
fun PagoScreen(
    solicitudId: String,
    usuarioId: String,
    onBack: () -> Unit,
    onPagoExitoso: (reservaId: String) -> Unit,
    viewModel: PagoViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()
    var medio by rememberSaveable { mutableStateOf(MEDIOS_DEMO.first()) }

    LaunchedEffect(solicitudId) { viewModel.cargar(solicitudId) }

    PantallaBase(titulo = Textos.obtener(R.string.texto_pago_simulado), onBack = onBack) { padding ->
        val monto = estado.monto
        if (monto == null) {
            EstadoError(
                mensaje = estado.error ?: Textos.obtener(R.string.texto_no_se_pudo_cargar_la_solicitud),
                onReintentar = { viewModel.cargar(solicitudId) },
                modifier = Modifier.padding(padding)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TarjetaHechos(pares = listOf(Textos.obtener(R.string.texto_total_a_pagar) to formatoPesos(monto)))
                Text(text = stringResource(R.string.texto_medio_de_pago_simulado), style = MaterialTheme.typography.labelLarge)
                MEDIOS_DEMO.forEach { opcion ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = medio == opcion, onClick = { medio = opcion })
                        Text(text = opcion)
                    }
                }
                if (estado.error != null) {
                    Aviso(texto = estado.error ?: "", tipo = TipoAviso.ERROR)
                }
                Button(
                    onClick = { viewModel.pagar(usuarioId, medio, onPagoExitoso) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.texto_pagar))
                }
            }
        }
    }
}
