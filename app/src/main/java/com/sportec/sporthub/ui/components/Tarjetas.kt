package com.sportec.sporthub.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.sportec.sporthub.data.model.ActividadConNegocio
import com.sportec.sporthub.utils.formatoDuracion
import com.sportec.sporthub.utils.formatoPesos

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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Etiqueta(texto = item.actividad.categoria)
                Etiqueta(
                    texto = item.actividad.tipo.etiqueta,
                    contenedor = MaterialTheme.colorScheme.secondaryContainer,
                    contenido = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}
