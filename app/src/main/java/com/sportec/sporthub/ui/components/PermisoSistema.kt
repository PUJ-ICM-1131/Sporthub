package com.sportec.sporthub.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermisoSistema(solicitarAlEntrar: Boolean = false, exigirPrecisa: Boolean = false) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val preferencias = remember { context.getSharedPreferences("permisos_sistema", Context.MODE_PRIVATE) }
    val gestor = remember { context.getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    var solicitado by remember { mutableStateOf(preferencias.getBoolean("ubicacion", false)) }
    var precisaSolicitada by remember { mutableStateOf(preferencias.getBoolean("ubicacion_precisa", false)) }
    var pedidoAlEntrar by rememberSaveable { mutableStateOf(false) }
    var ubicacionEncendida by remember { mutableStateOf(LocationManagerCompat.isLocationEnabled(gestor)) }
    val permisos = rememberPermisosUbicacion { resultado ->
        CambiosPermisos.version.value++
        if (resultado.values.none { it }) Ubicacion.borrarPosicion()
    }
    val permitido = permisos.permissions.any { it.status.isGranted }
    val preciso = permisos.permissions.any { it.permission == Manifest.permission.ACCESS_FINE_LOCATION && it.status.isGranted }
    val suficiente = if (exigirPrecisa) preciso else permitido
    fun solicitar() {
        if (exigirPrecisa) {
            precisaSolicitada = true
            preferencias.edit().putBoolean("ubicacion_precisa", true).apply()
        }
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
    LaunchedEffect(solicitarAlEntrar, exigirPrecisa) {
        if (solicitarAlEntrar && !pedidoAlEntrar && !suficiente) {
            pedidoAlEntrar = true
            if (!solicitado || permisos.shouldShowRationale || (permitido && exigirPrecisa && !precisaSolicitada)) solicitar()
        }
    }
    if (!suficiente || !ubicacionEncendida) {
        IconButton(onClick = {
            val intent = if (suficiente && !ubicacionEncendida) Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                else Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
            context.startActivity(intent)
        }) {
            Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.ajustes_ubicacion))
        }
    }
}
