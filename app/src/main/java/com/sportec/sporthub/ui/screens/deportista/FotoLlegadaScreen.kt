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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sportec.sporthub.domain.Reservas
import com.sportec.sporthub.ui.components.Aviso
import com.sportec.sporthub.ui.components.EstadoVacio
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.SelectorFoto
import com.sportec.sporthub.ui.components.TipoAviso

@Composable
fun FotoLlegadaScreen(
    reservaId: String,
    usuarioId: String,
    onBack: () -> Unit,
    onGuardada: () -> Unit
) {
    val reservas by Reservas.reservas.collectAsState()
    val reserva = reservas.firstOrNull { it.id == reservaId }
    var foto by rememberSaveable { mutableStateOf(reserva?.fotoLlegada) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val sinFoto = stringResource(R.string.foto_llegada_sin_foto)

    PantallaBase(titulo = Textos.obtener(R.string.foto_llegada_titulo), onBack = onBack) { padding ->
        if (reserva == null) {
            EstadoVacio(
                mensaje = stringResource(R.string.reserva_no_encontrada),
                modifier = Modifier.padding(padding)
            )
            return@PantallaBase
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.foto_llegada_detalle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SelectorFoto(
                fotoUri = foto,
                onFotoSeleccionada = { foto = it },
                modifier = Modifier.fillMaxWidth()
            )
            if (error != null) {
                Aviso(texto = error ?: "", tipo = TipoAviso.ERROR)
            }
            Button(
                onClick = {
                    val elegida = foto
                    if (elegida == null) {
                        error = sinFoto
                    } else {
                        try {
                            Reservas.adjuntarFotoLlegada(reservaId, usuarioId, elegida)
                            onGuardada()
                        } catch (e: Exception) {
                            error = e.message
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.foto_llegada_guardar))
            }
        }
    }
}
