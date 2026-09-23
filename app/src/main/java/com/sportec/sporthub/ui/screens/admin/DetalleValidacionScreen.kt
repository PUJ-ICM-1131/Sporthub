package com.sportec.sporthub.ui.screens.admin

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos
import androidx.compose.ui.res.stringResource

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

    PantallaBase(titulo = Textos.obtener(R.string.texto_validacion_de_negocio), onBack = onBack) { padding ->
        if (negocio == null) {
            EstadoError(mensaje = Textos.obtener(R.string.texto_el_negocio_ya_no_existe), onReintentar = {}, modifier = Modifier.padding(padding))
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
                        Textos.obtener(R.string.texto_nit) to negocio.nit,
                        Textos.obtener(R.string.texto_contacto) to negocio.contacto,
                        Textos.obtener(R.string.texto_direccion) to negocio.direccion,
                        Textos.obtener(R.string.texto_descripcion) to negocio.descripcion.ifBlank { Textos.obtener(R.string.texto_sin_descripcion) }
                    )
                )
                if (negocio.estado == EstadoNegocio.PENDIENTE) {
                    Button(onClick = { Negocios.validar(negocioId, aprobado = true); onBack() }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.texto_aprobar_negocio))
                    }
                    OutlinedButton(
                        onClick = { Negocios.validar(negocioId, aprobado = false); onBack() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.texto_rechazar_negocio))
                    }
                } else {
                    Aviso(texto = Textos.obtener(R.string.texto_este_negocio_ya_fue_revisado_valor, negocio.estado.etiqueta), tipo = TipoAviso.NORMAL)
                }
            }
        }
    }
}
