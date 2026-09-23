package com.sportec.sporthub.domain

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object Comunidades {

    private var secuencia = 800
    private fun uid() = "g" + (++secuencia)

    private val _comunidades = MutableStateFlow(DatosMock.comunidades)
    val comunidades: StateFlow<List<Comunidad>> = _comunidades.asStateFlow()

    fun comunidadDe(id: String): Comunidad? = _comunidades.value.firstOrNull { it.id == id }

    fun esMiembro(comunidadId: String, usuarioId: String): Boolean =
        comunidadDe(comunidadId)?.miembros?.contains(usuarioId) == true

    fun editar(comunidadId: String, nombre: String, descripcion: String, usuarioId: String) {
        val comunidad = comunidadDe(comunidadId)
            ?: throw IllegalArgumentException(Textos.obtener(R.string.comunidad_no_encontrada))
        if (comunidad.adminId != usuarioId) {
            throw IllegalArgumentException(Textos.obtener(R.string.comunidad_solo_admin))
        }
        if (nombre.isBlank()) throw IllegalArgumentException(Textos.obtener(R.string.comunidad_nombre_obligatorio))
        _comunidades.value = _comunidades.value.map {
            if (it.id == comunidadId) it.copy(nombre = nombre.trim(), descripcion = descripcion.trim()) else it
        }
    }

    fun invitar(comunidadId: String, invitadoId: String, usuarioId: String) {
        val comunidad = comunidadDe(comunidadId)
            ?: throw IllegalArgumentException(Textos.obtener(R.string.comunidad_no_encontrada))
        if (!comunidad.miembros.contains(usuarioId)) {
            throw IllegalArgumentException(Textos.obtener(R.string.comunidad_solo_miembros))
        }
        if (comunidad.miembros.contains(invitadoId)) {
            throw IllegalArgumentException(Textos.obtener(R.string.comunidad_ya_es_miembro))
        }
        _comunidades.value = _comunidades.value.map {
            if (it.id == comunidadId) it.copy(miembros = it.miembros + invitadoId) else it
        }
    }

    fun cederAdministracion(comunidadId: String, nuevoAdminId: String, usuarioId: String) {
        val comunidad = comunidadDe(comunidadId)
            ?: throw IllegalArgumentException(Textos.obtener(R.string.comunidad_no_encontrada))
        if (comunidad.adminId != usuarioId) {
            throw IllegalArgumentException(Textos.obtener(R.string.comunidad_solo_admin))
        }
        if (!comunidad.miembros.contains(nuevoAdminId)) {
            throw IllegalArgumentException(Textos.obtener(R.string.comunidad_debe_ser_miembro))
        }
        _comunidades.value = _comunidades.value.map {
            if (it.id == comunidadId) it.copy(adminId = nuevoAdminId) else it
        }
    }

    fun crear(nombre: String, descripcion: String, creadorId: String): Comunidad {
        if (nombre.isBlank()) throw IllegalArgumentException(Textos.obtener(R.string.texto_el_nombre_de_la_comunidad_no_puede_estar_vacio))
        val id = uid()
        val comunidad = Comunidad(id, nombre.trim(), descripcion.trim(), creadorId, creadorId, listOf(creadorId))
        _comunidades.value = _comunidades.value + comunidad
        Mensajes.crearGrupo(id)
        return comunidad
    }
}
