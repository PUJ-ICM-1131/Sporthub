package com.sportec.sporthub.ui.screens.deportista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportec.sporthub.data.mock.RepositorioCatalogoMock
import com.sportec.sporthub.data.mock.RepositorioReservasMock
import com.sportec.sporthub.data.model.ActividadConNegocio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ResumenSolicitudUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val item: ActividadConNegocio? = null,
    val enviando: Boolean = false
)

class ResumenSolicitudViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ResumenSolicitudUiState())
    val uiState: StateFlow<ResumenSolicitudUiState> = _uiState.asStateFlow()

    private var actividadId: String? = null

    fun cargar(id: String) {
        if (actividadId == id) return
        actividadId = id
        viewModelScope.launch {
            RepositorioCatalogoMock.obtener(id)
                .onSuccess { item -> _uiState.update { it.copy(cargando = false, item = item) } }
                .onFailure { error -> _uiState.update { it.copy(cargando = false, error = error.message) } }
        }
    }

    fun enviar(usuarioId: String, fecha: String, hora: Int, onExito: (solicitudId: String) -> Unit) {
        val actividad = _uiState.value.item?.actividad ?: return
        _uiState.update { it.copy(enviando = true, error = null) }
        viewModelScope.launch {
            RepositorioReservasMock.enviarSolicitud(usuarioId, actividad.id, fecha, hora)
                .onSuccess {
                    _uiState.update { it.copy(enviando = false) }
                    onExito(it.id)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(enviando = false, error = error.message ?: "No se pudo enviar la solicitud.") }
                }
        }
    }
}
