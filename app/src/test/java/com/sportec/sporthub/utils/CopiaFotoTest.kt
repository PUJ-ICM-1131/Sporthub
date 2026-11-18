package com.sportec.sporthub.utils

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import org.junit.Assert.*
import org.junit.Test

class CopiaFotoTest {
    @Test fun conservaLosBytesEnVariosBloques() {
        val bytes = ByteArray(25000) { (it % 251).toByte() }
        val salida = ByteArrayOutputStream()
        assertEquals(bytes.size.toLong(), CopiaFoto.copiar(ByteArrayInputStream(bytes), salida))
        assertArrayEquals(bytes, salida.toByteArray())
    }
    @Test fun aceptaElLimiteExacto() {
        assertEquals(3L, CopiaFoto.copiar(ByteArrayInputStream(byteArrayOf(1, 2, 3)), ByteArrayOutputStream(), 3))
    }
    @Test(expected = IOException::class) fun rechazaExcesoDeTamano() {
        CopiaFoto.copiar(ByteArrayInputStream(ByteArray(9000)), ByteArrayOutputStream(), 8500)
    }
    @Test(expected = IOException::class) fun rechazaArchivoVacio() {
        CopiaFoto.copiar(ByteArrayInputStream(byteArrayOf()), ByteArrayOutputStream())
    }
    @Test(expected = IOException::class) fun propagaFalloDeLectura() {
        CopiaFoto.copiar(object : java.io.InputStream() {
            override fun read(): Int = throw IOException()
        }, ByteArrayOutputStream())
    }
    @Test fun noEscribeMasDelLimite() {
        val salida = ByteArrayOutputStream()
        try { CopiaFoto.copiar(ByteArrayInputStream(ByteArray(10000)), salida, 9000); fail() }
        catch (_: IOException) { assertTrue(salida.size() <= 9000) }
    }
    @Test fun interrumpirLaCopiaNoEscribeDatos() {
        val salida = ByteArrayOutputStream()
        Thread.currentThread().interrupt()
        try {
            CopiaFoto.copiar(ByteArrayInputStream(byteArrayOf(1)), salida)
            fail()
        } catch (_: IOException) {
            assertEquals(0, salida.size())
        } finally {
            Thread.interrupted()
        }
    }
    @Test(expected = IOException::class) fun propagaFalloDeEscritura() {
        CopiaFoto.copiar(ByteArrayInputStream(byteArrayOf(1)), object : java.io.OutputStream() {
            override fun write(valor: Int) { throw IOException() }
        })
    }
    @Test(expected = IllegalArgumentException::class) fun rechazaLimiteInvalido() {
        CopiaFoto.copiar(ByteArrayInputStream(byteArrayOf(1)), ByteArrayOutputStream(), 0)
    }
}
