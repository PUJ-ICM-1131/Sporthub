package com.sportec.sporthub.ui.screens.establecimiento

import androidx.lifecycle.ViewModel
import com.sportec.sporthub.domain.Negocios
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class EditarNegocioUiState(
    val descripcion: String = "",
    val contacto: String = "",
    val direccion: String = "",
    val error: String? = null,
    val guardado: Boolean = false
)

class EditarNegocioViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EditarNegocioUiState())
    val uiState: StateFlow<EditarNegocioUiState> = _uiState.asStateFlow()

    fun cargar(negocioId: String) {
        val negocio = Negocios.negocioDe(negocioId) ?: return
        _uiState.value = EditarNegocioUiState(
            descripcion = negocio.descripcion,
            contacto = negocio.contacto,
            direccion = negocio.direccion
        )
    }

    fun onDescripcionCambiada(v: String) = _uiState.update { it.copy(descripcion = v, error = null) }
    fun onContactoCambiado(v: String) = _uiState.update { it.copy(contacto = v, error = null) }
    fun onDireccionCambiada(v: String) = _uiState.update { it.copy(direccion = v, error = null) }

    fun guardar(negocioId: String, operadorId: String) {
        val estado = _uiState.value
        try {
            Negocios.actualizarFicha(negocioId, operadorId, estado.descripcion.trim(), estado.contacto.trim(), estado.direccion.trim())
            _uiState.update { it.copy(guardado = true) }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message ?: "No se pudo guardar la ficha.") }
        }
    }
}
