package com.sportec.sporthub.ui.screens.deportista

import androidx.lifecycle.ViewModel
import com.sportec.sporthub.domain.ActividadConNegocio
import com.sportec.sporthub.domain.Catalogo
import com.sportec.sporthub.domain.Reserva
import com.sportec.sporthub.domain.Reservas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DetalleReservaUiState(
    val error: String? = null,
    val reserva: Reserva? = null,
    val item: ActividadConNegocio? = null
)

class DetalleReservaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleReservaUiState())
    val uiState: StateFlow<DetalleReservaUiState> = _uiState.asStateFlow()

    fun cargar(id: String) {
        val reserva = Reservas.obtenerReserva(id)
        if (reserva == null) {
            _uiState.value = DetalleReservaUiState(error = "La reserva ya no existe.")
            return
        }
        _uiState.value = try {
            DetalleReservaUiState(reserva = reserva, item = Catalogo.obtener(reserva.actividadId))
        } catch (e: Exception) {
            DetalleReservaUiState(error = e.message ?: "No se pudo cargar la reserva.")
        }
    }
}
