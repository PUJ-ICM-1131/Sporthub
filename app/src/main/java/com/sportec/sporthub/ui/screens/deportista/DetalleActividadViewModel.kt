package com.sportec.sporthub.ui.screens.deportista

import androidx.lifecycle.ViewModel
import com.sportec.sporthub.domain.ActividadConNegocio
import com.sportec.sporthub.domain.Catalogo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DetalleActividadUiState(
    val error: String? = null,
    val item: ActividadConNegocio? = null
)

class DetalleActividadViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleActividadUiState())
    val uiState: StateFlow<DetalleActividadUiState> = _uiState.asStateFlow()

    private var actividadId: String? = null

    fun cargar(id: String) {
        if (actividadId == id) return
        actividadId = id
        _uiState.value = try {
            DetalleActividadUiState(item = Catalogo.obtener(id))
        } catch (e: Exception) {
            DetalleActividadUiState(error = e.message)
        }
    }
}
