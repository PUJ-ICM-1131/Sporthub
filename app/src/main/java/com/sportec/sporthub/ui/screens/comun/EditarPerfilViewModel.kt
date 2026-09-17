package com.sportec.sporthub.ui.screens.comun

import androidx.lifecycle.ViewModel
import com.sportec.sporthub.domain.Cuenta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class EditarPerfilUiState(
    val nombre: String = "",
    val telefono: String = "",
    val fotoUri: String? = null,
    val error: String? = null,
    val guardada: Cuenta? = null
)

class EditarPerfilViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EditarPerfilUiState())
    val uiState: StateFlow<EditarPerfilUiState> = _uiState.asStateFlow()

    fun cargar(cuenta: Cuenta) {
        if (_uiState.value.guardada != null) return
        _uiState.value = EditarPerfilUiState(nombre = cuenta.nombre, telefono = cuenta.telefono.orEmpty(), fotoUri = cuenta.fotoUri)
    }

    fun onNombreCambiado(v: String) = _uiState.update { it.copy(nombre = v, error = null) }
    fun onTelefonoCambiado(v: String) = _uiState.update { it.copy(telefono = v, error = null) }
    fun onFotoCambiada(v: String) = _uiState.update { it.copy(fotoUri = v) }

    fun guardar(cuenta: Cuenta) {
        val estado = _uiState.value
        if (estado.nombre.isBlank()) {
            _uiState.update { it.copy(error = "El nombre no puede estar vacío.") }
            return
        }
        _uiState.update {
            it.copy(guardada = cuenta.copy(nombre = estado.nombre.trim(), telefono = estado.telefono.trim().ifBlank { null }, fotoUri = estado.fotoUri))
        }
    }
}
