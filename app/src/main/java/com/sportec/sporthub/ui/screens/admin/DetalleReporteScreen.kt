package com.sportec.sporthub.ui.screens.admin

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sportec.sporthub.domain.Cuentas
import com.sportec.sporthub.domain.EstadoReporte
import com.sportec.sporthub.domain.Reportes
import com.sportec.sporthub.ui.components.Aviso
import com.sportec.sporthub.ui.components.EstadoError
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.TarjetaHechos
import com.sportec.sporthub.ui.components.TipoAviso

@Composable
fun DetalleReporteScreen(reporteId: String, onBack: () -> Unit) {
    val reportes by Reportes.reportes.collectAsState()
    val reporte = reportes.firstOrNull { it.id == reporteId }

    PantallaBase(titulo = "Detalle del reporte", onBack = onBack) { padding ->
        if (reporte == null) {
            EstadoError(mensaje = "El reporte ya no existe.", onReintentar = {}, modifier = Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(reporte.objetivo, style = MaterialTheme.typography.titleLarge)
                TarjetaHechos(
                    pares = buildList {
                        add("Motivo" to reporte.motivo)
                        add("Reportado por" to (Cuentas.porId(reporte.autorId)?.nombre ?: reporte.autorId))
                        if (reporte.contexto.isNotBlank()) add("Contexto" to reporte.contexto)
                    }
                )
                if (reporte.estado == EstadoReporte.PENDIENTE) {
                    Button(onClick = { Reportes.resolver(reporteId); onBack() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Marcar como resuelto")
                    }
                } else {
                    Aviso(texto = "Este reporte ya fue resuelto.", tipo = TipoAviso.NORMAL)
                }
            }
        }
    }
}
