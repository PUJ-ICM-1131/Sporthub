package com.sportec.sporthub.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportec.sporthub.domain.Autenticacion
import com.sportec.sporthub.domain.Cuenta
import com.sportec.sporthub.domain.Cuentas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class SesionViewModel : ViewModel() {

    private val _cuenta = MutableStateFlow<Cuenta?>(null)
    val cuenta: StateFlow<Cuenta?> = _cuenta.asStateFlow()

    private val _restaurando = MutableStateFlow(Autenticacion.uidActual() != null)
    val restaurando: StateFlow<Boolean> = _restaurando.asStateFlow()

    init {
        val uid = Autenticacion.uidActual()
        if (uid != null) {
            viewModelScope.launch {
                try {
                    val perfil = Cuentas.cargarPerfil(uid)
                    if (perfil != null && perfil.activa) iniciar(perfil) else Autenticacion.cerrarSesion()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.w("SesionViewModel", "No se pudo restaurar la sesión", e)
                } finally {
                    _restaurando.value = false
                }
            }
        }
    }

    fun iniciar(cuenta: Cuenta) {
        _cuenta.value = cuenta
        Cuentas.iniciarEscucha(viewModelScope, cuenta)
    }

    fun actualizar(cuenta: Cuenta) {
        Cuentas.actualizar(cuenta)
        _cuenta.value = cuenta
        viewModelScope.launch {
            try {
                Cuentas.guardarPerfil(cuenta)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w("SesionViewModel", "No se pudo guardar el perfil en Firestore", e)
            }
        }
    }

    fun cerrar() {
        Cuentas.detenerEscucha()
        Autenticacion.cerrarSesion()
        _cuenta.value = null
    }
}
