package com.sportec.sporthub.navigation

import androidx.navigation3.runtime.NavKey
import com.sportec.sporthub.domain.*
import com.sportec.sporthub.domain.Mensajes as MensajesDominio
import com.sportec.sporthub.domain.Transferencias as TransferenciasDominio

fun destinoNotificacion(
    aviso: Notificacion,
    usuarioId: String,
    resolver: (String, String, String) -> NavKey? = ::destinoAutorizado
): NavKey? {
    if (usuarioId.isBlank() || aviso.usuarioId != usuarioId || aviso.id.isBlank()) return null
    val id = aviso.argumento?.takeIf { it.isNotBlank() } ?: return null
    val tipo = aviso.ruta?.takeIf { it in setOf("chat", "request", "reservation", "transfer", "invitation", "meetup") } ?: return null
    return resolver(tipo, id, usuarioId)
}

private fun destinoAutorizado(tipo: String, id: String, usuarioId: String): NavKey? {
    val cuenta = Cuentas.porId(usuarioId) ?: return null
    if (!cuenta.activa) return null
    fun gestiona(actividadId: String): Boolean = cuenta.rol == Rol.ESTABLECIMIENTO &&
        cuenta.negocioId != null && Negocios.actividadDe(actividadId)?.negocioId == cuenta.negocioId
    return when (tipo) {
        "chat" -> MensajesDominio.chatDe(id)?.takeIf { MensajesDominio.esMiembro(it, usuarioId) }?.let { Chat(id) }
        "request" -> Reservas.obtenerSolicitud(id)?.takeIf { it.usuarioId == usuarioId || gestiona(it.actividadId) }?.let { DetalleSolicitud(id) }
        "reservation" -> Reservas.obtenerReserva(id)?.takeIf { it.usuarioId == usuarioId || gestiona(it.actividadId) }?.let { DetalleReserva(id) }
        "transfer" -> TransferenciasDominio.obtener(id)?.takeIf { transferencia ->
            transferencia.propietarioId == usuarioId || transferencia.postulantes.any { it.usuarioId == usuarioId } ||
                Reservas.obtenerReserva(transferencia.reservaId)?.let { gestiona(it.actividadId) } == true
        }?.let { DetalleTransferencia(id) }
        "invitation" -> Comunidades.invitaciones.value.firstOrNull { it.id == id && it.invitadoId == usuarioId }?.let {
            if (it.puedeResponder(usuarioId)) Notificaciones
            else if (it.estado == EstadoInvitacion.ACEPTADA && Comunidades.esMiembro(it.comunidadId, usuarioId)) Comunidad(it.comunidadId)
            else null
        }
        "meetup" -> Encontrarnos(id).takeIf { AccesoEncuentros.puedeEntrar(id, usuarioId) }
        else -> null
    }
}
