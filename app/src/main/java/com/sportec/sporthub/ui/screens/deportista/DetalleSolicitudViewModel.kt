package com.sportec.sporthub.ui.screens.deportista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportec.sporthub.data.mock.RepositorioCatalogoMock
import com.sportec.sporthub.data.mock.RepositorioReservasMock
import com.sportec.sporthub.data.model.ActividadConNegocio
import com.sportec.sporthub.data.model.Solicitud
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetalleSolicitudUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val solicitud: Solicitud? = null,
    val item: ActividadConNegocio? = null,
    val procesando: Boolean = false
)

class DetalleSolicitudViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleSolicitudUiState())
    val uiState: StateFlow<DetalleSolicitudUiState> = _uiState.asStateFlow()

    private var solicitudId: String? = null

    fun cargar(id: String) {
        solicitudId = id
        _uiState.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            val solicitud = RepositorioReservasMock.obtenerSolicitud(id)
            if (solicitud == null) {
                _uiState.value = DetalleSolicitudUiState(cargando = false, error = "La solicitud ya no existe.")
                return@launch
            }
            RepositorioCatalogoMock.obtener(solicitud.actividadId)
                .onSuccess { item -> _uiState.value = DetalleSolicitudUiState(cargando = false, solicitud = solicitud, item = item) }
                .onFailure { error -> _uiState.value = DetalleSolicitudUiState(cargando = false, error = error.message ?: "No se pudo cargar la solicitud.") }
        }
    }

    fun aprobar(operadorId: String) = ejecutar {
        RepositorioReservasMock.aprobarSolicitud(solicitudId!!, operadorId).map { }
    }

    fun rechazar(motivo: String, operadorId: String) = ejecutar {
        RepositorioReservasMock.rechazarSolicitud(solicitudId!!, motivo, operadorId)
    }

    fun retirar(motivo: String, usuarioId: String) = ejecutar {
        RepositorioReservasMock.retirarSolicitud(solicitudId!!, motivo, usuarioId)
    }

    private fun ejecutar(accion: suspend () -> Result<Unit>) {
        val id = solicitudId ?: return
        _uiState.update { it.copy(procesando = true, error = null) }
        viewModelScope.launch {
            accion()
                .onSuccess { cargar(id) }
                .onFailure { error ->
                    _uiState.update { it.copy(procesando = false, error = error.message ?: "No se pudo completar la acción.") }
                }
        }
    }
}
