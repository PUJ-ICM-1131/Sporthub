package com.sportec.sporthub.ui.components

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted
import android.Manifest
import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.awaitCancellation

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermisoNotificacionesAlEntrar(usuarioId: String?) {
    if (usuarioId == null || Build.VERSION.SDK_INT < 33) return
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val permiso = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS) {
        CambiosPermisos.version.value++
    }
    LaunchedEffect(permiso.status.isGranted) { CambiosPermisos.version.value++ }
    LaunchedEffect(usuarioId, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            val preferencias = context.getSharedPreferences("permisos_sistema", Context.MODE_PRIVATE)
            val permitido = permiso.status.isGranted
            if (!permitido && !preferencias.getBoolean("notificaciones", false)) {
                preferencias.edit().putBoolean("notificaciones", true).apply()
                permiso.launchPermissionRequest()
            }
            CambiosPermisos.version.value++
            awaitCancellation()
        }
    }
}
