package com.sportec.sporthub.ui.screens.deportista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportec.sporthub.domain.Reservas
import com.sportec.sporthub.domain.EstadoReserva
import com.sportec.sporthub.domain.Reserva
import com.sportec.sporthub.domain.Solicitud
import com.sportec.sporthub.domain.Transferencia
import com.sportec.sporthub.domain.Transferencias
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class PestanaReservas { SOLICITUDES, PROXIMAS, HISTORIAL, TRANSFERENCIAS }

data class MisReservasUiState(
    val pestana: PestanaReservas = PestanaReservas.SOLICITUDES,
    val solicitudes: List<Solicitud> = emptyList(),
    val proximas: List<Reserva> = emptyList(),
    val historial: List<Reserva> = emptyList(),
    val transferencias: List<Transferencia> = emptyList()
)

class MisReservasViewModel : ViewModel() {

    private val _usuarioId = MutableStateFlow<String?>(null)
    private val _pestana = MutableStateFlow(PestanaReservas.SOLICITUDES)

    val uiState: StateFlow<MisReservasUiState> = combine(
        _usuarioId, _pestana, Reservas.solicitudes, Reservas.reservas, Transferencias.transferencias
    ) { usuarioId, pestana, solicitudes, reservas, _ ->
        MisReservasUiState(
            pestana = pestana,
            solicitudes = solicitudes.filter { it.usuarioId == usuarioId },
            proximas = reservas.filter { it.usuarioId == usuarioId && it.estado == EstadoReserva.CONFIRMADA && !it.cerrada },
            historial = reservas.filter { it.usuarioId == usuarioId && (it.estado == EstadoReserva.CANCELADA || it.cerrada) },
            transferencias = usuarioId?.let { Transferencias.listarDeUsuario(it) }.orEmpty()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MisReservasUiState())

    fun cargar(usuarioId: String) {
        _usuarioId.value = usuarioId
    }

    fun seleccionarPestana(pestana: PestanaReservas) {
        _pestana.value = pestana
    }
}
