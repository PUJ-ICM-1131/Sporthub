package com.sportec.sporthub.ui.screens.deportista

import coil3.compose.AsyncImage
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.ContentScale
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
import androidx.compose.material3.OutlinedButton
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
import com.sportec.sporthub.ui.components.TipoAviso

@Composable
fun CheckInScreen(
    reservaId: String,
    usuarioId: String,
    onBack: () -> Unit,
    onAdjuntarFoto: () -> Unit
) {
    val reservas by Reservas.reservas.collectAsState()
    val reserva = reservas.firstOrNull { it.id == reservaId }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    PantallaBase(titulo = Textos.obtener(R.string.checkin_titulo), onBack = onBack) { padding ->
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.checkin_detalle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (reserva.llegada) {
                Aviso(texto = stringResource(R.string.checkin_registrada), tipo = TipoAviso.NORMAL)
            }
            reserva.fotoLlegada?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = stringResource(R.string.foto_llegada_titulo),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(180.dp)
                )
            }
            if (error != null) {
                Aviso(texto = error ?: "", tipo = TipoAviso.ERROR)
            }
            Button(
                onClick = {
                    try {
                        Reservas.registrarLlegada(reservaId, usuarioId)
                        error = null
                    } catch (e: Exception) {
                        error = e.message
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.checkin_registrar))
            }
            OutlinedButton(onClick = onAdjuntarFoto, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.checkin_foto))
            }
        }
    }
}
