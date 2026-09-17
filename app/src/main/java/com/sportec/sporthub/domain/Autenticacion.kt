package com.sportec.sporthub.domain

object Autenticacion {

    fun iniciarSesion(usuario: String, contrasena: String, simularError: Boolean = false): Cuenta {
        if (simularError) throw IllegalStateException("El servicio no está disponible. Intenta de nuevo.")
        val clave = usuario.trim().lowercase()
        val cuenta = Cuentas.todas().firstOrNull { it.id == clave || it.email == clave }
            ?: throw IllegalArgumentException("Correo o contraseña incorrectos.")
        if (cuenta.contrasena != contrasena) throw IllegalArgumentException("Correo o contraseña incorrectos.")
        if (!cuenta.activa) throw IllegalStateException("Esta cuenta no tiene acceso.")
        return cuenta
    }
}
