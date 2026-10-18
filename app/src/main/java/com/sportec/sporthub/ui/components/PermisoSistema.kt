package com.sportec.sporthub.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import com.sportec.sporthub.R
import com.sportec.sporthub.domain.Ubicacion

object CambiosPermisos {
    val version = kotlinx.coroutines.flow.MutableStateFlow(0)
}

/**
 * Permiso de ubicación con Accompanist (sesión 6): si está concedido abre Ajustes;
 * si fue negado una vez muestra el rationale antes de volver a solicitar;
 * si fue negado de forma permanente abre Ajustes; si nunca se pidió, lo solicita.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermisoSistema() {
    val context = LocalContext.current
    val preferencias = remember { context.getSharedPreferences("permisos_sistema", Context.MODE_PRIVATE) }
    var solicitado by remember { mutableStateOf(preferencias.getBoolean("ubicacion", false)) }
    val permisos = rememberMultiplePermissionsState(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    ) { resultado ->
        CambiosPermisos.version.value++
        if (resultado.values.none { it }) Ubicacion.borrarPosicion()
    }
    val permitido = permisos.permissions.any { it.status.isGranted }
    val preciso = permisos.permissions.any { it.permission == Manifest.permission.ACCESS_FINE_LOCATION && it.status.isGranted }
    val mostrarRationale = permisos.shouldShowRationale
    var verRationale by remember { mutableStateOf(false) }
    LaunchedEffect(permitido, preciso) {
        CambiosPermisos.version.value++
        if (!permitido) Ubicacion.borrarPosicion()
    }
    val etiqueta = when {
        permitido && !preciso -> R.string.permiso_aproximado
        permitido -> R.string.permiso_ubicacion_activo
        solicitado && !mostrarRationale -> R.string.permiso_ajustes
        else -> R.string.permiso_pedir_ubicacion
    }
    fun solicitar() {
        solicitado = true
        preferencias.edit().putBoolean("ubicacion", true).apply()
        permisos.launchMultiplePermissionRequest()
    }
    if (verRationale) {
        AlertDialog(
            onDismissRequest = { verRationale = false },
            title = { Text(stringResource(R.string.permiso_ubicacion_titulo)) },
            text = { Text(stringResource(R.string.permiso_ubicacion_detalle)) },
            confirmButton = {
                TextButton(onClick = { verRationale = false; solicitar() }) { Text(stringResource(R.string.permiso_permitir)) }
            },
            dismissButton = {
                TextButton(onClick = { verRationale = false }) { Text(stringResource(R.string.permiso_ahora_no)) }
            }
        )
    }
    OutlinedButton(onClick = {
        when {
            permitido -> context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")))
            mostrarRationale -> verRationale = true
            solicitado -> context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")))
            else -> solicitar()
        }
    }) { Text(stringResource(etiqueta)) }
}
