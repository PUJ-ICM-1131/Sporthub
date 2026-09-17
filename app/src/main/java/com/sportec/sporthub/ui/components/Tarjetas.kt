package com.sportec.sporthub.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportec.sporthub.domain.ActividadConNegocio
import com.sportec.sporthub.domain.EstadoNegocio
import com.sportec.sporthub.domain.EstadoPago
import com.sportec.sporthub.domain.EstadoPostulante
import com.sportec.sporthub.domain.EstadoReembolso
import com.sportec.sporthub.domain.EstadoReserva
import com.sportec.sporthub.domain.EstadoSolicitud
import com.sportec.sporthub.domain.EstadoTransferencia
import com.sportec.sporthub.ui.theme.Calido
import com.sportec.sporthub.ui.theme.CalidoOscuro
import com.sportec.sporthub.ui.theme.CalidoTexto
import com.sportec.sporthub.ui.theme.GrisTenue
import com.sportec.sporthub.ui.theme.GrisTenueOscuro
import com.sportec.sporthub.ui.theme.GrisTenueTexto
import com.sportec.sporthub.ui.theme.RojoTenue
import com.sportec.sporthub.ui.theme.RojoTenueOscuro
import com.sportec.sporthub.ui.theme.RojoTenueTexto
import com.sportec.sporthub.ui.theme.RojoClaro
import com.sportec.sporthub.ui.theme.TenueOscuro
import com.sportec.sporthub.utils.formatoDuracion
import com.sportec.sporthub.utils.formatoPesos

/** Tono semántico de una etiqueta de estado, igual a .pill/.pill.amber/.pill.red/.pill.gray del prototipo. */
enum class TonoEtiqueta { VERDE, AMBAR, ROJO, GRIS }

val EstadoSolicitud.tono: TonoEtiqueta get() = when (this) {
    EstadoSolicitud.FILA, EstadoSolicitud.REVISION, EstadoSolicitud.PAGO -> TonoEtiqueta.AMBAR
    EstadoSolicitud.ATENDIDA -> TonoEtiqueta.VERDE
    EstadoSolicitud.VENCIDA, EstadoSolicitud.RECHAZADA, EstadoSolicitud.RETIRADA, EstadoSolicitud.INCOMPATIBLE -> TonoEtiqueta.ROJO
}

val EstadoReserva.tono: TonoEtiqueta get() = when (this) {
    EstadoReserva.CONFIRMADA -> TonoEtiqueta.VERDE
    EstadoReserva.CANCELADA -> TonoEtiqueta.ROJO
}

val EstadoTransferencia.tono: TonoEtiqueta get() = when (this) {
    EstadoTransferencia.PUBLICADA, EstadoTransferencia.TRAMITE -> TonoEtiqueta.AMBAR
    EstadoTransferencia.COMPLETADA -> TonoEtiqueta.VERDE
    EstadoTransferencia.RETIRADA, EstadoTransferencia.VENCIDA -> TonoEtiqueta.ROJO
}

val EstadoPostulante.tono: TonoEtiqueta get() = when (this) {
    EstadoPostulante.FILA, EstadoPostulante.REVISION, EstadoPostulante.PAGO -> TonoEtiqueta.AMBAR
    EstadoPostulante.ATENDIDA -> TonoEtiqueta.VERDE
    EstadoPostulante.VENCIDA, EstadoPostulante.INCOMPATIBLE, EstadoPostulante.RETIRADA -> TonoEtiqueta.ROJO
}

val EstadoReembolso.tono: TonoEtiqueta get() = when (this) {
    EstadoReembolso.PENDIENTE -> TonoEtiqueta.AMBAR
    EstadoReembolso.REALIZADO -> TonoEtiqueta.VERDE
    EstadoReembolso.FALLIDO -> TonoEtiqueta.ROJO
}

val EstadoNegocio.tono: TonoEtiqueta get() = when (this) {
    EstadoNegocio.VALIDADO -> TonoEtiqueta.VERDE
    EstadoNegocio.PENDIENTE -> TonoEtiqueta.AMBAR
    EstadoNegocio.RECHAZADO -> TonoEtiqueta.ROJO
}

val EstadoPago.tono: TonoEtiqueta get() = when (this) {
    EstadoPago.EXITOSO -> TonoEtiqueta.VERDE
    EstadoPago.FALLIDO -> TonoEtiqueta.ROJO
}

@Composable
fun Etiqueta(
    texto: String,
    modifier: Modifier = Modifier,
    contenedor: Color = MaterialTheme.colorScheme.primaryContainer,
    contenido: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = contenedor,
        modifier = modifier
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            color = contenido,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun Etiqueta(texto: String, tono: TonoEtiqueta, modifier: Modifier = Modifier) {
    val oscuro = isSystemInDarkTheme()
    val (contenedor, contenido) = when (tono) {
        TonoEtiqueta.VERDE -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        TonoEtiqueta.AMBAR -> (if (oscuro) CalidoOscuro else Calido) to CalidoTexto
        TonoEtiqueta.ROJO -> (if (oscuro) RojoTenueOscuro else RojoTenue) to (if (oscuro) RojoClaro else RojoTenueTexto)
        TonoEtiqueta.GRIS -> (if (oscuro) GrisTenueOscuro else GrisTenue) to (if (oscuro) TenueOscuro else GrisTenueTexto)
    }
    Etiqueta(texto = texto, modifier = modifier, contenedor = contenedor, contenido = contenido)
}

@Composable
fun TarjetaHechos(
    pares: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            pares.forEach { (etiqueta, valor) ->
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = etiqueta,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(text = valor, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
fun TarjetaActividad(
    item: ActividadConNegocio,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        IlustracionActividad(item.actividad)
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.actividad.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatoPesos(item.actividad.precio),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "${item.negocio.nombre} · ${formatoDuracion(item.actividad.duracionMinutos)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Etiqueta(texto = item.actividad.categoria)
        }
    }
}

private val VerdeTinte = Color(0xFFEDF8EB)

/** Tarjeta tenue verde, igual a .card.tint del prototipo (avisos y teasers destacados). */
@Composable
fun TarjetaTint(
    modifier: Modifier = Modifier,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Surface(
        color = VerdeTinte,
        shape = RoundedCornerShape(17.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            content = contenido
        )
    }
}
