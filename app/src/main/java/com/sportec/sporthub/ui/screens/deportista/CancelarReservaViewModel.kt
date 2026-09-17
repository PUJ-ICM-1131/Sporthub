package com.sportec.sporthub.ui.screens.deportista

import androidx.lifecycle.ViewModel
import com.sportec.sporthub.domain.Reserva
import com.sportec.sporthub.domain.Reservas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CancelarReservaUiState(
    val reserva: Reserva? = null,
    val error: String? = null,
    val cancelada: Boolean = false
)

class CancelarReservaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CancelarReservaUiState())
    val uiState: StateFlow<CancelarReservaUiState> = _uiState.asStateFlow()

    fun cargar(id: String) {
        _uiState.value = CancelarReservaUiState(reserva = Reservas.obtenerReserva(id))
    }

    fun confirmar(motivo: String, usuarioId: String) {
        val id = _uiState.value.reserva?.id ?: return
        try {
            Reservas.cancelarReserva(id, motivo, usuarioId)
            _uiState.update { it.copy(cancelada = true) }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message ?: "No se pudo cancelar la reserva.") }
        }
    }
}
