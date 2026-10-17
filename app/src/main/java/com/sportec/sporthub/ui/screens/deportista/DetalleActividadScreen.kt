package com.sportec.sporthub.ui.screens.deportista

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import com.sportec.sporthub.ui.components.EstadoError
import com.sportec.sporthub.ui.components.Etiqueta
import com.sportec.sporthub.ui.components.IlustracionActividad
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.TarjetaHechos
import com.sportec.sporthub.utils.formatoDuracion
import com.sportec.sporthub.utils.formatoPesos

@Composable
fun DetalleActividadScreen(
    actividadId: String,
    onBack: () -> Unit,
    onVerHorarios: () -> Unit,
    onVerEstablecimiento: () -> Unit,
    viewModel: DetalleActividadViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(actividadId) { viewModel.cargar(actividadId) }

    PantallaBase(titulo = estado.item?.actividad?.nombre ?: Textos.obtener(R.string.texto_detalle_del_servicio), onBack = onBack) { padding ->
        when {
            estado.error != null -> EstadoError(
                mensaje = estado.error ?: "",
                onReintentar = { viewModel.cargar(actividadId) },
                modifier = Modifier.padding(padding)
            )
            estado.item != null -> {
                val item = estado.item!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (item.actividad.fotos.isEmpty()) IlustracionActividad(item.actividad)
                    else com.sportec.sporthub.ui.components.CarruselFotos(item.actividad.fotos)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Etiqueta(texto = item.actividad.categoria)
                        Etiqueta(
                            texto = item.actividad.tipo.etiqueta,
                            contenedor = MaterialTheme.colorScheme.secondaryContainer,
                            contenido = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Text(
                        text = formatoPesos(item.actividad.precio),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(text = item.actividad.descripcion, style = MaterialTheme.typography.bodyLarge)
                    TarjetaHechos(
                        pares = listOf(
                            Textos.obtener(R.string.texto_establecimiento) to item.negocio.nombre,
                            Textos.obtener(R.string.texto_direccion) to item.negocio.direccion,
                            Textos.obtener(R.string.texto_duracion) to formatoDuracion(item.actividad.duracionMinutos),
                            Textos.obtener(R.string.texto_capacidad) to if (item.actividad.tipo.etiqueta == Textos.obtener(R.string.texto_espacio_completo)) Textos.obtener(R.string.texto_espacio_exclusivo) else Textos.obtener(R.string.texto_valor_cupos_por_sesion, item.actividad.capacidad)
                        )
                    )
                    TarjetaHechos(
                        pares = listOf(
                            Textos.obtener(R.string.texto_plazo_de_pago) to Textos.obtener(R.string.texto_valor_min_desde_la_aprobacion, item.actividad.politica.payMinutes),
                            Textos.obtener(R.string.texto_cancelar_hasta) to Textos.obtener(R.string.texto_valor_h_antes_del_inicio, item.actividad.politica.cancelHours),
                            Textos.obtener(R.string.texto_devolucion_aplicable) to Textos.obtener(R.string.formato_porcentaje, item.actividad.politica.refundPercent),
                            Textos.obtener(R.string.texto_transferencia) to if (item.actividad.politica.transferable) Textos.obtener(R.string.texto_permitida) else Textos.obtener(R.string.texto_no_permitida)
                        )
                    )
                    Button(onClick = onVerHorarios, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.texto_ver_horarios))
                    }
                    OutlinedButton(onClick = onVerEstablecimiento, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.texto_ver_establecimiento))
                    }
                }
            }
        }
    }
}
