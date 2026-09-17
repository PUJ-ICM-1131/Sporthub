package com.sportec.sporthub.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val localeColombia: Locale = Locale.forLanguageTag("es-CO")
private val formatoFechaLarga = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", localeColombia)

fun formatoHora(hora: Int): String = String.format(localeColombia, "%02d:00", hora)

fun formatoFecha(fecha: String): String =
    LocalDate.parse(fecha).format(formatoFechaLarga).replaceFirstChar { it.titlecase(localeColombia) }

fun formatoDiaCorto(fecha: String): String {
    val dia = LocalDate.parse(fecha)
    val nombre = dia.dayOfWeek.getDisplayName(TextStyle.SHORT, localeColombia).replaceFirstChar { it.uppercase() }
    return "$nombre ${dia.dayOfMonth}"
}

fun formatoIntervalo(fecha: String, hora: Int, duracionHoras: Int): String =
    "${formatoFecha(fecha)} · ${formatoHora(hora)}–${formatoHora(hora + duracionHoras)}"

fun formatoHoraMillis(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.of("America/Bogota"))
        .format(DateTimeFormatter.ofPattern("d MMM · HH:mm", localeColombia))

fun formatoPesos(valor: Int): String = "$" + String.format(localeColombia, "%,d", valor)

fun formatoDuracion(minutos: Int): String {
    val horas = minutos / 60
    val resto = minutos % 60
    return when {
        horas == 0 -> "$resto min"
        resto == 0 -> "$horas h"
        else -> "$horas h $resto min"
    }
}

fun iniciales(nombre: String): String = nombre
    .split(" ")
    .filter { it.isNotBlank() && it.first().isLetter() }
    .take(2)
    .joinToString("") { it.first().uppercase() }
