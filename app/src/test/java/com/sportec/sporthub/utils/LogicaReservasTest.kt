package com.sportec.sporthub.utils

import org.junit.Assert.*
import org.junit.Test

class LogicaReservasTest {

    private val fecha = "2027-01-15"
    private fun intervalo(hora: Int, duracion: Int = 2) = Intervalo(fecha, hora, duracion)

    @Test fun dosReservasQueSePisanSeCruzan() {
        assertTrue(LogicaReservas.seCruzan(intervalo(14), intervalo(15)))
        assertTrue(LogicaReservas.seCruzan(intervalo(15), intervalo(14)))
    }

    @Test fun horariosConsecutivosNoSeCruzan() {
        assertFalse(LogicaReservas.seCruzan(intervalo(14), intervalo(16)))
        assertFalse(LogicaReservas.seCruzan(intervalo(16), intervalo(14)))
    }

    @Test fun otroDiaNoSeCruza() {
        assertFalse(LogicaReservas.seCruzan(intervalo(14), Intervalo("2027-01-16", 14, 2)))
    }

    @Test fun elCruceSeBuscaEntreTodosLosCompromisos() {
        val compromisos = listOf(intervalo(8, 1), intervalo(14))
        assertTrue(LogicaReservas.hayCruce(compromisos, intervalo(15, 2)))
        assertFalse(LogicaReservas.hayCruce(compromisos, intervalo(16, 2)))
        assertFalse(LogicaReservas.hayCruce(emptyList(), intervalo(14)))
    }

    @Test fun elPlazoNuncaPasaDelInicioDeLaActividad() {
        val inicio = LogicaReservas.inicioMillis(fecha, 18)
        val aceptadoAntes = inicio - 60 * 60_000L
        assertEquals(aceptadoAntes + 10 * 60_000L, LogicaReservas.limiteDePago(aceptadoAntes, 10, inicio))
        val aceptadoJusto = inicio - 5 * 60_000L
        assertEquals(inicio, LogicaReservas.limiteDePago(aceptadoJusto, 10, inicio))
    }

    @Test fun elTurnoVenceAlLlegarSuLimite() {
        val inicio = LogicaReservas.inicioMillis(fecha, 18)
        val limite = inicio - 50 * 60_000L
        assertFalse(LogicaReservas.turnoVencido(limite, inicio, limite - 1))
        assertTrue(LogicaReservas.turnoVencido(limite, inicio, limite))
        assertTrue(LogicaReservas.turnoVencido(limite, inicio, limite + 1))
    }

    @Test fun desdeElInicioDeLaActividadYaNoSePaga() {
        val inicio = LogicaReservas.inicioMillis(fecha, 18)
        val plazoPosterior = inicio + 30 * 60_000L
        assertTrue(LogicaReservas.turnoVencido(plazoPosterior, inicio, inicio))
        assertFalse(LogicaReservas.turnoVencido(plazoPosterior, inicio, inicio - 1))
    }

    @Test fun sinPlazoGuardadoManda() {
        val inicio = LogicaReservas.inicioMillis(fecha, 18)
        assertTrue(LogicaReservas.turnoVencido(null, inicio, inicio))
        assertFalse(LogicaReservas.turnoVencido(null, inicio, inicio - 1))
    }

    @Test fun laHoraSeCalculaEnLaZonaDeBogota() {
        assertEquals(
            LogicaReservas.inicioMillis(fecha, 14) + 2 * 3_600_000L,
            LogicaReservas.finMillis(fecha, 14, 2)
        )
        assertEquals(3_600_000L, LogicaReservas.inicioMillis(fecha, 15) - LogicaReservas.inicioMillis(fecha, 14))
    }
}
