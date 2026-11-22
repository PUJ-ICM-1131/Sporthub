package com.sportec.sporthub.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Transaction
import com.google.firebase.firestore.WriteBatch
import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

sealed class Escritura {

    data class Crear(val ruta: String, val datos: Map<String, Any?>) : Escritura()

    data class Mezclar(val ruta: String, val datos: Map<String, Any?>) : Escritura()

    data class Actualizar(val ruta: String, val datos: Map<String, Any?>) : Escritura()
    data class Borrar(val ruta: String) : Escritura()
}

sealed class Condicion {
    abstract val ruta: String

    data class Igual(override val ruta: String, val campo: String, val esperado: Any?) : Condicion()

    data class Menor(override val ruta: String, val campo: String, val limite: Long) : Condicion()
}

class ConflictoException(mensaje: String) : Exception(mensaje)

object Nube {
    private const val ETIQUETA = "Nube"

    val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    fun uid(): String? = FirebaseAuth.getInstance().currentUser?.uid

    val alcance = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _errores = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val errores: SharedFlow<String> = _errores

    private class Grupo {
        val escrituras = mutableListOf<Escritura>()
        val condiciones = mutableListOf<Condicion>()
    }

    private var grupo: Grupo? = null

    fun escribir(vararg e: Escritura) {
        val actual = grupo
        if (actual != null) actual.escrituras.addAll(e) else confirmar(e.toList(), emptyList())
    }

    fun <T> agrupar(vararg condiciones: Condicion, bloque: () -> T): T {
        val externo = grupo
        if (externo != null) {
            externo.condiciones.addAll(condiciones)
            return bloque()
        }
        val nuevo = Grupo().also { it.condiciones.addAll(condiciones) }
        grupo = nuevo
        val resultado = try {
            bloque()
        } finally {
            grupo = null
        }
        confirmar(nuevo.escrituras, nuevo.condiciones)
        return resultado
    }

    suspend fun <T> confirmarOperacion(bloque: () -> T): T {
        check(grupo == null)
        check(uid() != null) { Textos.obtener(R.string.nube_sin_permiso) }
        val nuevo = Grupo()
        grupo = nuevo
        val resultado = try { bloque() } finally { grupo = null }
        try {
            persistir(nuevo.escrituras, nuevo.condiciones)
        } catch (e: CancellationException) {
            Sincronizacion.reiniciar()
            throw e
        } catch (e: Exception) {
            Sincronizacion.reiniciar()
            throw IllegalStateException(traducir(e), e)
        }
        return resultado
    }

    private fun confirmar(escrituras: List<Escritura>, condiciones: List<Condicion>) {
        if (escrituras.isEmpty()) return
        alcance.launch {
            try {
                persistir(escrituras, condiciones)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(ETIQUETA, "Falló la escritura agrupada", e)
                _errores.emit(traducir(e))
                Sincronizacion.reiniciar()
            }
        }
    }

    private suspend fun persistir(escrituras: List<Escritura>, condiciones: List<Condicion>) {
        check(uid() != null) { Textos.obtener(R.string.nube_sin_permiso) }
        if (escrituras.isEmpty()) return
        if (condiciones.isEmpty()) {
            val lote = db.batch()
            escrituras.forEach { aplicar(lote, it) }
            lote.commit().await()
        } else {
            db.runTransaction(Transaction.Function<Void?> { tx ->
                condiciones.forEach { verificar(tx, it) }
                escrituras.forEach { aplicar(tx, it) }
                null
            }).await()
        }
    }

    private fun traducir(e: Exception): String {
        val causa = if (e is ConflictoException) e else e.cause as? ConflictoException
        if (causa != null) return causa.message ?: Textos.obtener(R.string.nube_conflicto)
        return when ((e as? FirebaseFirestoreException)?.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> Textos.obtener(R.string.nube_sin_permiso)
            FirebaseFirestoreException.Code.ABORTED, FirebaseFirestoreException.Code.FAILED_PRECONDITION ->
                Textos.obtener(R.string.nube_conflicto)
            else -> Textos.obtener(R.string.nube_sin_conexion)
        }
    }

    private fun verificar(tx: Transaction, c: Condicion) {
        val doc = tx.get(db.document(c.ruta))
        when (c) {
            is Condicion.Igual -> {
                val actual = if (doc.exists()) doc.get(c.campo) else null
                val coincide = if (actual is Number && c.esperado is Number) actual.toLong() == c.esperado.toLong()
                else actual == c.esperado
                if (!coincide) throw ConflictoException(Textos.obtener(R.string.nube_conflicto))
            }
            is Condicion.Menor -> {
                val actual = if (doc.exists()) doc.getLong(c.campo) ?: 0L else 0L
                if (actual >= c.limite) throw ConflictoException(Textos.obtener(R.string.nube_sin_cupos))
            }
        }
    }

    private fun aplicar(lote: WriteBatch, e: Escritura) {
        when (e) {
            is Escritura.Crear -> lote.set(db.document(e.ruta), e.datos)
            is Escritura.Mezclar -> lote.set(db.document(e.ruta), e.datos, SetOptions.merge())
            is Escritura.Actualizar -> lote.update(db.document(e.ruta), e.datos)
            is Escritura.Borrar -> lote.delete(db.document(e.ruta))
        }
    }

    private fun aplicar(tx: Transaction, e: Escritura) {
        when (e) {
            is Escritura.Crear -> tx.set(db.document(e.ruta), e.datos)
            is Escritura.Mezclar -> tx.set(db.document(e.ruta), e.datos, SetOptions.merge())
            is Escritura.Actualizar -> tx.update(db.document(e.ruta), e.datos)
            is Escritura.Borrar -> tx.delete(db.document(e.ruta))
        }
    }

    suspend fun informarError(e: Exception) {
        _errores.emit(traducir(e))
    }

    fun ejecutar(mensajeError: Int, bloque: suspend () -> Unit) {
        alcance.launch {
            try { bloque() }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                Log.w(ETIQUETA, "No se pudo completar la operacion", e)
                _errores.emit(Textos.obtener(mensajeError))
            }
        }
    }

    fun ahora(): Any = FieldValue.serverTimestamp()
    fun sumar(delta: Long): Any = FieldValue.increment(delta)
}
