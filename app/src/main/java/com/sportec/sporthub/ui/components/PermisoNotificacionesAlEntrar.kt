package com.sportec.sporthub.ui.components

import androidx.compose.material.icons.filled.Settings
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

@Composable
fun PermisoNotificaciones() {
    val context = LocalContext.current
    androidx.compose.material3.SmallFloatingActionButton(onClick = {
        context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName))
    }) {
        androidx.compose.material3.Icon(
            androidx.compose.material.icons.Icons.Filled.Settings,
            contentDescription = stringResource(R.string.ajustes_notificaciones)
        )
    }
}
