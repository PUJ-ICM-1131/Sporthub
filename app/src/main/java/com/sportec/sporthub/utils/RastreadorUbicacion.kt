package com.sportec.sporthub.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.sportec.sporthub.domain.Ubicacion

class RastreadorUbicacion(context: Context) {
    private val cliente = LocationServices.getFusedLocationProviderClient(context)
    private var activo = false

    private val solicitud = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, INTERVALO_MS)
        .setMinUpdateDistanceMeters(DISTANCIA_MINIMA_M)
        .build()

    private val callback = object : LocationCallback() {
        override fun onLocationResult(resultado: LocationResult) {
            resultado.lastLocation?.let { publicar(it) }
        }
    }

    @SuppressLint("MissingPermission")
    fun iniciar() {
        if (activo) return
        try {
            cliente.lastLocation.addOnSuccessListener { it?.let(::publicar) }
            cliente.requestLocationUpdates(solicitud, callback, Looper.getMainLooper())
            activo = true
        } catch (_: SecurityException) {
            activo = false
        }
    }

    fun detener() {
        if (!activo) return
        cliente.removeLocationUpdates(callback)
        activo = false
    }

    private fun publicar(lugar: Location) {
        Ubicacion.actualizarPosicion(
            latitud = lugar.latitude,
            longitud = lugar.longitude,
            instante = lugar.time.coerceAtMost(System.currentTimeMillis()),
            precisionMetros = if (lugar.hasAccuracy()) lugar.accuracy else null
        )
    }

    private companion object {
        const val INTERVALO_MS = 2000L
        const val DISTANCIA_MINIMA_M = 3f
    }
}
