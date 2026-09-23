package com.sportec.sporthub.domain

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos

object Autenticacion {

    fun iniciarSesion(usuario: String, contrasena: String, simularError: Boolean = false): Cuenta {
        if (simularError) throw IllegalStateException(Textos.obtener(R.string.texto_el_servicio_no_esta_disponible_intenta_de_nuevo))
        val clave = usuario.trim().lowercase()
        val cuenta = Cuentas.todas().firstOrNull { it.id == clave || it.email == clave }
            ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_correo_o_contrasena_incorrectos))
        if (cuenta.contrasena != contrasena) throw IllegalArgumentException(Textos.obtener(R.string.texto_correo_o_contrasena_incorrectos))
        if (!cuenta.activa) throw IllegalStateException(Textos.obtener(R.string.texto_esta_cuenta_no_tiene_acceso))
        return cuenta
    }
}
