package com.sportec.sporthub.domain

enum class EstadoInvitacion { PENDIENTE, ACEPTADA, RECHAZADA }

data class InvitacionComunidad(
    val id: String,
    val comunidadId: String,
    val invitadoId: String,
    val remitenteId: String,
    val estado: EstadoInvitacion = EstadoInvitacion.PENDIENTE
) {
    fun responder(usuarioId: String, aceptar: Boolean): InvitacionComunidad {
        require(puedeResponder(usuarioId))
        return copy(estado = if (aceptar) EstadoInvitacion.ACEPTADA else EstadoInvitacion.RECHAZADA)
    }

    fun miembrosTrasRespuesta(miembros: List<String>): List<String> =
        if (estado == EstadoInvitacion.ACEPTADA) (miembros + invitadoId).distinct() else miembros

    fun puedeResponder(usuarioId: String): Boolean = invitadoId == usuarioId && estado == EstadoInvitacion.PENDIENTE
}
