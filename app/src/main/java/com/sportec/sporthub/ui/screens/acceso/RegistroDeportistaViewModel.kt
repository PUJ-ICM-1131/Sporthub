package com.sportec.sporthub.ui.screens.acceso

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos

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
    val confirmaMayoriaEdad: Boolean = false,
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

    fun onMayoriaEdadCambiada(valor: Boolean) =
        _uiState.update { it.copy(confirmaMayoriaEdad = valor, error = null) }

    fun registrar() {
        val estado = _uiState.value
        if (!estado.confirmaMayoriaEdad) {
            _uiState.update { it.copy(error = Textos.obtener(R.string.error_mayoria_edad)) }
            return
        }
        try {
            Cuentas.registrar(estado.nombre, estado.email, estado.contrasena, Rol.DEPORTISTA, estado.telefono)
            _uiState.update { it.copy(registrado = true) }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message ?: Textos.obtener(R.string.texto_no_se_pudo_crear_la_cuenta)) }
        }
    }
}
