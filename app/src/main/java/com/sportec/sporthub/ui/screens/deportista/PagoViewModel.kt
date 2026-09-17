package com.sportec.sporthub.ui.screens.deportista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportec.sporthub.data.mock.RepositorioReservasMock
import com.sportec.sporthub.data.model.Solicitud
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PagoUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val solicitud: Solicitud? = null,
    val pagando: Boolean = false
)

class PagoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PagoUiState())
    val uiState: StateFlow<PagoUiState> = _uiState.asStateFlow()

    fun cargar(solicitudId: String) {
        val solicitud = RepositorioReservasMock.obtenerSolicitud(solicitudId)
        _uiState.value = if (solicitud == null) {
            PagoUiState(cargando = false, error = "La solicitud ya no existe.")
        } else {
            PagoUiState(cargando = false, solicitud = solicitud)
        }
    }

    fun pagar(usuarioId: String, medio: String, onExito: (reservaId: String) -> Unit) {
        val solicitud = _uiState.value.solicitud ?: return
        _uiState.update { it.copy(pagando = true, error = null) }
        viewModelScope.launch {
            RepositorioReservasMock.pagarSolicitud(solicitud.id, medio, usuarioId)
                .onSuccess { reserva ->
                    _uiState.update { it.copy(pagando = false) }
                    onExito(reserva.id)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(pagando = false, error = error.message ?: "No se pudo procesar el pago.") }
                }
        }
    }
}
