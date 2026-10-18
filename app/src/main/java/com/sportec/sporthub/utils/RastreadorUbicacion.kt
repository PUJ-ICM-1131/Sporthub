package com.sportec.sporthub.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.sportec.sporthub.domain.Ubicacion

class RastreadorUbicacion(private val context: Context) {
    private val gestor = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var activo = false

    private val oyente = LocationListener { publicar(it) }

    fun tienePermiso(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun iniciar() {
        if (activo || !tienePermiso()) return
        val proveedores = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { runCatching { gestor.isProviderEnabled(it) }.getOrDefault(false) }
        proveedores.mapNotNull { runCatching { gestor.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }
            ?.let { publicar(it) }
        proveedores.forEach {
            runCatching { gestor.requestLocationUpdates(it, INTERVALO_MS, DISTANCIA_MINIMA_M, oyente, Looper.getMainLooper()) }
        }
        activo = proveedores.isNotEmpty()
    }

    fun detener() {
        if (!activo) return
        runCatching { gestor.removeUpdates(oyente) }
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
