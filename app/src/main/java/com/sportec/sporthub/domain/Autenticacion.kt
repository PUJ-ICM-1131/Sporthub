package com.sportec.sporthub.domain

import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos
import com.sportec.sporthub.utils.Validaciones
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

object Autenticacion {

    const val COLECCION_USUARIOS = "usuarios"
    private const val ETIQUETA = "Autenticacion"

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    fun uidActual(): String? = auth.currentUser?.uid

    suspend fun iniciarSesion(correo: String, contrasena: String): Cuenta {
        try {
            val uid = auth.signInWithEmailAndPassword(correo.trim().lowercase(), contrasena).await().user?.uid
                ?: throw IllegalStateException(Textos.obtener(R.string.texto_no_fue_posible_iniciar_sesion))
            val cuenta = Cuentas.cargarPerfil(uid)
            if (cuenta == null || !cuenta.activa) {
                auth.signOut()
                throw IllegalStateException(Textos.obtener(R.string.texto_esta_cuenta_no_tiene_acceso))
            }
            return cuenta
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw traducir(e)
        }
    }

    suspend fun registrar(
        nombre: String,
        email: String,
        contrasena: String,
        rol: Rol,
        telefono: String? = null,
        negocioId: String? = null
    ): Cuenta {
        Cuentas.validarRegistro(nombre, email, contrasena, telefono)
        val correo = email.trim().lowercase()
        val tel = telefono?.trim()?.ifBlank { null }
        try {
            val usuario = auth.createUserWithEmailAndPassword(correo, contrasena).await().user
                ?: throw IllegalStateException(Textos.obtener(R.string.texto_no_se_pudo_crear_la_cuenta))
            val perfil = hashMapOf<String, Any?>(
                "nombre" to nombre.trim(),
                "email" to correo,
                "telefono" to tel,
                "rol" to rol.name,
                "negocioId" to negocioId,
                "activa" to true,
                "creadoEn" to FieldValue.serverTimestamp()
            )
            try {
                db.collection(COLECCION_USUARIOS).document(usuario.uid).set(perfil).await()
            } catch (e: Exception) {
                runCatching { usuario.delete().await() }
                throw e
            }
            auth.signOut()
            return Cuenta(
                id = usuario.uid, nombre = nombre.trim(), email = correo, rol = rol,
                telefono = tel, negocioId = negocioId, uid = usuario.uid
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw traducir(e)
        }
    }

    suspend fun enviarRecuperacion(email: String) {
        if (!Validaciones.correo(email)) {
            throw IllegalArgumentException(Textos.obtener(R.string.texto_ingresa_un_correo_valido))
        }
        try {
            auth.sendPasswordResetEmail(email.trim().lowercase()).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw traducir(e)
        }
    }

    fun cerrarSesion() {
        auth.signOut()
    }

    private fun traducir(e: Exception): Exception {
        Log.w(ETIQUETA, "Fallo de autenticación: ${e.javaClass.simpleName}", e)
        return when (e) {
            is IllegalArgumentException, is IllegalStateException -> e
            is FirebaseAuthWeakPasswordException ->
                IllegalArgumentException(Textos.obtener(R.string.texto_la_contrasena_debe_tener_al_menos_6_caracteres))
            is FirebaseAuthUserCollisionException ->
                IllegalStateException(Textos.obtener(R.string.texto_ya_existe_una_cuenta_con_este_correo))
            is FirebaseAuthInvalidUserException ->
                if (e.errorCode == "ERROR_USER_DISABLED") IllegalStateException(Textos.obtener(R.string.texto_esta_cuenta_no_tiene_acceso))
                else IllegalArgumentException(Textos.obtener(R.string.texto_correo_o_contrasena_incorrectos))
            is FirebaseAuthInvalidCredentialsException ->
                IllegalArgumentException(Textos.obtener(R.string.texto_correo_o_contrasena_incorrectos))
            is FirebaseNetworkException ->
                IllegalStateException(Textos.obtener(R.string.texto_el_servicio_no_esta_disponible_intenta_de_nuevo))
            else ->
                IllegalStateException(Textos.obtener(R.string.texto_el_servicio_no_esta_disponible_intenta_de_nuevo))
        }
    }
}
