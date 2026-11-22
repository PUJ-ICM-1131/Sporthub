package com.sportec.sporthub.utils

import android.content.Context
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object Agenda {

    private class Acumulado(var nombre: String) {
        val correos = mutableListOf<String>()
        val telefonos = mutableListOf<String>()
    }

    suspend fun leer(context: Context): List<ContactoAgenda> = withContext(Dispatchers.IO) {
        val resolutor = context.applicationContext.contentResolver
        val porContacto = LinkedHashMap<String, Acumulado>()

        fun recorrer(uri: android.net.Uri, columnaValor: String, destino: (Acumulado, String) -> Unit) {
            val proyeccion = arrayOf(
                ContactsContract.Data.CONTACT_ID,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                columnaValor
            )
            resolutor.query(uri, proyeccion, null, null, null)?.use { cursor ->
                val columnaId = cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID)
                val columnaNombre = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                val columnaDato = cursor.getColumnIndex(columnaValor)
                if (columnaId < 0 || columnaNombre < 0 || columnaDato < 0) return@use
                while (cursor.moveToNext()) {
                    val id = cursor.getString(columnaId) ?: continue
                    val valor = cursor.getString(columnaDato)?.trim().orEmpty()
                    if (valor.isEmpty()) continue
                    val nombre = cursor.getString(columnaNombre)?.trim().orEmpty()
                    val acumulado = porContacto.getOrPut(id) { Acumulado(nombre) }
                    if (acumulado.nombre.isEmpty() && nombre.isNotEmpty()) acumulado.nombre = nombre
                    destino(acumulado, valor)
                }
            }
        }

        recorrer(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            ContactsContract.CommonDataKinds.Email.ADDRESS
        ) { acumulado, valor -> acumulado.correos.add(valor) }

        recorrer(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        ) { acumulado, valor -> acumulado.telefonos.add(valor) }

        porContacto.values
            .filter { it.nombre.isNotEmpty() && (it.correos.isNotEmpty() || it.telefonos.isNotEmpty()) }
            .map { ContactoAgenda(it.nombre, it.correos.toList(), it.telefonos.toList()) }
    }
}
