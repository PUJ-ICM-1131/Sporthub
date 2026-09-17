package com.sportec.sporthub.ui.screens.acceso

import androidx.lifecycle.ViewModel
import com.sportec.sporthub.domain.Cuentas
import com.sportec.sporthub.domain.Negocios
import com.sportec.sporthub.domain.Rol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RegistroEstablecimientoUiState(
    val nombreContacto: String = "",
    val email: String = "",
    val telefono: String = "",
    val contrasena: String = "",
    val nombreNegocio: String = "",
    val nit: String = "",
    val direccion: String = "",
    val error: String? = null,
    val registrado: Boolean = false
)

class RegistroEstablecimientoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroEstablecimientoUiState())
    val uiState: StateFlow<RegistroEstablecimientoUiState> = _uiState.asStateFlow()

    fun onNombreContactoCambiado(v: String) = _uiState.update { it.copy(nombreContacto = v, error = null) }
    fun onEmailCambiado(v: String) = _uiState.update { it.copy(email = v, error = null) }
    fun onTelefonoCambiado(v: String) = _uiState.update { it.copy(telefono = v, error = null) }
    fun onContrasenaCambiada(v: String) = _uiState.update { it.copy(contrasena = v, error = null) }
    fun onNombreNegocioCambiado(v: String) = _uiState.update { it.copy(nombreNegocio = v, error = null) }
    fun onNitCambiado(v: String) = _uiState.update { it.copy(nit = v, error = null) }
    fun onDireccionCambiada(v: String) = _uiState.update { it.copy(direccion = v, error = null) }

    fun registrar() {
        val estado = _uiState.value
        try {
            val negocio = Negocios.crear(estado.nombreNegocio, estado.nit, estado.telefono, estado.direccion)
            Cuentas.registrar(estado.nombreContacto, estado.email, estado.contrasena, Rol.ESTABLECIMIENTO, estado.telefono, negocio.id)
            _uiState.update { it.copy(registrado = true) }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message ?: "No se pudo crear la cuenta.") }
        }
    }
}
