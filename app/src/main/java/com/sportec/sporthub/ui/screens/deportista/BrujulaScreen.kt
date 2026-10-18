package com.sportec.sporthub.ui.screens.deportista

import android.hardware.GeomagneticField
import android.view.Surface
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import com.sportec.sporthub.R
import com.sportec.sporthub.domain.Negocios
import com.sportec.sporthub.domain.Ubicacion
import com.sportec.sporthub.domain.Origen
import com.sportec.sporthub.ui.components.Aviso
import com.sportec.sporthub.ui.components.EstadoVacio
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.TipoAviso
import com.sportec.sporthub.utils.CalculosBrujula
import com.sportec.sporthub.utils.SensoresBrujula

@Composable
fun BrujulaScreen(negocioId: String, onBack: () -> Unit) {
    var ayuda by remember { mutableStateOf(false) }
    val negocio = Negocios.negocioDe(negocioId)
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val vista = LocalView.current
    var ahora by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val sensores = remember { SensoresBrujula(context.applicationContext) }
    val estado by sensores.estado.collectAsState()
    val posicion by Ubicacion.posicionActual.collectAsState()
    val actual = posicion?.takeIf { it.esFiable(ahora) }
    val origen = actual?.let { Origen(stringResource(R.string.brujula_posicion_actual), it.latitud, it.longitud) }

    DisposableEffect(lifecycle, vista, negocioId) {
        fun iniciar() { if (negocio != null) sensores.iniciar(vista.display?.rotation ?: Surface.ROTATION_0) }
        val observer = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) iniciar()
            if (evento == Lifecycle.Event.ON_PAUSE) sensores.detener()
        }
        lifecycle.addObserver(observer)
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) iniciar()
        onDispose { lifecycle.removeObserver(observer); sensores.detener() }
    }
    LaunchedEffect(sensores, lifecycle, vista) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                ahora = System.currentTimeMillis()
                sensores.actualizarRotacion(vista.display?.rotation ?: Surface.ROTATION_0)
                sensores.revisarVigencia()
                delay(500)
            }
        }
    }
    if (ayuda) AlertDialog(onDismissRequest = { ayuda = false }, title = { Text(stringResource(R.string.brujula_ayuda_titulo)) },
        text = { Column {
            Text(stringResource(R.string.brujula_uso))
            Text(stringResource(R.string.brujula_acelerometro, stringResource(if (estado.acelerometroDisponible) R.string.brujula_disponible else R.string.brujula_no_disponible)))
            Text(stringResource(R.string.brujula_magnetometro, stringResource(if (estado.magnetometroDisponible) R.string.brujula_disponible else R.string.brujula_no_disponible)))
        } }, confirmButton = { TextButton(onClick = { ayuda = false }) { Text(stringResource(R.string.selector_foto_aceptar)) } })
    PantallaBase(titulo = stringResource(R.string.texto_brujula), onBack = onBack) { padding ->
        if (negocio == null) {
            EstadoVacio(mensaje = stringResource(R.string.texto_el_establecimiento_ya_no_existe), modifier = Modifier.padding(padding))
        } else {
            val distancia = origen?.let { Ubicacion.distanciaKm(it, negocio.latitud, negocio.longitud) }
            val rumbo = origen?.let { CalculosBrujula.rumbo(it.latitud, it.longitud, negocio.latitud, negocio.longitud) }
            val declinacion = origen?.let { GeomagneticField(it.latitud.toFloat(), it.longitud.toFloat(), 0f, ahora).declination } ?: 0f
            val azimut = estado.azimut?.let { CalculosBrujula.normalizar(it + declinacion) }
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(negocio.nombre, style = MaterialTheme.typography.titleLarge)
                if (origen != null && distancia != null) Text(stringResource(R.string.formato_distancia_origen).format(distancia, origen.nombre))
                if (actual == null) Aviso(texto = stringResource(R.string.brujula_sin_posicion_fiable), tipo = TipoAviso.NORMAL)
                when {
                    !estado.acelerometroDisponible || !estado.magnetometroDisponible -> Aviso(texto = stringResource(R.string.brujula_sin_sensores), tipo = TipoAviso.ADVERTENCIA)
                    !estado.lecturaValida || azimut == null -> Aviso(texto = stringResource(R.string.brujula_sin_lectura), tipo = TipoAviso.NORMAL)
                    estado.precisionBaja -> Aviso(texto = stringResource(R.string.brujula_precision_baja), tipo = TipoAviso.ADVERTENCIA)
                    actual == null || rumbo == null || distancia == null -> {
                        Icon(Icons.Filled.Navigation, contentDescription = stringResource(R.string.brujula_norte), tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(180.dp).graphicsLayer { rotationZ = -azimut })
                        Text(stringResource(R.string.brujula_orientacion_magnetica, azimut.toInt()))
                    }
                    distancia * 1000 <= maxOf(10f, actual.precisionMetros ?: 50f) -> Text(stringResource(R.string.brujula_destino_incierto))
                    else -> {
                        Icon(Icons.Filled.Navigation, contentDescription = stringResource(R.string.texto_direccion_hacia_el_establecimiento), tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(160.dp).graphicsLayer { rotationZ = CalculosBrujula.diferencia(rumbo, azimut) })
                        Text(stringResource(R.string.brujula_orientacion_real, azimut.toInt()))
                        Text(stringResource(R.string.brujula_rumbo, rumbo.toInt()))
                    }
                }
                com.sportec.sporthub.ui.components.PermisoSistema(ubicacion = true)
                TextButton(onClick = { ayuda = true }) { Text(stringResource(R.string.brujula_ayuda_titulo)) }
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.brujula_volver_ruta)) }
            }
        }
    }
}
