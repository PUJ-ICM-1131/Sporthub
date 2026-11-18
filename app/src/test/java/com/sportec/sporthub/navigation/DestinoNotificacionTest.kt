package com.sportec.sporthub.navigation

import com.sportec.sporthub.domain.Notificacion
import org.junit.Assert.*
import org.junit.Test

class DestinoNotificacionTest {
    private val aviso = Notificacion("aviso-1", "ana", "", "", "chat", "chat-1")
    private fun noDebeConsultar(aviso: Notificacion, usuario: String) {
        assertNull(destinoNotificacion(aviso, usuario) { _, _, _ ->
            fail("No debe consultar recursos de una entrada inválida")
            null
        })
    }
    @Test fun rechazaOtraCuentaAntesDeConsultar() = noDebeConsultar(aviso, "carlos")
    @Test fun rechazaUsuarioVacio() = noDebeConsultar(aviso.copy(usuarioId = ""), "")
    @Test fun rechazaIdentificadorDeAvisoVacio() = noDebeConsultar(aviso.copy(id = " "), "ana")
    @Test fun rechazaRecursoVacio() = noDebeConsultar(aviso.copy(argumento = " "), "ana")
    @Test fun rechazaRecursoNulo() = noDebeConsultar(aviso.copy(argumento = null), "ana")
    @Test fun rechazaRutaDesconocida() = noDebeConsultar(aviso.copy(ruta = "admin"), "ana")
    @Test fun rechazaRutaNula() = noDebeConsultar(aviso.copy(ruta = null), "ana")
    @Test fun noAbreRecursoInaccesible() {
        assertNull(destinoNotificacion(aviso, "ana") { _, _, _ -> null })
    }
    @Test fun conservaIdentidadDeCadaAviso() {
        val recibidos = mutableListOf<String>()
        for (id in listOf("chat-1", "chat-2")) {
            assertEquals(Chat(id), destinoNotificacion(aviso.copy(argumento = id), "ana") { tipo, recurso, usuario ->
                assertEquals("chat", tipo)
                assertEquals("ana", usuario)
                recibidos.add(recurso)
                Chat(recurso)
            })
        }
        assertEquals(listOf("chat-1", "chat-2"), recibidos)
    }
    @Test fun consultaLosTiposExistentes() {
        for (tipo in listOf("chat", "request", "reservation", "transfer", "invitation")) {
            var consultas = 0
            destinoNotificacion(aviso.copy(ruta = tipo), "ana") { recibido, _, _ ->
                assertEquals(tipo, recibido)
                consultas++
                null
            }
            assertEquals(1, consultas)
        }
    }
}
