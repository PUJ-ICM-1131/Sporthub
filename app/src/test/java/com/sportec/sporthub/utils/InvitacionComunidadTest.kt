package com.sportec.sporthub.utils

import com.sportec.sporthub.domain.InvitacionComunidad
import com.sportec.sporthub.domain.EstadoInvitacion
import org.junit.Assert.*
import org.junit.Test

class InvitacionComunidadTest {
    private val invitacion = InvitacionComunidad("i", "g", "invitado", "admin")

    @Test fun pendienteNoIncorporaAlGrupo() {
        assertEquals(listOf("admin"), invitacion.miembrosTrasRespuesta(listOf("admin")))
    }
    @Test fun aceptarIncorporaUnaSolaVez() {
        val resuelta = invitacion.responder("invitado", true)
        assertEquals(EstadoInvitacion.ACEPTADA, resuelta.estado)
        assertEquals(listOf("admin", "invitado"), resuelta.miembrosTrasRespuesta(listOf("admin", "invitado")))
        assertEquals(listOf("admin", "invitado"), resuelta.miembrosTrasRespuesta(listOf("admin")))
    }
    @Test fun rechazarNoIncorporaAlGrupo() {
        val resuelta = invitacion.responder("invitado", false)
        assertEquals(EstadoInvitacion.RECHAZADA, resuelta.estado)
        assertEquals(listOf("admin"), resuelta.miembrosTrasRespuesta(listOf("admin")))
    }
    @Test(expected = IllegalArgumentException::class) fun otroUsuarioNoPuedeAceptar() {
        invitacion.responder("admin", true)
    }
    @Test(expected = IllegalArgumentException::class) fun noSePuedeResponderDosVeces() {
        invitacion.responder("invitado", false).responder("invitado", true)
    }
}
