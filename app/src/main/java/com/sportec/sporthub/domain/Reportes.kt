package com.sportec.sporthub.domain

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object Reportes {

    private var secuencia = 1100
    private fun uid() = "rep" + (++secuencia)

    private val _reportes = MutableStateFlow(DatosMock.reportes)
    val reportes: StateFlow<List<Reporte>> = _reportes.asStateFlow()

    fun obtener(id: String): Reporte? = _reportes.value.firstOrNull { it.id == id }

    fun listarPendientes(): List<Reporte> = _reportes.value.filter { it.estado == EstadoReporte.PENDIENTE }

    fun crear(autorId: String, tipoObjetivo: String, motivo: String): Reporte {
        if (motivo.isBlank()) throw IllegalArgumentException(Textos.obtener(R.string.texto_cuentanos_que_paso_antes_de_enviar_el_reporte))
        val nuevo = Reporte(
            id = uid(),
            usuarioId = autorId,
            objetivo = tipoObjetivo,
            contexto = "",
            motivo = motivo,
            autorId = autorId,
            objetivoId = "",
            tipoObjetivo = tipoObjetivo
        )
        _reportes.value = _reportes.value + nuevo
        return nuevo
    }

    fun resolver(id: String) {
        val reporte = obtener(id) ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_el_reporte_ya_no_existe))
        _reportes.value = _reportes.value.map { if (it.id == id) it.copy(estado = EstadoReporte.RESUELTO) else it }
    }
}
