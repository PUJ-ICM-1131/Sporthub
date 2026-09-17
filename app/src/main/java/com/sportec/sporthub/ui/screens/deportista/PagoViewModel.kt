package com.sportec.sporthub.ui.screens.deportista

import androidx.lifecycle.ViewModel
import com.sportec.sporthub.domain.Reservas
import com.sportec.sporthub.domain.Solicitud
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PagoUiState(
    val error: String? = null,
    val solicitud: Solicitud? = null
)

class PagoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PagoUiState())
    val uiState: StateFlow<PagoUiState> = _uiState.asStateFlow()

    fun cargar(solicitudId: String) {
        val solicitud = Reservas.obtenerSolicitud(solicitudId)
        _uiState.value = if (solicitud == null) {
            PagoUiState(error = "La solicitud ya no existe.")
        } else {
            PagoUiState(solicitud = solicitud)
        }
    }

    fun pagar(usuarioId: String, medio: String, onExito: (reservaId: String) -> Unit) {
        val solicitud = _uiState.value.solicitud ?: return
        try {
            val reserva = Reservas.pagarSolicitud(solicitud.id, medio, usuarioId)
            onExito(reserva.id)
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message ?: "No se pudo procesar el pago.") }
        }
    }
}
