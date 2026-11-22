package com.sportec.sporthub.utils

import org.junit.Assert.*
import org.junit.Test

class CoincidenciaContactosTest {

    private val juan = CuentaContactable("u1", "juan@demo.test", "3133930243")
    private val camila = CuentaContactable("u2", "camila@demo.test", "+57 300 111 2233")
    private val ajena = CuentaContactable("u3", "otra@demo.test", null)

    @Test fun coincidePorCorreoSinImportarMayusculas() {
        val agenda = listOf(ContactoAgenda("Juan Pérez", correos = listOf("  JUAN@Demo.Test ")))
        val resultado = CoincidenciaContactos.emparejar(listOf(juan, ajena), agenda)
        assertEquals(mapOf("u1" to "Juan Pérez"), resultado)
    }

    @Test fun coincidePorTelefonoConPrefijoYSeparadores() {
        val agenda = listOf(ContactoAgenda("Juan casa", telefonos = listOf("+57 (313) 393-0243")))
        val resultado = CoincidenciaContactos.emparejar(listOf(juan), agenda)
        assertEquals(mapOf("u1" to "Juan casa"), resultado)
    }

    @Test fun elCorreoTienePrioridadSobreElTelefono() {
        val agenda = listOf(
            ContactoAgenda("Por correo", correos = listOf("juan@demo.test")),
            ContactoAgenda("Por teléfono", telefonos = listOf("3133930243"))
        )
        assertEquals("Por correo", CoincidenciaContactos.emparejar(listOf(juan), agenda)["u1"])
    }

    @Test fun unNumeroDemasiadoCortoNoEmpareja() {
        val agenda = listOf(ContactoAgenda("Recados", telefonos = listOf("0243")))
        assertTrue(CoincidenciaContactos.emparejar(listOf(juan), agenda).isEmpty())
    }

    @Test fun unaCuentaSinTelefonoNiCorreoEnLaAgendaNoAparece() {
        val agenda = listOf(ContactoAgenda("Camila", correos = listOf("camila@demo.test")))
        val resultado = CoincidenciaContactos.emparejar(listOf(juan, camila, ajena), agenda)
        assertEquals(setOf("u2"), resultado.keys)
    }

    @Test fun contactoSinNombreNoEmpareja() {
        val agenda = listOf(ContactoAgenda("   ", correos = listOf("juan@demo.test")))
        assertTrue(CoincidenciaContactos.emparejar(listOf(juan), agenda).isEmpty())
    }

    @Test fun conVariosContactosIgualesSeQuedaConElPrimero() {
        val agenda = listOf(
            ContactoAgenda("Camila trabajo", correos = listOf("camila@demo.test")),
            ContactoAgenda("Camila casa", correos = listOf("camila@demo.test"))
        )
        assertEquals("Camila trabajo", CoincidenciaContactos.emparejar(listOf(camila), agenda)["u2"])
    }

    @Test fun agendaVaciaDevuelveVacio() {
        assertTrue(CoincidenciaContactos.emparejar(listOf(juan, camila), emptyList()).isEmpty())
    }

    @Test fun normalizarTelefonoDejaLosUltimosDiezDigitos() {
        assertEquals("3133930243", CoincidenciaContactos.normalizarTelefono("+57 313 393 0243"))
        assertNull(CoincidenciaContactos.normalizarTelefono("123"))
        assertNull(CoincidenciaContactos.normalizarTelefono(null))
    }
}
