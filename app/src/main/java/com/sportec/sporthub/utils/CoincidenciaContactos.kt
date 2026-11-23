package com.sportec.sporthub.utils

data class ContactoAgenda(
    val nombre: String,
    val correos: List<String> = emptyList(),
    val telefonos: List<String> = emptyList()
)

data class CuentaContactable(
    val id: String,
    val correo: String,
    val telefono: String? = null
)

object CoincidenciaContactos {

    fun normalizarCorreo(valor: String?): String = valor?.trim()?.lowercase().orEmpty()

    fun normalizarTelefono(valor: String?): String? {
        val digitos = valor?.filter(Char::isDigit).orEmpty()
        if (digitos.length < 7) return null
        return digitos.takeLast(10)
    }

    fun emparejar(
        cuentas: List<CuentaContactable>,
        contactos: List<ContactoAgenda>
    ): Map<String, String> {
        if (cuentas.isEmpty() || contactos.isEmpty()) return emptyMap()
        val porCorreo = HashMap<String, String>()
        val porTelefono = HashMap<String, String>()
        contactos.forEach { contacto ->
            val nombre = contacto.nombre.trim()
            if (nombre.isEmpty()) return@forEach
            contacto.correos.forEach { correo ->
                normalizarCorreo(correo).takeIf { it.isNotEmpty() }?.let { porCorreo.putIfAbsent(it, nombre) }
            }
            contacto.telefonos.forEach { telefono ->
                normalizarTelefono(telefono)?.let { porTelefono.putIfAbsent(it, nombre) }
            }
        }
        return cuentas.mapNotNull { cuenta ->
            val nombre = porCorreo[normalizarCorreo(cuenta.correo)]
                ?: normalizarTelefono(cuenta.telefono)?.let { porTelefono[it] }
            nombre?.let { cuenta.id to it }
        }.toMap()
    }
}
