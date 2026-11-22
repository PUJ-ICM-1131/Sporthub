package com.sportec.sporthub.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.sportec.sporthub.domain.Ubicacion

class RastreadorUbicacion(context: Context) {
    private val cliente = LocationServices.getFusedLocationProviderClient(context)
    private var activo = false

    private val solicitud = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, INTERVALO_MS)
        .setMinUpdateDistanceMeters(0f)
        .setWaitForAccurateLocation(false)
        .build()

    private val callback = object : LocationCallback() {
        override fun onLocationResult(resultado: LocationResult) {
            resultado.lastLocation?.let { publicar(it) }
        }
    }

    private val manejador = Handler(Looper.getMainLooper())

    private val refresco = object : Runnable {
        @SuppressLint("MissingPermission")
        override fun run() {
            try {
                cliente.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                    .addOnSuccessListener { lugar -> lugar?.let(::publicar) }
            } catch (_: SecurityException) {
            }
            manejador.postDelayed(this, REFRESCO_MS)
        }
    }

    @SuppressLint("MissingPermission")
    fun iniciar() {
        if (activo) return
        try {
            cliente.lastLocation.addOnSuccessListener { it?.let(::publicar) }
            cliente.requestLocationUpdates(solicitud, callback, Looper.getMainLooper())
            manejador.post(refresco)
            activo = true
        } catch (_: SecurityException) {
            activo = false
        }
    }

    fun detener() {
        if (!activo) return
        cliente.removeLocationUpdates(callback)
        manejador.removeCallbacks(refresco)
        activo = false
    }

    private fun publicar(lugar: Location) {
        val ahora = System.currentTimeMillis()
        val edadMs = ((SystemClock.elapsedRealtimeNanos() - lugar.elapsedRealtimeNanos) / 1_000_000L).coerceAtLeast(0L)
        val instante = if (lugar.elapsedRealtimeNanos > 0L) ahora - edadMs else lugar.time.coerceAtMost(ahora)
        Ubicacion.actualizarPosicion(
            latitud = lugar.latitude,
            longitud = lugar.longitude,
            instante = instante.coerceAtMost(ahora),
            precisionMetros = if (lugar.hasAccuracy()) lugar.accuracy else null
        )
    }

    private companion object {
        const val INTERVALO_MS = 2000L
        const val REFRESCO_MS = 20_000L
    }
}
