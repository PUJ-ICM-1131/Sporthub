package com.sportec.sporthub.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculosBrujulaTest {
    @Test fun cardinales() {
        assertEquals(0f, CalculosBrujula.rumbo(0.0, 0.0, 1.0, 0.0), 0.01f)
        assertEquals(90f, CalculosBrujula.rumbo(0.0, 0.0, 0.0, 1.0), 0.01f)
        assertEquals(180f, CalculosBrujula.rumbo(0.0, 0.0, -1.0, 0.0), 0.01f)
        assertEquals(270f, CalculosBrujula.rumbo(0.0, 0.0, 0.0, -1.0), 0.01f)
    }
    @Test fun cambioDeHemisferio() {
        assertEquals(90f, CalculosBrujula.rumbo(0.0, 179.0, 0.0, -179.0), 0.01f)
    }
    @Test fun giroCortoAlCruzarNorte() {
        assertEquals(2f, CalculosBrujula.diferencia(1f, 359f), 0.01f)
        assertEquals(-2f, CalculosBrujula.diferencia(359f, 1f), 0.01f)
    }
    @Test fun suavizadoNoDaVueltaCompleta() {
        val resultado = CalculosBrujula.suavizar(359f, 1f)
        assertTrue(resultado > 359f)
        assertTrue(resultado < 360f)
    }
    @Test fun normalizacionDeAngulos() {
        assertEquals(0f, CalculosBrujula.normalizar(720f), 0f)
        assertEquals(350f, CalculosBrujula.normalizar(-10f), 0f)
    }
    @Test fun mismoPuntoNoProduceNaN() {
        assertTrue(CalculosBrujula.rumbo(4.6, -74.0, 4.6, -74.0).isFinite())
    }
}
