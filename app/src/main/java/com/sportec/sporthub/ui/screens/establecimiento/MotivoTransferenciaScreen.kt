package com.sportec.sporthub.ui.screens.establecimiento

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos
import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sportec.sporthub.domain.EstadoPostulante
import com.sportec.sporthub.domain.Transferencias
import com.sportec.sporthub.ui.components.Aviso
import com.sportec.sporthub.ui.components.EstadoVacio
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.TipoAviso

@Composable
fun MotivoTransferenciaScreen(
    transferenciaId: String,
    operadorId: String,
    onBack: () -> Unit,
    onRechazada: () -> Unit
) {
    val transferencias by Transferencias.transferencias.collectAsState()
    val transferencia = transferencias.firstOrNull { it.id == transferenciaId }
    val postulante = transferencia?.postulantes?.firstOrNull { it.estado == EstadoPostulante.REVISION }

    var motivo by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val obligatorio = stringResource(R.string.motivo_obligatorio)

    PantallaBase(titulo = Textos.obtener(R.string.motivo_transferencia_titulo), onBack = onBack) { padding ->
        if (postulante == null) {
            EstadoVacio(
                mensaje = stringResource(R.string.motivo_transferencia_sin_postulante),
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
            OutlinedTextField(
                value = motivo,
                onValueChange = { motivo = it },
                label = { Text(stringResource(R.string.motivo_transferencia_etiqueta)) },
                modifier = Modifier.fillMaxWidth()
            )
            if (error != null) {
                Aviso(texto = error ?: "", tipo = TipoAviso.ERROR)
            }
            Button(
                onClick = {
                    if (motivo.isBlank()) {
                        error = obligatorio
                    } else {
                        try {
                            Transferencias.rechazar(transferenciaId, postulante.usuarioId, motivo.trim(), operadorId)
                            onRechazada()
                        } catch (e: Exception) {
                            error = e.message
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.motivo_transferencia_enviar))
            }
        }
    }
}
