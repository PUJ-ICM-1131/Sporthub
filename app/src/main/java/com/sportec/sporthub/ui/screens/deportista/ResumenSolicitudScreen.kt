package com.sportec.sporthub.ui.screens.deportista

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos
import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
fun ResumenSolicitudScreen(
    actividadId: String,
    fecha: String,
    hora: Int,
    usuarioId: String,
    onBack: () -> Unit,
    onSolicitudEnviada: (String) -> Unit,
    viewModel: ResumenSolicitudViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(actividadId) { viewModel.cargar(actividadId) }

    PantallaBase(titulo = Textos.obtener(R.string.texto_resumen_de_la_solicitud), onBack = onBack) { padding ->
        val item = estado.item
        if (item == null) {
            EstadoError(
                mensaje = estado.error ?: Textos.obtener(R.string.texto_no_se_pudo_cargar_el_servicio),
                onReintentar = { viewModel.cargar(actividadId) },
                modifier = Modifier.padding(padding)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TarjetaHechos(
                    pares = listOf(
                        Textos.obtener(R.string.texto_servicio) to item.actividad.nombre,
                        Textos.obtener(R.string.texto_establecimiento) to item.negocio.nombre,
                        Textos.obtener(R.string.texto_horario) to formatoIntervalo(fecha, hora, item.actividad.duracionMinutos / 60),
                        Textos.obtener(R.string.texto_total_a_pagar) to formatoPesos(item.actividad.precio)
                    )
                )
                Text(text = stringResource(R.string.texto_esta_solicitud_entra_a_revision_del_establecimiento_si_hay),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (estado.error != null) {
                    Aviso(texto = estado.error ?: "", tipo = TipoAviso.ERROR)
                }
                Button(
                    onClick = { viewModel.enviar(usuarioId, fecha, hora, onSolicitudEnviada) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.texto_enviar_solicitud))
                }
            }
        }
    }
}
