package com.sportec.sporthub.ui.screens.comun

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos
import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sportec.sporthub.domain.Permisos
import com.sportec.sporthub.ui.components.Aviso
import com.sportec.sporthub.ui.components.TipoAviso
import com.sportec.sporthub.ui.components.PantallaBase

@Composable
fun PermisoScreen(tipo: String, onBack: () -> Unit, onResuelto: () -> Unit) {
    val concedidos by Permisos.concedidos.collectAsState()
    val yaConcedido = tipo in concedidos

    val icono: ImageVector = when (tipo) {
        Permisos.UBICACION -> Icons.Filled.LocationOn
        Permisos.CAMARA -> Icons.Filled.CameraAlt
        Permisos.CONTACTOS -> Icons.Filled.Contacts
        else -> Icons.Filled.Notifications
    }
    val titulo = when (tipo) {
        Permisos.UBICACION -> stringResource(R.string.permiso_ubicacion_titulo)
        Permisos.CAMARA -> stringResource(R.string.permiso_camara_titulo)
        Permisos.CONTACTOS -> stringResource(R.string.permiso_contactos_titulo)
        else -> stringResource(R.string.permiso_notificaciones_titulo)
    }
    val detalle = when (tipo) {
        Permisos.UBICACION -> stringResource(R.string.permiso_ubicacion_detalle)
        Permisos.CAMARA -> stringResource(R.string.permiso_camara_detalle)
        Permisos.CONTACTOS -> stringResource(R.string.permiso_contactos_detalle)
        else -> stringResource(R.string.permiso_notificaciones_detalle)
    }

    PantallaBase(titulo = Textos.obtener(R.string.permiso_titulo), onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )
            Text(
                text = titulo,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Text(
                text = detalle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Aviso(texto = stringResource(R.string.permiso_aviso_simulado), tipo = TipoAviso.NORMAL)
            if (yaConcedido) {
                Aviso(texto = stringResource(R.string.permiso_concedido), tipo = TipoAviso.NORMAL)
            }
            Button(
                onClick = {
                    Permisos.conceder(tipo)
                    onResuelto()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.permiso_permitir))
            }
            OutlinedButton(
                onClick = {
                    Permisos.rechazar(tipo)
                    onResuelto()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.permiso_ahora_no))
            }
        }
    }
}
