package com.sportec.sporthub.utils

import java.time.LocalDate
import java.time.ZoneId

data class Intervalo(val fecha: String, val hora: Int, val duracionHoras: Int)

object LogicaReservas {

    val ZONA: ZoneId = ZoneId.of("America/Bogota")

    fun inicioMillis(fecha: String, hora: Int): Long =
        LocalDate.parse(fecha).atTime(hora, 0).atZone(ZONA).toInstant().toEpochMilli()

    fun finMillis(fecha: String, hora: Int, duracionHoras: Int): Long =
        inicioMillis(fecha, hora) + duracionHoras * 3_600_000L

    fun inicioMillis(intervalo: Intervalo): Long = inicioMillis(intervalo.fecha, intervalo.hora)

    fun finMillis(intervalo: Intervalo): Long = finMillis(intervalo.fecha, intervalo.hora, intervalo.duracionHoras)

    fun seCruzan(uno: Intervalo, otro: Intervalo): Boolean =
        inicioMillis(uno) < finMillis(otro) && inicioMillis(otro) < finMillis(uno)

    fun hayCruce(compromisos: List<Intervalo>, intervalo: Intervalo): Boolean =
        compromisos.any { seCruzan(it, intervalo) }

    fun limiteDePago(aceptadoEn: Long, minutosDePlazo: Int, inicioActividad: Long): Long =
        minOf(aceptadoEn + minutosDePlazo * 60_000L, inicioActividad)

    fun limiteVigente(deadline: Long?, inicioActividad: Long): Long =
        minOf(deadline ?: inicioActividad, inicioActividad)

    fun turnoVencido(deadline: Long?, inicioActividad: Long, ahora: Long): Boolean =
        ahora >= limiteVigente(deadline, inicioActividad)
}
