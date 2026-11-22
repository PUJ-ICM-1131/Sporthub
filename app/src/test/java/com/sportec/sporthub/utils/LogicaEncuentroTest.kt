package com.sportec.sporthub.utils

import com.sportec.sporthub.data.Encuentro
import com.sportec.sporthub.data.Encuentros
import com.sportec.sporthub.data.EstadoEncuentro
import com.sportec.sporthub.data.OrigenEncuentro
import com.sportec.sporthub.data.PosicionCompartida
import com.sportec.sporthub.domain.PosicionActual
import org.junit.Assert.*
import org.junit.Test

class LogicaEncuentroTest {
    private val ahora = 1_000_000_000_000L
    private fun lectura(haceMs: Long, lat: Double = 4.6, lng: Double = -74.08) =
        PosicionActual(lat, lng, ahora - haceMs, 8f)
    private fun compartida(uid: String, haceS: Long) =
        PosicionCompartida(uid, 4.6, -74.08, 10f, ahora - haceS * 1000)
    private fun encuentro(tipo: OrigenEncuentro, creador: String) =
        Encuentro("e_x", tipo, "x", creador, EstadoEncuentro.ABIERTO, null)

    @Test fun sinPosicionOGpsApagadoNoSePublica() {
        assertFalse(LogicaEncuentro.debePublicar(null, null, ahora, ubicacionActiva = true))
        assertFalse(LogicaEncuentro.debePublicar(null, lectura(1_000), ahora, ubicacionActiva = false))
    }

    @Test fun lecturaAntiguaNoSePresentaComoEnVivo() {
        val vieja = lectura(LogicaEncuentro.EDAD_MAXIMA_LECTURA_MS + 1)
        assertFalse(LogicaEncuentro.debePublicar(null, vieja, ahora, ubicacionActiva = true))
    }

    @Test fun primeraLecturaRecienteSePublica() {
        assertTrue(LogicaEncuentro.debePublicar(null, lectura(1_000), ahora, ubicacionActiva = true))
    }

    @Test fun noSePublicaSeguidoSinMoverse() {
        val ultima = Publicacion(Coordenada(4.6, -74.08), ahora - 5_000)
        assertFalse(LogicaEncuentro.debePublicar(ultima, lectura(500), ahora, ubicacionActiva = true))
    }

    @Test fun seRenuevaAunqueNoHayaMovimiento() {
        val ultima = Publicacion(Coordenada(4.6, -74.08), ahora - LogicaEncuentro.RENOVACION_MS)
        assertTrue(LogicaEncuentro.debePublicar(ultima, lectura(500), ahora, ubicacionActiva = true))
    }

    @Test fun unDesplazamientoSePublicaAntesDeLaRenovacion() {
        val ultima = Publicacion(Coordenada(4.6, -74.08), ahora - 5_000)
        val movida = lectura(500, lat = 4.6002)
        assertTrue(LogicaEncuentro.debePublicar(ultima, movida, ahora, ubicacionActiva = true))
    }

    @Test fun respetaElIntervaloMinimoAunqueSeMueva() {
        val ultima = Publicacion(Coordenada(4.6, -74.08), ahora - 1_000)
        assertFalse(LogicaEncuentro.debePublicar(ultima, lectura(100, lat = 4.61), ahora, ubicacionActiva = true))
    }

    @Test fun estadosDeLasPosiciones() {
        assertEquals(EstadoPosicion.EN_VIVO, LogicaEncuentro.estado(compartida("a", 5), ahora))
        assertEquals(EstadoPosicion.DESACTUALIZADA, LogicaEncuentro.estado(compartida("a", Encuentros.SEGUNDOS_EN_VIVO + 1L), ahora))
        assertEquals(EstadoPosicion.VENCIDA, LogicaEncuentro.estado(compartida("a", Encuentros.SEGUNDOS_VIGENCIA + 1L), ahora))
    }

    @Test fun nuncaMuestraAjenosNiLaPropiaNiVencidas() {
        val lista = listOf(
            compartida("yo", 1),
            compartida("amiga", 1),
            compartida("ajeno", 1),
            compartida("vieja", Encuentros.SEGUNDOS_VIGENCIA + 10L)
        )
        val visibles = LogicaEncuentro.visibles(lista, setOf("yo", "amiga", "vieja"), "yo", ahora)
        assertEquals(listOf("amiga"), visibles.map { it.uid })
    }

    @Test fun finalizaQuienCreaOElAdministradorDeLaComunidad() {
        assertTrue(LogicaEncuentro.puedeFinalizar(encuentro(OrigenEncuentro.CHAT, "a"), "a", null))
        assertFalse(LogicaEncuentro.puedeFinalizar(encuentro(OrigenEncuentro.CHAT, "a"), "b", "b"))
        assertTrue(LogicaEncuentro.puedeFinalizar(encuentro(OrigenEncuentro.COMUNIDAD, "a"), "admin", "admin"))
        assertFalse(LogicaEncuentro.puedeFinalizar(encuentro(OrigenEncuentro.COMUNIDAD, "a"), "c", "admin"))
    }

    @Test fun tipoDeOrigen() {
        assertEquals(OrigenEncuentro.COMUNIDAD, LogicaEncuentro.tipoOrigen(esComunidad = true))
        assertEquals(OrigenEncuentro.CHAT, LogicaEncuentro.tipoOrigen(esComunidad = false))
    }
}
