package com.sportec.sporthub.domain

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos

import com.sportec.sporthub.utils.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object Cuentas {

    private var secuencia = 1000
    private fun uid() = "u" + (++secuencia)

    private val _cuentas = MutableStateFlow(DatosMock.cuentas)
    val cuentas: StateFlow<List<Cuenta>> = _cuentas.asStateFlow()

    fun todas(): List<Cuenta> = _cuentas.value

    fun porId(id: String): Cuenta? = _cuentas.value.firstOrNull { it.id == id }

    fun nombreParaMostrar(cuenta: Cuenta): String =
        if (cuenta.rol == Rol.ESTABLECIMIENTO) {
            cuenta.negocioId?.let { Negocios.negocioDe(it)?.nombre } ?: cuenta.nombre
        } else {
            cuenta.nombre
        }

    fun validarRegistro(nombre: String, email: String, contrasena: String, telefono: String? = null) {
        val correo = email.trim().lowercase()
        if (nombre.isBlank()) throw IllegalArgumentException(Textos.obtener(R.string.texto_el_nombre_es_obligatorio))
        if (!Validaciones.correo(correo)) throw IllegalArgumentException(Textos.obtener(R.string.texto_ingresa_un_correo_valido))
        if (!Validaciones.contrasena(contrasena)) throw IllegalArgumentException(Textos.obtener(R.string.texto_la_contrasena_debe_tener_al_menos_6_caracteres))
        if (_cuentas.value.any { it.email == correo }) throw IllegalStateException(Textos.obtener(R.string.texto_ya_existe_una_cuenta_con_este_correo))
        if (!Validaciones.telefono(telefono)) throw IllegalArgumentException(Textos.obtener(R.string.error_telefono_formato))
    }

    fun registrar(nombre: String, email: String, contrasena: String, rol: Rol, telefono: String? = null, negocioId: String? = null): Cuenta {
        validarRegistro(nombre, email, contrasena, telefono)
        val correo = email.trim().lowercase()
        val cuenta = Cuenta(uid(), nombre.trim(), correo, contrasena, rol, telefono?.trim()?.ifBlank { null }, negocioId)
        _cuentas.value = _cuentas.value + cuenta
        return cuenta
    }

    fun actualizar(cuenta: Cuenta) {
        _cuentas.value = _cuentas.value.map { if (it.id == cuenta.id) cuenta else it }
    }

    fun restablecerContrasena(email: String, nuevaContrasena: String) {
        val cuenta = _cuentas.value.firstOrNull { it.email == email.trim().lowercase() }
            ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_no_existe_una_cuenta_con_este_correo))
        if (!Validaciones.contrasena(nuevaContrasena)) throw IllegalArgumentException(Textos.obtener(R.string.texto_la_contrasena_debe_tener_al_menos_6_caracteres))
        _cuentas.value = _cuentas.value.map { if (it.id == cuenta.id) it.copy(contrasena = nuevaContrasena) else it }
    }
}
