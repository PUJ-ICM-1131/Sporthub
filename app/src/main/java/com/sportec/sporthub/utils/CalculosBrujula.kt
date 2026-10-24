package com.sportec.sporthub.utils

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object CalculosBrujula {
    fun normalizar(grados: Float): Float = ((grados % 360f) + 360f) % 360f

    fun diferencia(destino: Float, actual: Float): Float = normalizar(destino - actual + 180f) - 180f

    fun suavizar(anterior: Float, nuevo: Float): Float = normalizar(anterior + diferencia(nuevo, anterior) * 0.2f)

    fun rumbo(latitud: Double, longitud: Double, destinoLatitud: Double, destinoLongitud: Double): Float {
        val lat1 = Math.toRadians(latitud)
        val lat2 = Math.toRadians(destinoLatitud)
        val delta = Math.toRadians(destinoLongitud - longitud)
        val y = sin(delta) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(delta)
        return normalizar(Math.toDegrees(atan2(y, x)).toFloat())
    }
}
