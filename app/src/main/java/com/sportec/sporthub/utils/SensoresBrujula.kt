package com.sportec.sporthub.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import android.view.Surface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class EstadoSensores(
    val acelerometroDisponible: Boolean,
    val magnetometroDisponible: Boolean,
    val activo: Boolean = false,
    val azimut: Float? = null,
    val precisionBaja: Boolean = false,
    val lecturaValida: Boolean = false
)

class SensoresBrujula(context: Context) : SensorEventListener {
    private val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val acelerometro = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometro = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private val _estado = MutableStateFlow(EstadoSensores(acelerometro != null, magnetometro != null))
    val estado = _estado.asStateFlow()
    private val gravedad = FloatArray(3)
    private val campo = FloatArray(3)
    private var tiempoAceleracion = 0L
    private var tiempoMagnetico = 0L
    private var rotacion = Surface.ROTATION_0

    fun iniciar(rotacionPantalla: Int) {
        detener()
        rotacion = rotacionPantalla
        tiempoAceleracion = 0L
        tiempoMagnetico = 0L
        val tieneA = acelerometro?.let { manager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) } ?: false
        val tieneM = magnetometro?.let { manager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) } ?: false
        if (!tieneA || !tieneM) manager.unregisterListener(this)
        _estado.value = EstadoSensores(tieneA, tieneM, activo = tieneA && tieneM)
    }

    fun actualizarRotacion(nueva: Int) {
        if (rotacion != nueva) {
            rotacion = nueva
            _estado.value = _estado.value.copy(azimut = null, lecturaValida = false)
        }
    }

    fun detener() {
        manager.unregisterListener(this)
        _estado.value = _estado.value.copy(activo = false, azimut = null, lecturaValida = false)
    }

    fun revisarVigencia() {
        val ahora = SystemClock.elapsedRealtime()
        if (ahora - tiempoAceleracion > 2000 || ahora - tiempoMagnetico > 2000) {
            _estado.value = _estado.value.copy(lecturaValida = false, azimut = null)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (!_estado.value.activo) return
        if (event.values.size < 3 || event.values.take(3).any { !it.isFinite() }) {
            tiempoAceleracion = 0L
            tiempoMagnetico = 0L
            _estado.value = _estado.value.copy(lecturaValida = false, azimut = null)
            return
        }
        val ahora = SystemClock.elapsedRealtime()
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                for (i in 0..2) gravedad[i] = if (tiempoAceleracion == 0L) event.values[i] else gravedad[i] * 0.8f + event.values[i] * 0.2f
                tiempoAceleracion = ahora
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                event.values.copyInto(campo, endIndex = 3)
                tiempoMagnetico = ahora
                _estado.value = _estado.value.copy(precisionBaja = event.accuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW)
            }
        }
        if (tiempoAceleracion == 0L || tiempoMagnetico == 0L) return
        val matriz = FloatArray(9)
        if (!SensorManager.getRotationMatrix(matriz, null, gravedad, campo)) {
            _estado.value = _estado.value.copy(lecturaValida = false, azimut = null)
            return
        }
        val ejes = when (rotacion) {
            Surface.ROTATION_90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
            Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
            Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
            else -> SensorManager.AXIS_X to SensorManager.AXIS_Y
        }
        val ajustada = FloatArray(9)
        if (!SensorManager.remapCoordinateSystem(matriz, ejes.first, ejes.second, ajustada)) return
        val angulos = FloatArray(3)
        SensorManager.getOrientation(ajustada, angulos)
        val nuevo = CalculosBrujula.normalizar(Math.toDegrees(angulos[0].toDouble()).toFloat())
        val anterior = _estado.value.azimut
        _estado.value = _estado.value.copy(azimut = if (anterior == null) nuevo else CalculosBrujula.suavizar(anterior, nuevo), lecturaValida = true)
        revisarVigencia()
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        if (sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            _estado.value = _estado.value.copy(precisionBaja = accuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW)
        }
    }
}
