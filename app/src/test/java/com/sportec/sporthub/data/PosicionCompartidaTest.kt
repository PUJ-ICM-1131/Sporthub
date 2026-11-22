package com.sportec.sporthub.data

import org.junit.Assert.*
import org.junit.Test

class PosicionCompartidaTest {
    private val base = 1_000_000_000_000L
    private fun posicion(hace: Long) = PosicionCompartida("u", 4.6, -74.0, 10f, base - hace * 1000)

    @Test fun recienteSePresentaEnVivo() {
        assertTrue(posicion(hace = 10).enVivo(base))
        assertTrue(posicion(hace = Encuentros.SEGUNDOS_EN_VIVO.toLong()).enVivo(base))
    }

    @Test fun antiguaYaNoEsEnVivoPeroSigueVigente() {
        val p = posicion(hace = Encuentros.SEGUNDOS_EN_VIVO + 1L)
        assertFalse(p.enVivo(base))
        assertTrue(p.vigente(base))
    }

    @Test fun vencidaDejaDeMostrarse() {
        assertFalse(posicion(hace = Encuentros.SEGUNDOS_VIGENCIA + 1L).vigente(base))
    }
    @Test fun fechaFuturaNoSeMuestra() {
        assertFalse(posicion(hace = -1).enVivo(base))
        assertFalse(posicion(hace = -1).vigente(base))
    }
}
