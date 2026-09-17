package com.sportec.sporthub.ui.screens.deportista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportec.sporthub.data.mock.RepositorioCatalogoMock
import com.sportec.sporthub.data.mock.RepositorioReservasMock
import com.sportec.sporthub.data.model.ActividadConNegocio
import com.sportec.sporthub.data.model.Reserva
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetalleReservaUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val reserva: Reserva? = null,
    val item: ActividadConNegocio? = null
)

class DetalleReservaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleReservaUiState())
    val uiState: StateFlow<DetalleReservaUiState> = _uiState.asStateFlow()

    fun cargar(id: String) {
        _uiState.value = DetalleReservaUiState()
        val reserva = RepositorioReservasMock.obtenerReserva(id)
        if (reserva == null) {
            _uiState.value = DetalleReservaUiState(cargando = false, error = "La reserva ya no existe.")
            return
        }
        viewModelScope.launch {
            RepositorioCatalogoMock.obtener(reserva.actividadId)
                .onSuccess { item -> _uiState.value = DetalleReservaUiState(cargando = false, reserva = reserva, item = item) }
                .onFailure { error -> _uiState.value = DetalleReservaUiState(cargando = false, error = error.message ?: "No se pudo cargar la reserva.") }
        }
    }
}
