package com.sportec.sporthub.domain

import com.google.firebase.firestore.FieldValue
import com.sportec.sporthub.R
import com.sportec.sporthub.data.Condicion
import com.sportec.sporthub.data.Escritura
import com.sportec.sporthub.data.FotosRemotas
import com.sportec.sporthub.data.Mapeos
import com.sportec.sporthub.data.Nube
import com.sportec.sporthub.utils.FotosLocales
import com.sportec.sporthub.utils.Textos

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Comunidades en `comunidades/{id}` e invitaciones en `invitaciones/{comunidadId_invitadoId}`.
 * Aceptar una invitación es una transacción: solo prospera si sigue PENDIENTE.
 */
object Comunidades {

    private fun uid() = "g" + java.util.UUID.randomUUID().toString()

    private val _comunidades = MutableStateFlow<List<Comunidad>>(emptyList())
    val comunidades: StateFlow<List<Comunidad>> = _comunidades.asStateFlow()

    private val _invitaciones = MutableStateFlow<List<InvitacionComunidad>>(emptyList())
    val invitaciones = _invitaciones.asStateFlow()

    internal fun cargarComunidades(lista: List<Comunidad>) {
        // La foto de la comunidad puede ser local (recién elegida) hasta que se sube.
        _comunidades.value = lista.map { c ->
            c.copy(fotoUri = c.fotoUri ?: FotosLocales.obtener("comunidad", c.id))
        }
    }

    internal fun cargarInvitaciones(lista: List<InvitacionComunidad>) {
        _invitaciones.value = lista
    }

    internal fun vaciar() {
        _comunidades.value = emptyList(); _invitaciones.value = emptyList()
    }

    fun idInvitacion(comunidadId: String, invitadoId: String) = "${comunidadId}_$invitadoId"

    fun responderInvitacion(id: String, usuarioId: String, aceptar: Boolean) {
        val invitacion = _invitaciones.value.firstOrNull { it.id == id }
            ?: throw IllegalArgumentException(Textos.obtener(R.string.invitacion_no_disponible))
        require(invitacion.puedeResponder(usuarioId)) { Textos.obtener(R.string.invitacion_no_disponible) }
        val comunidad = comunidadDe(invitacion.comunidadId)
            ?: throw IllegalArgumentException(Textos.obtener(R.string.comunidad_no_encontrada))
        Nube.agrupar(Condicion.Igual("invitaciones/$id", "estado", EstadoInvitacion.PENDIENTE.name)) {
            val resuelta = invitacion.responder(usuarioId, aceptar)
            if (aceptar) {
                _comunidades.value = _comunidades.value.map {
                    if (it.id == comunidad.id) it.copy(miembros = resuelta.miembrosTrasRespuesta(it.miembros)) else it
                }
                Mensajes.crearGrupo(comunidad.id, persistir = false)
                Nube.escribir(
                    Escritura.Actualizar("comunidades/${comunidad.id}", mapOf("miembros" to FieldValue.arrayUnion(usuarioId))),
                    Escritura.Actualizar("chats/${comunidad.id}", mapOf("miembros" to FieldValue.arrayUnion(usuarioId)))
                )
            }
            _invitaciones.value = _invitaciones.value.map { if (it.id == id) resuelta else it }
            Nube.escribir(Escritura.Actualizar("invitaciones/$id",
                mapOf("estado" to resuelta.estado.name, "actualizadaEn" to Nube.ahora())))
            Notificaciones.retirar("invitation", id, usuarioId)
            Notificaciones.publicar(invitacion.remitenteId, Textos.obtener(R.string.invitacion_resultado),
                Textos.obtener(if (aceptar) R.string.invitacion_aceptada else R.string.invitacion_rechazada, Cuentas.porId(usuarioId)?.nombre.orEmpty(), comunidad.nombre))
        }
    }

    fun comunidadDe(id: String): Comunidad? = _comunidades.value.firstOrNull { it.id == id }

    fun esMiembro(comunidadId: String, usuarioId: String): Boolean =
        comunidadDe(comunidadId)?.miembros?.contains(usuarioId) == true

    /** Sube la foto elegida (si es local) y guarda solo la referencia compartida. */
    private fun subirFoto(comunidadId: String, fotoUri: String?) {
        if (fotoUri == null || FotosRemotas.esReferencia(fotoUri)) return
        Nube.alcance.launch {
            val ref = FotosRemotas.subirTodas(listOf(fotoUri)).firstOrNull() ?: return@launch
            Nube.escribir(Escritura.Actualizar("comunidades/$comunidadId", mapOf("foto" to ref)))
        }
    }

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
        Nube.escribir(Escritura.Actualizar("comunidades/$comunidadId",
            mapOf("nombre" to nombre.trim(), "descripcion" to descripcion.trim())))
        subirFoto(comunidadId, fotoUri)
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
        val id = idInvitacion(comunidadId, invitadoId)
        require(_invitaciones.value.none { it.id == id && it.estado == EstadoInvitacion.PENDIENTE }) { Textos.obtener(R.string.invitacion_pendiente) }
        Nube.agrupar {
            val invitacion = InvitacionComunidad(id, comunidadId, invitadoId, usuarioId)
            _invitaciones.value = _invitaciones.value.filterNot { it.id == id } + invitacion
            Nube.escribir(Escritura.Crear("invitaciones/$id", Mapeos.mapa(invitacion)))
            Notificaciones.publicar(invitadoId, Textos.obtener(R.string.invitacion_titulo),
                Textos.obtener(R.string.invitacion_mensaje, Cuentas.porId(usuarioId)?.nombre.orEmpty(), comunidad.nombre), "invitation", invitacion.id)
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
        Nube.escribir(Escritura.Actualizar("comunidades/$comunidadId", mapOf("adminId" to nuevoAdminId)))
    }

    fun crear(nombre: String, descripcion: String, creadorId: String, fotoUri: String? = null): Comunidad = Nube.agrupar {
        if (nombre.isBlank()) throw IllegalArgumentException(Textos.obtener(R.string.texto_el_nombre_de_la_comunidad_no_puede_estar_vacio))
        val id = uid()
        val comunidad = Comunidad(id, nombre.trim(), descripcion.trim(), creadorId, creadorId, listOf(creadorId), fotoUri)
        FotosLocales.asociar("comunidad", id, fotoUri)
        _comunidades.value = _comunidades.value + comunidad
        Nube.escribir(Escritura.Crear("comunidades/$id", Mapeos.mapa(comunidad, null)))
        Mensajes.crearGrupo(id)
        subirFoto(id, fotoUri)
        comunidad
    }
}
