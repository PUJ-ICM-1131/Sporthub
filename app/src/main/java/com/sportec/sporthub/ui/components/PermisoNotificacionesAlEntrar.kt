package com.sportec.sporthub.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.awaitCancellation

@Composable
fun PermisoNotificacionesAlEntrar(usuarioId: String?) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        CambiosPermisos.version.value++
    }
    LaunchedEffect(usuarioId, lifecycle) {
        if (usuarioId == null || Build.VERSION.SDK_INT < 33) return@LaunchedEffect
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            val preferencias = context.getSharedPreferences("permisos_sistema", Context.MODE_PRIVATE)
            val permitido = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            if (!permitido && !preferencias.getBoolean("notificaciones", false)) {
                preferencias.edit().putBoolean("notificaciones", true).apply()
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            CambiosPermisos.version.value++
            awaitCancellation()
        }
    }
}
