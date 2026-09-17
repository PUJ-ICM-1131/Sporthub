package com.sportec.sporthub.ui.screens.deportista

import androidx.lifecycle.ViewModel
import com.sportec.sporthub.domain.Reserva
import com.sportec.sporthub.domain.Reservas
import com.sportec.sporthub.domain.Transferencias
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PublicarTransferenciaUiState(
    val reserva: Reserva? = null,
    val error: String? = null,
    val publicada: String? = null
)

class PublicarTransferenciaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PublicarTransferenciaUiState())
    val uiState: StateFlow<PublicarTransferenciaUiState> = _uiState.asStateFlow()

    fun cargar(reservaId: String) {
        _uiState.value = PublicarTransferenciaUiState(reserva = Reservas.obtenerReserva(reservaId))
    }

    fun publicar(usuarioId: String) {
        val reservaId = _uiState.value.reserva?.id ?: return
        try {
            val transferencia = Transferencias.publicar(reservaId, usuarioId)
            _uiState.update { it.copy(publicada = transferencia.id) }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message ?: "No se pudo publicar la transferencia.") }
        }
    }
}
