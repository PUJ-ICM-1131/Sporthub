package com.sportec.sporthub.ui.screens.acceso

import androidx.lifecycle.ViewModel
import com.sportec.sporthub.domain.Autenticacion
import com.sportec.sporthub.domain.Cuenta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoginUiState(
    val usuario: String = "",
    val contrasena: String = "",
    val error: String? = null,
    val simularError: Boolean = false,
    val cuentaAutenticada: Cuenta? = null
)

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsuarioCambiado(valor: String) {
        _uiState.update { it.copy(usuario = valor, error = null) }
    }

    fun onContrasenaCambiada(valor: String) {
        _uiState.update { it.copy(contrasena = valor, error = null) }
    }

    fun onSimularErrorCambiado(valor: Boolean) {
        _uiState.update { it.copy(simularError = valor) }
    }

    fun usarCuentaDemo(cuenta: Cuenta) {
        _uiState.update { it.copy(usuario = cuenta.id, contrasena = cuenta.contrasena, error = null) }
    }

    fun iniciarSesion() {
        val estado = _uiState.value
        if (estado.usuario.isBlank() || estado.contrasena.isBlank()) {
            _uiState.update { it.copy(error = "Ingresa tu correo o usuario y tu contraseña.") }
            return
        }
        try {
            val cuenta = Autenticacion.iniciarSesion(estado.usuario, estado.contrasena, estado.simularError)
            _uiState.update { it.copy(contrasena = "", error = null, cuentaAutenticada = cuenta) }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message ?: "No fue posible iniciar sesión.") }
        }
    }

    fun autenticacionAtendida() {
        _uiState.update { it.copy(cuentaAutenticada = null) }
    }
}
