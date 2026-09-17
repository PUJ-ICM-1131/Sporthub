package com.sportec.sporthub.ui.screens.deportista

import androidx.lifecycle.ViewModel
import com.sportec.sporthub.domain.ActividadConNegocio
import com.sportec.sporthub.domain.Catalogo
import com.sportec.sporthub.domain.Reserva
import com.sportec.sporthub.domain.Reservas
import com.sportec.sporthub.domain.Resenas
import com.sportec.sporthub.domain.Transferencias
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DetalleReservaUiState(
    val error: String? = null,
    val reserva: Reserva? = null,
    val item: ActividadConNegocio? = null,
    val transferenciaId: String? = null,
    val yaCalificada: Boolean = false
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
            DetalleReservaUiState(
                reserva = reserva,
                item = Catalogo.obtener(reserva.actividadId),
                transferenciaId = Transferencias.deReserva(id)?.id,
                yaCalificada = Resenas.resenas.value.any { it.reservaId == id }
            )
        } catch (e: Exception) {
            DetalleReservaUiState(error = e.message ?: "No se pudo cargar la reserva.")
        }
    }
}
