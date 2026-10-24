package com.sportec.sporthub.utils

import com.sportec.sporthub.domain.PosicionActual
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PosicionActualTest {
    private val ahora = 100000L
    private val posicion = PosicionActual(4.6, -74.0, ahora, 10f)

    @Test fun ubicacionRecienteConPrecisionConocida() {
        assertTrue(posicion.esFiable(ahora))
        assertTrue(posicion.esFiable(ahora + 30000L))
        assertFalse(posicion.esFiable(ahora + 30001L))
    }

    @Test fun sinPrecisionNoEsUbicacionFiable() {
        assertFalse(posicion.copy(precisionMetros = null).esFiable(ahora))
        for (precision in listOf(0f, -1f, 51f, Float.NaN, Float.POSITIVE_INFINITY)) {
            assertFalse(posicion.copy(precisionMetros = precision).esFiable(ahora))
        }
    }

    @Test fun fechaFuturaOInvalida() {
        assertFalse(posicion.esFiable(ahora - 1))
        assertFalse(posicion.copy(instante = 0).esFiable(ahora))
    }

    @Test fun coordenadasInvalidas() {
        assertFalse(posicion.copy(latitud = 91.0).esFiable(ahora))
        assertFalse(posicion.copy(longitud = -181.0).esFiable(ahora))
        assertFalse(posicion.copy(latitud = Double.NaN).esFiable(ahora))
    }

    @Test fun rumboCambiaConLaPosicionDelUsuario() {
        val inicial = CalculosBrujula.rumbo(0.0, 0.0, 1.0, 0.0)
        val despues = CalculosBrujula.rumbo(0.0, 1.0, 1.0, 0.0)
        assertTrue(inicial == 0f)
        assertTrue(despues > 270f && despues < 360f)
    }
}
