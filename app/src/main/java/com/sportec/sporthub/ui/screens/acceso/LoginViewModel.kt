package com.sportec.sporthub.ui.screens.acceso

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportec.sporthub.R
import com.sportec.sporthub.domain.Autenticacion
import com.sportec.sporthub.domain.Cuenta
import com.sportec.sporthub.utils.Textos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

data class LoginUiState(
    val usuario: String = "",
    val contrasena: String = "",
    val error: String? = null,
    val cargando: Boolean = false,
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

    fun iniciarSesion() {
        val estado = _uiState.value
        if (estado.cargando) return
        if (estado.usuario.isBlank() || estado.contrasena.isBlank()) {
            _uiState.update { it.copy(error = Textos.obtener(R.string.texto_ingresa_tu_correo_o_usuario_y_tu_contrasena)) }
            return
        }
        _uiState.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            try {
                val cuenta = Autenticacion.iniciarSesion(estado.usuario, estado.contrasena)
                _uiState.update { it.copy(contrasena = "", cargando = false, cuentaAutenticada = cuenta) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = e.message ?: Textos.obtener(R.string.texto_no_fue_posible_iniciar_sesion))
                }
            }
        }
    }

    fun autenticacionAtendida() {
        _uiState.update { it.copy(cuentaAutenticada = null) }
    }
}
