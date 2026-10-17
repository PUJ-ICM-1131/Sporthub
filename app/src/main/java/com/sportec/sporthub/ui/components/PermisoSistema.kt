package com.sportec.sporthub.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.sportec.sporthub.R
import com.sportec.sporthub.domain.Ubicacion

object CambiosPermisos {
    val version = kotlinx.coroutines.flow.MutableStateFlow(0)
}

@Composable
fun PermisoSistema(ubicacion: Boolean = false) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val preferencias = remember { context.getSharedPreferences("permisos_sistema", Context.MODE_PRIVATE) }
    val clave = if (ubicacion) "ubicacion" else "notificaciones"
    var revision by remember { mutableIntStateOf(0) }
    var solicitado by remember { mutableStateOf(preferencias.getBoolean(clave, false)) }
    fun tiene(permiso: String) = ContextCompat.checkSelfPermission(context, permiso) == PackageManager.PERMISSION_GRANTED
    val permitido = remember(revision, ubicacion) {
        if (ubicacion) tiene(Manifest.permission.ACCESS_FINE_LOCATION) || tiene(Manifest.permission.ACCESS_COARSE_LOCATION)
        else NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
    val preciso = remember(revision) { tiene(Manifest.permission.ACCESS_FINE_LOCATION) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        revision++
        CambiosPermisos.version.value++
        if (ubicacion && !tiene(Manifest.permission.ACCESS_FINE_LOCATION) && !tiene(Manifest.permission.ACCESS_COARSE_LOCATION)) Ubicacion.borrarPosicion()
    }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) {
                revision++
                CambiosPermisos.version.value++
                if (ubicacion && !tiene(Manifest.permission.ACCESS_FINE_LOCATION) && !tiene(Manifest.permission.ACCESS_COARSE_LOCATION)) Ubicacion.borrarPosicion()
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    val etiqueta = when {
        permitido && ubicacion && !preciso -> R.string.permiso_aproximado
        permitido && ubicacion -> R.string.permiso_ubicacion_activo
        permitido -> R.string.permiso_notificaciones_activo
        solicitado -> R.string.permiso_ajustes
        ubicacion -> R.string.permiso_pedir_ubicacion
        else -> R.string.permiso_pedir_notificaciones
    }
    OutlinedButton(onClick = {
        if (permitido || solicitado || (!ubicacion && Build.VERSION.SDK_INT < 33)) {
            context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")))
        } else {
            solicitado = true
            preferencias.edit().putBoolean(clave, true).apply()
            val permisos = if (ubicacion) arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                else arrayOf(Manifest.permission.POST_NOTIFICATIONS)
            launcher.launch(permisos)
        }
    }) { Text(stringResource(etiqueta)) }
}
