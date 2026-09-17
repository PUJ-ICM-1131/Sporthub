package com.sportec.sporthub.ui.screens.deportista

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportec.sporthub.domain.Negocios
import com.sportec.sporthub.domain.Ubicacion
import com.sportec.sporthub.ui.components.EstadoError
import com.sportec.sporthub.ui.components.PantallaBase

@Composable
fun BrujulaScreen(negocioId: String, onBack: () -> Unit) {
    val negocio = Negocios.negocioDe(negocioId)
    val origen by Ubicacion.origen.collectAsState()
    val context = LocalContext.current
    var azimut by remember { mutableFloatStateOf(0f) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometro = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        val lecturaAcel = FloatArray(3)
        val lecturaMag = FloatArray(3)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> event.values.copyInto(lecturaAcel)
                    Sensor.TYPE_MAGNETIC_FIELD -> event.values.copyInto(lecturaMag)
                }
                val matrizR = FloatArray(9)
                val orientacion = FloatArray(3)
                if (SensorManager.getRotationMatrix(matrizR, null, lecturaAcel, lecturaMag)) {
                    SensorManager.getOrientation(matrizR, orientacion)
                    azimut = (Math.toDegrees(orientacion[0].toDouble()).toFloat() + 360) % 360
                }
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
        }
        if (acelerometro != null && magnetometro != null) {
            sensorManager.registerListener(listener, acelerometro, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(listener, magnetometro, SensorManager.SENSOR_DELAY_UI)
        }
        onDispose { sensorManager.unregisterListener(listener) }
    }

    PantallaBase(titulo = "Cómo llegar", onBack = onBack) { padding ->
        if (negocio == null) {
            EstadoError(mensaje = "El establecimiento ya no existe.", onReintentar = {}, modifier = Modifier.padding(padding))
        } else {
            val distancia = Ubicacion.distanciaKm(origen, negocio.latitud, negocio.longitud)
            val rumbo = Ubicacion.rumboGrados(origen, negocio.latitud, negocio.longitud)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(negocio.nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = "%.1f km desde %s".format(distancia, origen.nombre),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = Icons.Filled.Navigation,
                    contentDescription = "Dirección hacia el establecimiento",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(160.dp)
                        .graphicsLayer { rotationZ = (rumbo - azimut).toFloat() }
                )
                Text(
                    text = "La flecha gira con tu brújula y apunta hacia el establecimiento.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
