package com.sportec.sporthub.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.sportec.sporthub.utils.RastreadorUbicacion

/** Mantiene la posición actual al día mientras la pantalla que lo incluye está visible. */
@Composable
fun SeguimientoUbicacion() {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val rastreador = remember { RastreadorUbicacion(context.applicationContext) }
    val versionPermisos by CambiosPermisos.version.collectAsState()

    DisposableEffect(lifecycle, versionPermisos) {
        val observer = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) rastreador.iniciar()
            if (evento == Lifecycle.Event.ON_PAUSE) rastreador.detener()
        }
        lifecycle.addObserver(observer)
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) rastreador.iniciar()
        onDispose {
            lifecycle.removeObserver(observer)
            rastreador.detener()
        }
    }
}
