package com.sportec.sporthub.ui.screens.deportista

import androidx.lifecycle.ViewModel
import com.sportec.sporthub.domain.Reservas
import com.sportec.sporthub.domain.EstadoReserva
import com.sportec.sporthub.domain.Reserva
import com.sportec.sporthub.domain.Solicitud
import com.sportec.sporthub.domain.Transferencia
import com.sportec.sporthub.domain.Transferencias
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class PestanaReservas { SOLICITUDES, PROXIMAS, HISTORIAL, TRANSFERENCIAS }

data class MisReservasUiState(
    val pestana: PestanaReservas = PestanaReservas.SOLICITUDES,
    val solicitudes: List<Solicitud> = emptyList(),
    val proximas: List<Reserva> = emptyList(),
    val historial: List<Reserva> = emptyList(),
    val transferencias: List<Transferencia> = emptyList()
)

class MisReservasViewModel : ViewModel() {

    private var usuarioId: String? = null

    private val _uiState = MutableStateFlow(MisReservasUiState())
    val uiState: StateFlow<MisReservasUiState> = _uiState.asStateFlow()

    fun cargar(usuarioId: String) {
        this.usuarioId = usuarioId
        refrescar()
    }

    fun seleccionarPestana(pestana: PestanaReservas) {
        _uiState.update { it.copy(pestana = pestana) }
    }

    private fun refrescar() {
        val id = usuarioId
        val solicitudes = Reservas.solicitudes.value.filter { it.usuarioId == id }
        val reservas = Reservas.reservas.value
        _uiState.update {
            it.copy(
                solicitudes = solicitudes,
                proximas = reservas.filter { reserva ->
                    reserva.usuarioId == id && reserva.estado == EstadoReserva.CONFIRMADA && !reserva.cerrada
                },
                historial = reservas.filter { reserva ->
                    reserva.usuarioId == id && (reserva.estado == EstadoReserva.CANCELADA || reserva.cerrada)
                },
                transferencias = id?.let { usuario -> Transferencias.listarDeUsuario(usuario) }.orEmpty()
            )
        }
    }
}
