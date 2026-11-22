package com.sportec.sporthub.utils

import java.io.InputStream
import java.io.OutputStream
import java.io.IOException

object CopiaFoto {
    const val LIMITE_BYTES = 20L * 1024 * 1024

    fun copiar(origen: InputStream, destino: OutputStream, limite: Long = LIMITE_BYTES): Long {
        require(limite > 0)
        val buffer = ByteArray(8192)
        var total = 0L
        while (true) {
            if (Thread.currentThread().isInterrupted) throw IOException()
            val cantidad = origen.read(buffer)
            if (cantidad < 0) break
            total += cantidad
            if (total > limite) throw IOException()
            destino.write(buffer, 0, cantidad)
        }
        if (total == 0L) throw IOException()
        return total
    }
}
