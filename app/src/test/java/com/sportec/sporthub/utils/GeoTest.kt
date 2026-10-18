package com.sportec.sporthub.utils

import com.sportec.sporthub.domain.ModoViaje
import com.sportec.sporthub.domain.Trayecto
import com.sportec.sporthub.domain.Trayectos
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GeoTest {
    private val chapinero = Coordenada(4.6486, -74.0631)
    private val usaquen = Coordenada(4.6947, -74.0303)

    @Test fun distanciaEntreLocalidadesDeBogota() {
        val metros = Geo.distanciaMetros(chapinero, usaquen)
        assertTrue("fue $metros", metros in 6000.0..6400.0)
        assertEquals(0.0, Geo.distanciaMetros(chapinero, chapinero), 1e-6)
    }

    @Test fun distanciaAlTrazadoMideHastaElSegmentoMasCercano() {
        val trazado = listOf(Coordenada(4.60, -74.10), Coordenada(4.60, -74.08))
        val sobre = Coordenada(4.60, -74.09)
        val alLado = Coordenada(4.6009, -74.09)
        assertTrue(Geo.distanciaAlTrazado(sobre, trazado) < 1.0)
        assertEquals(100.0, Geo.distanciaAlTrazado(alLado, trazado), 3.0)
    }

    @Test fun restanteDisminuyeAlAvanzar() {
        val trazado = listOf(Coordenada(4.60, -74.10), Coordenada(4.60, -74.09), Coordenada(4.61, -74.09))
        val alInicio = Geo.restanteMetros(trazado.first(), trazado)
        val aMitad = Geo.restanteMetros(Coordenada(4.60, -74.095), trazado)
        assertEquals(Geo.longitudMetros(trazado), alInicio, 1.0)
        assertTrue(aMitad < alInicio)
        assertEquals(0.0, Geo.restanteMetros(trazado.last(), trazado), 1.0)
    }

    @Test fun circuloTieneElRadioPedidoYSeCierra() {
        val puntos = Geo.circulo(chapinero, 2.0)
        assertEquals(puntos.first(), puntos.last())
        puntos.forEach { assertEquals(2000.0, Geo.distanciaMetros(chapinero, it), 40.0) }
    }

    @Test fun encuadreAlejaLaCamaraParaAreasGrandes() {
        val (_, zoomCerca) = Geo.encuadre(listOf(chapinero, Coordenada(4.6496, -74.0621)))
        val (centro, zoomLejos) = Geo.encuadre(listOf(chapinero, usaquen))
        assertTrue(zoomCerca > zoomLejos)
        assertEquals((chapinero.latitud + usaquen.latitud) / 2, centro.latitud, 1e-9)
    }

    @Test fun rutaSeConservaMientrasNoHayaDesvio() {
        val puntos = listOf(Coordenada(4.60, -74.10), Coordenada(4.60, -74.08))
        val ruta = Trayecto("b1", ModoViaje.CAMINANDO, puntos.first(), puntos, 2200.0, 1600.0, estimado = false)
        assertNotNull(Trayectos.vigente("b1", ModoViaje.CAMINANDO, Coordenada(4.6003, -74.09), ruta))
        assertNull(Trayectos.vigente("b1", ModoViaje.CAMINANDO, Coordenada(4.602, -74.09), ruta))
        assertNull(Trayectos.vigente("b2", ModoViaje.CAMINANDO, puntos.first(), ruta))
        assertNull(Trayectos.vigente("b1", ModoViaje.CARRO, puntos.first(), ruta))
    }

    @Test fun rutaEstimadaSeRecalculaAlAlejarseDelInicio() {
        val ruta = Trayectos.estimado("b1", ModoViaje.CAMINANDO, chapinero, usaquen)
        assertTrue(ruta.estimado)
        assertNotNull(Trayectos.vigente("b1", ModoViaje.CAMINANDO, chapinero, ruta))
        assertNull(Trayectos.vigente("b1", ModoViaje.CAMINANDO, Coordenada(4.6500, -74.0631), ruta))
    }
}
