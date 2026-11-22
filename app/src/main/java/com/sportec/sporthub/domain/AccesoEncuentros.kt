package com.sportec.sporthub.domain

object AccesoEncuentros {
    fun permite(rol: String?, activa: Boolean, usuarioId: String, miembros: List<String>, comunidad: Boolean, deportistas: Set<String>): Boolean {
        if (rol != "DEPORTISTA" || !activa || usuarioId !in miembros) return false
        return comunidad || (miembros.size == 2 && miembros.all { it in deportistas })
    }

    fun puedeEntrar(origenId: String, usuarioId: String): Boolean {
        val comunidad = Comunidades.comunidadDe(origenId)
        val chat = Mensajes.chatDe(origenId)
        val cuenta = Cuentas.porId(usuarioId)
        val miembros = comunidad?.miembros ?: chat?.comunidadId?.let { Comunidades.comunidadDe(it)?.miembros } ?: chat?.miembros.orEmpty()
        return permite(cuenta?.rol?.name, cuenta?.activa == true, usuarioId, miembros,
            comunidad != null || chat?.comunidadId != null,
            Cuentas.todas().filter { it.activa && it.rol == Rol.DEPORTISTA }.map { it.id }.toSet())
    }
}
