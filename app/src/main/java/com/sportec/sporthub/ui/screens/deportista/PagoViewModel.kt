package com.sportec.sporthub.ui.screens.deportista

import androidx.lifecycle.ViewModel
import com.sportec.sporthub.domain.Reservas
import com.sportec.sporthub.domain.Transferencias
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PagoUiState(
    val error: String? = null,
    val monto: Int? = null,
    val esTransferencia: Boolean = false
)

/**
 * Ruta Pago(referencia) se comparte entre pagar una solicitud aceptada y
 * pagar una transferencia aprobada: primero se busca como solicitud y, si
 * no existe, como transferencia.
 */
class PagoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PagoUiState())
    val uiState: StateFlow<PagoUiState> = _uiState.asStateFlow()

    private var referencia: String = ""

    fun cargar(id: String) {
        referencia = id
        val solicitud = Reservas.obtenerSolicitud(id)
        if (solicitud != null) {
            _uiState.value = PagoUiState(monto = solicitud.precio, esTransferencia = false)
            return
        }
        val transferencia = Transferencias.obtener(id)
        val reserva = transferencia?.let { Reservas.obtenerReserva(it.reservaId) }
        _uiState.value = if (reserva != null) {
            PagoUiState(monto = reserva.precio, esTransferencia = true)
        } else {
            PagoUiState(error = "La solicitud ya no existe.")
        }
    }

    fun pagar(usuarioId: String, medio: String, onExito: (reservaId: String) -> Unit) {
        try {
            val reserva = if (_uiState.value.esTransferencia) {
                Transferencias.pagar(referencia, usuarioId, medio)
            } else {
                Reservas.pagarSolicitud(referencia, medio, usuarioId)
            }
            onExito(reserva.id)
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message ?: "No se pudo procesar el pago.") }
        }
    }
}
