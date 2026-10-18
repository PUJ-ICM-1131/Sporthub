package com.sportec.sporthub.ui.components

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.sportec.sporthub.utils.RastreadorUbicacion

/** Estado de los permisos de ubicación (precisa y aproximada) con Accompanist. */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberPermisosUbicacion(onResultado: (Map<String, Boolean>) -> Unit = {}): MultiplePermissionsState =
    rememberMultiplePermissionsState(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
        onResultado
    )

/**
 * Mantiene la posición actual al día mientras la pantalla está visible (sesión 9): solo inicia
 * cuando Accompanist indica que hay permiso, y se suscribe al reanudar y se cancela al pausar o salir.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SeguimientoUbicacion() {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val rastreador = remember { RastreadorUbicacion(context.applicationContext) }
    val permisos = rememberPermisosUbicacion()
    val permitido = permisos.permissions.any { it.status.isGranted }

    DisposableEffect(lifecycle, permitido) {
        val observer = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME && permitido) rastreador.iniciar()
            if (evento == Lifecycle.Event.ON_PAUSE) rastreador.detener()
        }
        lifecycle.addObserver(observer)
        if (permitido && lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) rastreador.iniciar()
        onDispose {
            lifecycle.removeObserver(observer)
            rastreador.detener()
        }
    }
}
