package com.sportec.sporthub.domain

import com.sportec.sporthub.utils.FotosLocales
import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object Comunidades {

    private fun uid() = "g" + java.util.UUID.randomUUID().toString()

    private val _comunidades = MutableStateFlow(DatosMock.comunidades.map { it.copy(fotoUri = FotosLocales.obtener("comunidad", it.id) ?: it.fotoUri) })
    val comunidades: StateFlow<List<Comunidad>> = _comunidades.asStateFlow()

    private val _invitaciones = MutableStateFlow<List<InvitacionComunidad>>(emptyList())
    val invitaciones = _invitaciones.asStateFlow()

    fun responderInvitacion(id: String, usuarioId: String, aceptar: Boolean) {
        val invitacion = _invitaciones.value.firstOrNull { it.id == id }
            ?: throw IllegalArgumentException(Textos.obtener(R.string.invitacion_no_disponible))
        require(invitacion.puedeResponder(usuarioId)) { Textos.obtener(R.string.invitacion_no_disponible) }
        val comunidad = comunidadDe(invitacion.comunidadId)
            ?: throw IllegalArgumentException(Textos.obtener(R.string.comunidad_no_encontrada))
        val resuelta = invitacion.responder(usuarioId, aceptar)
        if (aceptar) {
            _comunidades.value = _comunidades.value.map {
                if (it.id == comunidad.id) it.copy(miembros = resuelta.miembrosTrasRespuesta(it.miembros)) else it
            }
            Mensajes.crearGrupo(comunidad.id)
        }
        _invitaciones.value = _invitaciones.value.map {
            if (it.id == id) resuelta else it
        }
        Notificaciones.retirar("invitation", id, usuarioId)
        Notificaciones.publicar(invitacion.remitenteId, Textos.obtener(R.string.invitacion_resultado),
            Textos.obtener(if (aceptar) R.string.invitacion_aceptada else R.string.invitacion_rechazada, Cuentas.porId(usuarioId)?.nombre.orEmpty(), comunidad.nombre))
    }

    fun comunidadDe(id: String): Comunidad? = _comunidades.value.firstOrNull { it.id == id }

    fun esMiembro(comunidadId: String, usuarioId: String): Boolean =
        comunidadDe(comunidadId)?.miembros?.contains(usuarioId) == true

    fun editar(comunidadId: String, nombre: String, descripcion: String, usuarioId: String, fotoUri: String? = comunidadDe(comunidadId)?.fotoUri) {
        val comunidad = comunidadDe(comunidadId)
            ?: throw IllegalArgumentException(Textos.obtener(R.string.comunidad_no_encontrada))
        if (comunidad.adminId != usuarioId) {
            throw IllegalArgumentException(Textos.obtener(R.string.comunidad_solo_admin))
        }
        if (nombre.isBlank()) throw IllegalArgumentException(Textos.obtener(R.string.comunidad_nombre_obligatorio))
        FotosLocales.asociar("comunidad", comunidadId, fotoUri)
        _comunidades.value = _comunidades.value.map {
            if (it.id == comunidadId) it.copy(nombre = nombre.trim(), descripcion = descripcion.trim(), fotoUri = fotoUri) else it
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
        require(Cuentas.porId(invitadoId)?.let { it.activa && it.rol == Rol.DEPORTISTA } == true) { Textos.obtener(R.string.invitacion_no_disponible) }
        require(_invitaciones.value.none { it.comunidadId == comunidadId && it.invitadoId == invitadoId && it.estado == EstadoInvitacion.PENDIENTE }) { Textos.obtener(R.string.invitacion_pendiente) }
        val invitacion = InvitacionComunidad(java.util.UUID.randomUUID().toString(), comunidadId, invitadoId, usuarioId)
        _invitaciones.value = _invitaciones.value + invitacion
        Notificaciones.publicar(invitadoId, Textos.obtener(R.string.invitacion_titulo),
            Textos.obtener(R.string.invitacion_mensaje, Cuentas.porId(usuarioId)?.nombre.orEmpty(), comunidad.nombre), "invitation", invitacion.id)
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

    fun crear(nombre: String, descripcion: String, creadorId: String, fotoUri: String? = null): Comunidad {
        if (nombre.isBlank()) throw IllegalArgumentException(Textos.obtener(R.string.texto_el_nombre_de_la_comunidad_no_puede_estar_vacio))
        val id = uid()
        val comunidad = Comunidad(id, nombre.trim(), descripcion.trim(), creadorId, creadorId, listOf(creadorId), fotoUri)
        FotosLocales.asociar("comunidad", id, fotoUri)
        _comunidades.value = _comunidades.value + comunidad
        Mensajes.crearGrupo(id)
        return comunidad
    }
}
