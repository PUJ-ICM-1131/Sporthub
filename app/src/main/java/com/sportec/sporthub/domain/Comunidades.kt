package com.sportec.sporthub.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Comunidades (grupos) y su chat asociado (HU-D20 a HU-D25). */
object Comunidades {

    private var secuencia = 800
    private fun uid() = "g" + (++secuencia)

    private val _comunidades = MutableStateFlow(DatosMock.comunidades)
    val comunidades: StateFlow<List<Comunidad>> = _comunidades.asStateFlow()

    fun comunidadDe(id: String): Comunidad? = _comunidades.value.firstOrNull { it.id == id }

    fun esMiembro(comunidadId: String, usuarioId: String): Boolean =
        comunidadDe(comunidadId)?.miembros?.contains(usuarioId) == true

    fun crear(nombre: String, descripcion: String, creadorId: String): Comunidad {
        if (nombre.isBlank()) throw IllegalArgumentException("El nombre de la comunidad no puede estar vacío.")
        val id = uid()
        val comunidad = Comunidad(id, nombre.trim(), descripcion.trim(), creadorId, creadorId, listOf(creadorId))
        _comunidades.value = _comunidades.value + comunidad
        Mensajes.crearGrupo(id)
        return comunidad
    }
}
