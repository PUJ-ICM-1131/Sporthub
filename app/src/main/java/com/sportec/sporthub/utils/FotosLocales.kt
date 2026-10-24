package com.sportec.sporthub.utils

import android.content.Context
import android.content.ContentResolver
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.IOException

object FotosLocales {
    private lateinit var carpeta: File
    private lateinit var resolver: ContentResolver
    private lateinit var preferencias: SharedPreferences

    fun iniciar(context: Context) {
        val aplicacion = context.applicationContext
        carpeta = File(aplicacion.filesDir, "fotografias")
        resolver = aplicacion.contentResolver
        preferencias = aplicacion.getSharedPreferences("fotografias", Context.MODE_PRIVATE)
    }

    fun importar(uri: Uri): String {
        if (!carpeta.exists() && !carpeta.mkdirs()) throw IOException()
        val archivo = File.createTempFile("foto_", ".img", carpeta)
        try {
            val entrada = resolver.openInputStream(uri) ?: throw IOException()
            entrada.use { origen -> archivo.outputStream().use { origen.copyTo(it) } }
            val opciones = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(archivo.path, opciones)
            if (opciones.outWidth <= 0 || opciones.outHeight <= 0) throw IOException()
            return Uri.fromFile(archivo).toString()
        } catch (e: Exception) {
            archivo.delete()
            throw e
        }
    }

    fun guardarGaleria(id: String, fotos: List<String>) {
        preferencias.edit().putString("galeria:$id", org.json.JSONArray(fotos).toString()).apply()
        asociar("servicio", id, fotos.firstOrNull())
    }

    fun obtenerGaleria(id: String): List<String> {
        val guardada = preferencias.getString("galeria:$id", null)
            ?: return listOfNotNull(obtener("servicio", id))
        return try {
            val lista = org.json.JSONArray(guardada)
            (0 until lista.length()).map { lista.getString(it) }.filter { uri ->
                val archivo = Uri.parse(uri).path?.let { File(it) }
                archivo != null && archivo.parentFile == carpeta && archivo.isFile && archivo.length() > 0
            }
        } catch (_: org.json.JSONException) { emptyList() }
    }

    fun asociar(tipo: String, id: String, uri: String?) {
        preferencias.edit().putString("$tipo:$id", uri).apply()
    }

    fun obtener(tipo: String, id: String): String? {
        val uri = preferencias.getString("$tipo:$id", null) ?: return null
        val ruta = Uri.parse(uri).path ?: return null
        val archivo = File(ruta)
        return if (archivo.parentFile == carpeta && archivo.isFile && archivo.length() > 0) uri else null
    }
}
