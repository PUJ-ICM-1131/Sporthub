package com.sportec.sporthub.domain

import org.junit.Assert.*
import org.junit.Test

class AccesoEncuentrosTest {
    private fun permite(rol: String? = "DEPORTISTA", activa: Boolean = true, usuario: String = "a", miembros: List<String> = listOf("a", "b"), comunidad: Boolean = false, deportistas: Set<String> = setOf("a", "b")) =
        AccesoEncuentros.permite(rol, activa, usuario, miembros, comunidad, deportistas)

    @Test fun dosDeportistasDelChatPuedenEntrar() { assertTrue(permite()) }
    @Test fun establecimientoNoEntraAunqueSeaMiembro() { assertFalse(permite(rol = "ESTABLECIMIENTO")) }
    @Test fun administradorNoEntraAunqueSeaMiembro() { assertFalse(permite(rol = "ADMINISTRADOR")) }
    @Test fun cuentaInactivaNoEntra() { assertFalse(permite(activa = false)) }
    @Test fun ajenoNoEntra() { assertFalse(permite(usuario = "c")) }
    @Test fun chatConEstablecimientoNoOfreceEncuentro() { assertFalse(permite(deportistas = setOf("a"))) }
    @Test fun comunidadPermiteDeportistaMiembro() { assertTrue(permite(comunidad = true, miembros = listOf("a", "b", "c"))) }
    @Test fun comunidadNoPermiteAdministradorDeAplicacion() { assertFalse(permite(rol = "ADMINISTRADOR", comunidad = true)) }
    @Test fun chatIncompletoNoEntra() { assertFalse(permite(miembros = listOf("a"))) }
}
