package com.sportec.sporthub.ui.screens.deportista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportec.sporthub.data.mock.RepositorioCatalogoMock
import com.sportec.sporthub.data.model.ActividadConNegocio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetalleActividadUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val item: ActividadConNegocio? = null
)

class DetalleActividadViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleActividadUiState())
    val uiState: StateFlow<DetalleActividadUiState> = _uiState.asStateFlow()

    private var actividadId: String? = null

    fun cargar(id: String) {
        if (actividadId == id && _uiState.value.item != null) return
        actividadId = id
        _uiState.value = DetalleActividadUiState()
        viewModelScope.launch {
            RepositorioCatalogoMock.obtener(id)
                .onSuccess { item -> _uiState.value = DetalleActividadUiState(cargando = false, item = item) }
                .onFailure { error -> _uiState.value = DetalleActividadUiState(cargando = false, error = error.message) }
        }
    }
}
