package com.sportec.sporthub.ui.screens.comun

import androidx.lifecycle.ViewModel
import com.sportec.sporthub.domain.Comunidades
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CrearComunidadUiState(
    val nombre: String = "",
    val descripcion: String = "",
    val error: String? = null,
    val creadaId: String? = null
)

class CrearComunidadViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CrearComunidadUiState())
    val uiState: StateFlow<CrearComunidadUiState> = _uiState.asStateFlow()

    fun onNombreCambiado(v: String) = _uiState.update { it.copy(nombre = v, error = null) }
    fun onDescripcionCambiada(v: String) = _uiState.update { it.copy(descripcion = v, error = null) }

    fun crear(creadorId: String) {
        val estado = _uiState.value
        try {
            val comunidad = Comunidades.crear(estado.nombre, estado.descripcion, creadorId)
            _uiState.update { it.copy(creadaId = comunidad.id) }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message ?: "No se pudo crear la comunidad.") }
        }
    }
}
