package com.sportec.sporthub.utils

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

data class Coordenada(val latitud: Double, val longitud: Double)

object Geo {
    private const val RADIO_TIERRA_M = 6371008.8

    fun distanciaMetros(a: Coordenada, b: Coordenada): Double {
        val dLat = Math.toRadians(b.latitud - a.latitud)
        val dLng = Math.toRadians(b.longitud - a.longitud)
        val h = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(a.latitud)) * cos(Math.toRadians(b.latitud)) * sin(dLng / 2) * sin(dLng / 2)
        return 2 * RADIO_TIERRA_M * asin(sqrt(h.coerceIn(0.0, 1.0)))
    }

    fun distanciaAlTrazado(punto: Coordenada, trazado: List<Coordenada>): Double {
        if (trazado.isEmpty()) return Double.POSITIVE_INFINITY
        if (trazado.size == 1) return distanciaMetros(punto, trazado.first())
        return trazado.zipWithNext().minOf { (a, b) -> distanciaAlSegmento(punto, a, b) }
    }

    fun longitudMetros(trazado: List<Coordenada>): Double =
        trazado.zipWithNext().sumOf { (a, b) -> distanciaMetros(a, b) }

    fun restanteMetros(punto: Coordenada, trazado: List<Coordenada>): Double {
        if (trazado.size < 2) return trazado.firstOrNull()?.let { distanciaMetros(punto, it) } ?: 0.0
        val tramos = trazado.zipWithNext()
        val indice = tramos.indices.minBy { distanciaAlSegmento(punto, tramos[it].first, tramos[it].second) }
        return distanciaMetros(punto, tramos[indice].second) + longitudMetros(trazado.drop(indice + 1))
    }

    fun circulo(centro: Coordenada, radioKm: Double, lados: Int = 64): List<Coordenada> {
        val dLat = radioKm / 111.32
        val dLng = radioKm / (111.32 * max(cos(Math.toRadians(centro.latitud)), 0.01))
        val puntos = (0 until lados).map {
            val angulo = 2 * Math.PI * it / lados
            Coordenada(centro.latitud + dLat * sin(angulo), centro.longitud + dLng * cos(angulo))
        }
        return puntos + puntos.first()
    }

    fun encuadre(puntos: List<Coordenada>): Pair<Coordenada, Double> {
        val minLat = puntos.minOf { it.latitud }
        val maxLat = puntos.maxOf { it.latitud }
        val minLng = puntos.minOf { it.longitud }
        val maxLng = puntos.maxOf { it.longitud }
        val centro = Coordenada((minLat + maxLat) / 2, (minLng + maxLng) / 2)
        val extension = max(max(maxLat - minLat, (maxLng - minLng) * cos(Math.toRadians(centro.latitud))), 0.002)
        val zoom = ln(360.0 / extension) / ln(2.0) - 1.6
        return centro to zoom.coerceIn(3.0, 16.0)
    }

    private fun distanciaAlSegmento(p: Coordenada, a: Coordenada, b: Coordenada): Double {
        val escala = cos(Math.toRadians(p.latitud))
        val ax = (a.longitud - p.longitud) * escala
        val ay = a.latitud - p.latitud
        val bx = (b.longitud - p.longitud) * escala
        val by = b.latitud - p.latitud
        val dx = bx - ax
        val dy = by - ay
        val largo = dx * dx + dy * dy
        val t = if (largo == 0.0) 0.0 else (-(ax * dx + ay * dy) / largo).coerceIn(0.0, 1.0)
        val cercano = Coordenada(a.latitud + (b.latitud - a.latitud) * t, a.longitud + (b.longitud - a.longitud) * t)
        return distanciaMetros(p, cercano)
    }
}
