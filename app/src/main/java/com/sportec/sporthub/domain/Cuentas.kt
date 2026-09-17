package com.sportec.sporthub.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Cuentas mutables: arranca con los datos de demostración y suma las registradas en sesión (HU-C01, HU-C02). */
object Cuentas {

    private var secuencia = 1000
    private fun uid() = "u" + (++secuencia)

    private val _cuentas = MutableStateFlow(DatosMock.cuentas)
    val cuentas: StateFlow<List<Cuenta>> = _cuentas.asStateFlow()

    fun todas(): List<Cuenta> = _cuentas.value

    fun porId(id: String): Cuenta? = _cuentas.value.firstOrNull { it.id == id }

    /** Nombre a mostrar: la razón social del negocio para cuentas de establecimiento, nunca el nombre de la persona de contacto. */
    fun nombreParaMostrar(cuenta: Cuenta): String =
        if (cuenta.rol == Rol.ESTABLECIMIENTO) {
            cuenta.negocioId?.let { Negocios.negocioDe(it)?.nombre } ?: cuenta.nombre
        } else {
            cuenta.nombre
        }

    fun registrar(nombre: String, email: String, contrasena: String, rol: Rol, telefono: String? = null, negocioId: String? = null): Cuenta {
        val correo = email.trim().lowercase()
        if (nombre.isBlank()) throw IllegalArgumentException("El nombre es obligatorio.")
        if (!correo.contains("@") || !correo.contains(".")) throw IllegalArgumentException("Ingresa un correo válido.")
        if (contrasena.length < 6) throw IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.")
        if (_cuentas.value.any { it.email == correo }) throw IllegalStateException("Ya existe una cuenta con este correo.")
        val cuenta = Cuenta(uid(), nombre.trim(), correo, contrasena, rol, telefono?.trim()?.ifBlank { null }, negocioId)
        _cuentas.value = _cuentas.value + cuenta
        return cuenta
    }

    fun actualizar(cuenta: Cuenta) {
        _cuentas.value = _cuentas.value.map { if (it.id == cuenta.id) cuenta else it }
    }

    fun restablecerContrasena(email: String, nuevaContrasena: String) {
        val cuenta = _cuentas.value.firstOrNull { it.email == email.trim().lowercase() }
            ?: throw IllegalArgumentException("No existe una cuenta con este correo.")
        if (nuevaContrasena.length < 6) throw IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.")
        _cuentas.value = _cuentas.value.map { if (it.id == cuenta.id) it.copy(contrasena = nuevaContrasena) else it }
    }
}
