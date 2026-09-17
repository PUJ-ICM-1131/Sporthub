package com.sportec.sporthub.ui.screens.deportista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportec.sporthub.data.mock.RepositorioReservasMock
import com.sportec.sporthub.data.model.Reserva
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CancelarReservaUiState(
    val reserva: Reserva? = null,
    val enviando: Boolean = false,
    val error: String? = null,
    val cancelada: Boolean = false
)

class CancelarReservaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CancelarReservaUiState())
    val uiState: StateFlow<CancelarReservaUiState> = _uiState.asStateFlow()

    fun cargar(id: String) {
        _uiState.value = CancelarReservaUiState(reserva = RepositorioReservasMock.obtenerReserva(id))
    }

    fun confirmar(motivo: String, usuarioId: String) {
        val id = _uiState.value.reserva?.id ?: return
        _uiState.update { it.copy(enviando = true, error = null) }
        viewModelScope.launch {
            RepositorioReservasMock.cancelarReserva(id, motivo, usuarioId)
                .onSuccess { _uiState.update { it.copy(enviando = false, cancelada = true) } }
                .onFailure { error -> _uiState.update { it.copy(enviando = false, error = error.message ?: "No se pudo cancelar la reserva.") } }
        }
    }
}
