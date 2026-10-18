package com.sportec.sporthub.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.NotificationManagerCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.sportec.sporthub.R

private const val CLAVE_SOLICITADO = "notificaciones"

/**
 * Primera solicitud del permiso de notificaciones al iniciar sesión (paso 4 de Accompanist, sesión 6).
 * Solo se lanza si nunca se ha pedido; los pasos siguientes quedan en [PermisoNotificaciones].
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermisoNotificacionesAlEntrar(usuarioId: String?) {
    if (usuarioId == null || Build.VERSION.SDK_INT < 33) return
    val context = LocalContext.current
    val permiso = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS) {
        CambiosPermisos.version.value++
    }
    LaunchedEffect(permiso.status.isGranted) { CambiosPermisos.version.value++ }
    LaunchedEffect(usuarioId) {
        val preferencias = context.getSharedPreferences("permisos_sistema", Context.MODE_PRIVATE)
        if (!permiso.status.isGranted && !preferencias.getBoolean(CLAVE_SOLICITADO, false)) {
            preferencias.edit().putBoolean(CLAVE_SOLICITADO, true).apply()
            permiso.launchPermissionRequest()
        }
    }
}

/**
 * Botón del permiso de notificaciones con los 4 pasos de Accompanist (sesión 6):
 * concedido abre Ajustes, negado una vez muestra el rationale, negado de forma permanente
 * abre Ajustes y, si nunca se pidió, lo solicita. Antes de Android 13 no hay permiso en
 * ejecución y solo se revisa si las notificaciones están activas.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermisoNotificaciones() {
    val context = LocalContext.current
    val preferencias = remember { context.getSharedPreferences("permisos_sistema", Context.MODE_PRIVATE) }
    val abrirAjustes = {
        context.startActivity(
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        )
    }
    if (Build.VERSION.SDK_INT < 33) {
        val activas = NotificationManagerCompat.from(context).areNotificationsEnabled()
        OutlinedButton(onClick = abrirAjustes) {
            Text(stringResource(if (activas) R.string.permiso_notificaciones_activo else R.string.permiso_notificaciones_ajustes))
        }
        return
    }
    var solicitado by remember { mutableStateOf(preferencias.getBoolean(CLAVE_SOLICITADO, false)) }
    var verRationale by remember { mutableStateOf(false) }
    val permiso = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS) {
        CambiosPermisos.version.value++
    }
    val concedido = permiso.status.isGranted
    val mostrarRationale = permiso.status.shouldShowRationale
    fun solicitar() {
        solicitado = true
        preferencias.edit().putBoolean(CLAVE_SOLICITADO, true).apply()
        permiso.launchPermissionRequest()
    }
    if (verRationale) {
        AlertDialog(
            onDismissRequest = { verRationale = false },
            title = { Text(stringResource(R.string.permiso_notificaciones_titulo)) },
            text = { Text(stringResource(R.string.permiso_notificaciones_detalle)) },
            confirmButton = {
                TextButton(onClick = { verRationale = false; solicitar() }) { Text(stringResource(R.string.permiso_permitir)) }
            },
            dismissButton = {
                TextButton(onClick = { verRationale = false }) { Text(stringResource(R.string.permiso_ahora_no)) }
            }
        )
    }
    val etiqueta = when {
        concedido -> R.string.permiso_notificaciones_activo
        solicitado && !mostrarRationale -> R.string.permiso_notificaciones_ajustes
        else -> R.string.permiso_pedir_notificaciones
    }
    OutlinedButton(onClick = {
        when {
            concedido -> abrirAjustes()
            mostrarRationale -> verRationale = true
            solicitado -> abrirAjustes()
            else -> solicitar()
        }
    }) { Text(stringResource(etiqueta)) }
}
