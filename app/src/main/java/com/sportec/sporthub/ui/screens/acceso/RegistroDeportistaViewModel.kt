package com.sportec.sporthub.ui.screens.acceso

import androidx.lifecycle.ViewModel
import com.sportec.sporthub.domain.Cuentas
import com.sportec.sporthub.domain.Rol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RegistroDeportistaUiState(
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val contrasena: String = "",
    val error: String? = null,
    val registrado: Boolean = false
)

class RegistroDeportistaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroDeportistaUiState())
    val uiState: StateFlow<RegistroDeportistaUiState> = _uiState.asStateFlow()

    fun onNombreCambiado(v: String) = _uiState.update { it.copy(nombre = v, error = null) }
    fun onEmailCambiado(v: String) = _uiState.update { it.copy(email = v, error = null) }
    fun onTelefonoCambiado(v: String) = _uiState.update { it.copy(telefono = v, error = null) }
    fun onContrasenaCambiada(v: String) = _uiState.update { it.copy(contrasena = v, error = null) }

    fun registrar() {
        val estado = _uiState.value
        try {
            Cuentas.registrar(estado.nombre, estado.email, estado.contrasena, Rol.DEPORTISTA, estado.telefono)
            _uiState.update { it.copy(registrado = true) }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message ?: "No se pudo crear la cuenta.") }
        }
    }
}
