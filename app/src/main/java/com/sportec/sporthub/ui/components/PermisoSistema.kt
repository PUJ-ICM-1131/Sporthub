package com.sportec.sporthub.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.sportec.sporthub.R
import com.sportec.sporthub.domain.Ubicacion

object CambiosPermisos {
    val version = kotlinx.coroutines.flow.MutableStateFlow(0)
}

/**
 * Permiso de ubicación precisa y aproximada con Accompanist (sesiones 6 y 9), usando solo el
 * diálogo del sistema. Con [solicitarAlEntrar] lo pide al abrir la pantalla. El botón aparece
 * únicamente si falta el permiso (lo vuelve a pedir o, si fue negado de forma permanente, abre
 * Ajustes) o si la ubicación del teléfono está apagada (abre su configuración).
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermisoSistema(solicitarAlEntrar: Boolean = false) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val preferencias = remember { context.getSharedPreferences("permisos_sistema", Context.MODE_PRIVATE) }
    val gestor = remember { context.getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    var solicitado by remember { mutableStateOf(preferencias.getBoolean("ubicacion", false)) }
    var pedidoAlEntrar by rememberSaveable { mutableStateOf(false) }
    var ubicacionEncendida by remember { mutableStateOf(LocationManagerCompat.isLocationEnabled(gestor)) }
    val permisos = rememberPermisosUbicacion { resultado ->
        CambiosPermisos.version.value++
        if (resultado.values.none { it }) Ubicacion.borrarPosicion()
    }
    val permitido = permisos.permissions.any { it.status.isGranted }
    val preciso = permisos.permissions.any { it.permission == Manifest.permission.ACCESS_FINE_LOCATION && it.status.isGranted }
    fun solicitar() {
        solicitado = true
        preferencias.edit().putBoolean("ubicacion", true).apply()
        permisos.launchMultiplePermissionRequest()
    }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) ubicacionEncendida = LocationManagerCompat.isLocationEnabled(gestor)
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(permitido, preciso) {
        CambiosPermisos.version.value++
        if (!permitido) Ubicacion.borrarPosicion()
    }
    LaunchedEffect(solicitarAlEntrar) {
        if (solicitarAlEntrar && !pedidoAlEntrar && !permitido) {
            pedidoAlEntrar = true
            solicitar()
        }
    }
    when {
        !permitido -> {
            val puedePedir = !solicitado || permisos.shouldShowRationale
            OutlinedButton(onClick = {
                if (puedePedir) solicitar()
                else context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")))
            }) { Text(stringResource(if (puedePedir) R.string.permiso_pedir_ubicacion else R.string.permiso_ajustes)) }
        }
        !ubicacionEncendida -> OutlinedButton(onClick = {
            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }) { Text(stringResource(R.string.permiso_ubicacion_apagada)) }
    }
}
