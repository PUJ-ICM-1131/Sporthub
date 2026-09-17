package com.sportec.sporthub.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sportec.sporthub.domain.EstadoNegocio
import com.sportec.sporthub.domain.Negocios
import com.sportec.sporthub.ui.components.Aviso
import com.sportec.sporthub.ui.components.EstadoError
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.TarjetaHechos
import com.sportec.sporthub.ui.components.TipoAviso

@Composable
fun DetalleValidacionScreen(negocioId: String, onBack: () -> Unit) {
    val negocios by Negocios.negocios.collectAsState()
    val negocio = negocios.firstOrNull { it.id == negocioId }

    PantallaBase(titulo = "Validación de negocio", onBack = onBack) { padding ->
        if (negocio == null) {
            EstadoError(mensaje = "El negocio ya no existe.", onReintentar = {}, modifier = Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(negocio.nombre, style = MaterialTheme.typography.titleLarge)
                TarjetaHechos(
                    pares = listOf(
                        "NIT" to negocio.nit,
                        "Contacto" to negocio.contacto,
                        "Dirección" to negocio.direccion,
                        "Descripción" to negocio.descripcion.ifBlank { "Sin descripción" }
                    )
                )
                if (negocio.estado == EstadoNegocio.PENDIENTE) {
                    Button(onClick = { Negocios.validar(negocioId, aprobado = true); onBack() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Aprobar negocio")
                    }
                    OutlinedButton(
                        onClick = { Negocios.validar(negocioId, aprobado = false); onBack() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Rechazar negocio")
                    }
                } else {
                    Aviso(texto = "Este negocio ya fue revisado: ${negocio.estado.etiqueta}.", tipo = TipoAviso.NORMAL)
                }
            }
        }
    }
}
