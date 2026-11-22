package com.sportec.sporthub.data

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.Blob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import kotlin.coroutines.cancellation.CancellationException

object FotosRemotas {
    private const val ETIQUETA = "FotosRemotas"
    const val PREFIJO = "fr:"
    const val PREFIJO_PRIVADA = "fp:"
    const val COLECCION = "fotos"
    const val COLECCION_PRIVADA = "fotosPrivadas"
    private const val LADO_MAXIMO = 1280
    private const val LIMITE_BYTES = 700 * 1024

    private var carpeta: File? = null
    private var resolver: ContentResolver? = null
    private val enCurso = mutableSetOf<String>()

    private val _version = MutableStateFlow(0)

    val version: StateFlow<Int> = _version.asStateFlow()

    fun iniciar(context: Context) {
        val app = context.applicationContext
        carpeta = File(app.filesDir, "fotografias_compartidas").also { it.mkdirs() }
        resolver = app.contentResolver
    }

    fun esReferencia(valor: String?) = valor?.let { it.startsWith(PREFIJO) || it.startsWith(PREFIJO_PRIVADA) } == true

    fun resolver(referencia: String): String? {
        if (!esReferencia(referencia)) return referencia
        val privada = referencia.startsWith(PREFIJO_PRIVADA)
        val id = referencia.substringAfter(":")
        val archivo = archivoDe(id) ?: return null
        if (archivo.isFile && archivo.length() > 0) return Uri.fromFile(archivo).toString()
        descargar(id, privada)
        return null
    }

    private fun archivoDe(id: String): File? = carpeta?.let { File(it, "$id.jpg") }

    private fun descargar(id: String, privada: Boolean) {
        if (Nube.uid() == null || !enCurso.add(id)) return
        Nube.alcance.launch {
            try {
                val doc = Nube.db.collection(if (privada) COLECCION_PRIVADA else COLECCION).document(id).get().await()
                val bytes = doc.getBlob("datos")?.toBytes()
                val archivo = archivoDe(id)
                if (bytes != null && archivo != null) {
                    withContext(Dispatchers.IO) { archivo.writeBytes(bytes) }
                    _version.value = _version.value + 1
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(ETIQUETA, "No se pudo descargar la foto $id", e)
            } finally {
                enCurso.remove(id)
            }
        }
    }

    suspend fun subir(uri: String, reservaId: String? = null): String {
        if (esReferencia(uri)) return uri
        val bytes = withContext(Dispatchers.IO) { comprimir(uri) }
        val id = withContext(Dispatchers.Default) {
            MessageDigest.getInstance("SHA-256").digest((reservaId.orEmpty().toByteArray() + bytes)).joinToString("") { "%02x".format(it) }
        }
        val ref = Nube.db.collection(if (reservaId == null) COLECCION else COLECCION_PRIVADA).document(id)
        if (!ref.get().await().exists()) {
            ref.set(
                mapOf(
                    "datos" to Blob.fromBytes(bytes),
                    "subidaPor" to Nube.uid(),
                    "creadaEn" to Nube.ahora(),
                    "reservaId" to reservaId
                )
            ).await()
        }
        archivoDe(id)?.let { destino ->
            if (!destino.isFile) withContext(Dispatchers.IO) { destino.writeBytes(bytes) }
        }
        return (if (reservaId == null) PREFIJO else PREFIJO_PRIVADA) + id
    }

    suspend fun subirTodas(uris: List<String>): List<String> = uris.map { subir(it) }

    private fun comprimir(uri: String): ByteArray {
        val resolutor = resolver ?: throw IOException()
        val destino = Uri.parse(uri)
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        (resolutor.openInputStream(destino) ?: throw IOException()).use {
            BitmapFactory.decodeStream(it, null, limites)
        }
        if (limites.outWidth <= 0 || limites.outHeight <= 0) throw IOException()
        var muestreo = 1
        while (maxOf(limites.outWidth, limites.outHeight) / muestreo > LADO_MAXIMO * 2) muestreo *= 2
        val opciones = BitmapFactory.Options().apply { inSampleSize = muestreo }
        val original = resolutor.openInputStream(destino)?.use { BitmapFactory.decodeStream(it, null, opciones) }
            ?: throw IOException()
        val escala = LADO_MAXIMO.toFloat() / maxOf(original.width, original.height)
        val imagen = if (escala < 1f) {
            Bitmap.createScaledBitmap(original, (original.width * escala).toInt().coerceAtLeast(1),
                (original.height * escala).toInt().coerceAtLeast(1), true)
        } else original
        for (calidad in intArrayOf(80, 65, 50, 35)) {
            val salida = ByteArrayOutputStream()
            imagen.compress(Bitmap.CompressFormat.JPEG, calidad, salida)
            if (salida.size() <= LIMITE_BYTES) return salida.toByteArray()
        }
        throw IOException()
    }
}
