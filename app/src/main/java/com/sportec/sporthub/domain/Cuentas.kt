package com.sportec.sporthub.domain

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.sportec.sporthub.R
import com.sportec.sporthub.utils.FotosLocales
import com.sportec.sporthub.utils.Textos
import com.sportec.sporthub.utils.Validaciones
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.tasks.await

object Cuentas {

    private const val ETIQUETA = "Cuentas"

    private val _cuentas = MutableStateFlow<List<Cuenta>>(emptyList())
    val cuentas: StateFlow<List<Cuenta>> = _cuentas.asStateFlow()

    private var escucha: Job? = null

    fun todas(): List<Cuenta> = _cuentas.value

    fun porId(id: String): Cuenta? = _cuentas.value.firstOrNull { it.id == id }

    fun nombreParaMostrar(cuenta: Cuenta): String =
        if (cuenta.rol == Rol.ESTABLECIMIENTO) {
            cuenta.negocioId?.let { Negocios.negocioDe(it)?.nombre } ?: cuenta.nombre
        } else {
            cuenta.nombre
        }

    private fun aCuenta(doc: DocumentSnapshot): Cuenta? {
        val rol = doc.getString("rol")?.let { runCatching { Rol.valueOf(it) }.getOrNull() } ?: return null
        val id = doc.getString("idApp") ?: doc.id
        return Cuenta(
            id = id,
            nombre = doc.getString("nombre").orEmpty(),
            email = doc.getString("email").orEmpty(),
            rol = rol,
            telefono = doc.getString("telefono"),
            negocioId = doc.getString("negocioId"),
            activa = doc.getBoolean("activa") ?: true,
            fotoUri = FotosLocales.obtener("cuenta", id),
            uid = doc.id
        )
    }

    suspend fun cargarPerfil(uid: String): Cuenta? {
        val doc = FirebaseFirestore.getInstance()
            .collection(Autenticacion.COLECCION_USUARIOS).document(uid).get().await()
        return if (doc.exists()) aCuenta(doc) else null
    }

    fun iniciarEscucha(scope: CoroutineScope, actual: Cuenta) {
        if (_cuentas.value.none { it.id == actual.id }) _cuentas.value = _cuentas.value + actual
        escucha?.cancel()
        escucha = FirebaseFirestore.getInstance()
            .collection(Autenticacion.COLECCION_USUARIOS)
            .snapshots()
            .map { instantanea -> instantanea.documents.mapNotNull { aCuenta(it) } }
            .onEach { _cuentas.value = it }
            .catch { Log.w(ETIQUETA, "No se pudo escuchar la colección de usuarios", it) }
            .launchIn(scope)
    }

    fun detenerEscucha() {
        escucha?.cancel()
        escucha = null
        _cuentas.value = emptyList()
    }

    fun validarRegistro(nombre: String, email: String, contrasena: String, telefono: String? = null) {
        val correo = email.trim().lowercase()
        if (nombre.isBlank()) throw IllegalArgumentException(Textos.obtener(R.string.texto_el_nombre_es_obligatorio))
        if (!Validaciones.correo(correo)) throw IllegalArgumentException(Textos.obtener(R.string.texto_ingresa_un_correo_valido))
        if (!Validaciones.contrasena(contrasena)) throw IllegalArgumentException(Textos.obtener(R.string.texto_la_contrasena_debe_tener_al_menos_6_caracteres))
        if (!Validaciones.telefono(telefono)) throw IllegalArgumentException(Textos.obtener(R.string.error_telefono_formato))
    }

    fun actualizar(cuenta: Cuenta) {
        FotosLocales.asociar("cuenta", cuenta.id, cuenta.fotoUri)
        _cuentas.value = _cuentas.value.map { if (it.id == cuenta.id) cuenta else it }
    }

    suspend fun guardarPerfil(cuenta: Cuenta) {
        if (cuenta.uid.isBlank()) return
        FirebaseFirestore.getInstance()
            .collection(Autenticacion.COLECCION_USUARIOS).document(cuenta.uid)
            .update(mapOf("nombre" to cuenta.nombre, "telefono" to cuenta.telefono))
            .await()
    }
}
