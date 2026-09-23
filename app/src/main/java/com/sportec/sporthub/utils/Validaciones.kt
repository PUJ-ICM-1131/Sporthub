package com.sportec.sporthub.utils

object Validaciones {
    private val formatoCorreo = Regex("^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)+$")

    fun correo(valor: String): Boolean = formatoCorreo.matches(valor.trim())

    fun telefono(valor: String?): Boolean {
        if (valor.isNullOrBlank()) return true
        val limpio = valor.trim()
        return limpio.all { it.isDigit() || it in "+ -()" } &&
            limpio.count(Char::isDigit) in 7..15 &&
            limpio.count { it == '+' } <= 1 &&
            ('+' !in limpio || limpio.startsWith('+'))
    }

    fun nit(valor: String): Boolean = Regex("[0-9]{9}(?:-?[0-9])?").matches(valor.trim())

    fun contrasena(valor: String): Boolean = valor.length >= 6 && valor.isNotBlank()
}
