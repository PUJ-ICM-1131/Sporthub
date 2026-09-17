package com.sportec.sporthub.ui.screens.establecimiento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportec.sporthub.domain.Reserva
import com.sportec.sporthub.domain.Reservas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class AgendaViewModel : ViewModel() {

    private val _negocioId = MutableStateFlow<String?>(null)

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val reservas: StateFlow<List<Reserva>> = combine(_negocioId, Reservas.reservas) { negocioId, _ ->
        negocioId?.let { Reservas.listarReservasNegocio(it) }.orEmpty().sortedBy { it.fecha + it.hora }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun cargar(negocioId: String) {
        _negocioId.value = negocioId
    }

    fun confirmarCierre(reservaId: String, operadorId: String) = ejecutar {
        Reservas.cerrarActividad(reservaId, operadorId)
    }

    fun enviarAvisoSalida(reservaId: String, operadorId: String) = ejecutar {
        Reservas.enviarAvisoSalida(reservaId, operadorId)
    }

    private fun ejecutar(accion: () -> Unit) {
        try {
            accion()
            _error.value = null
        } catch (e: Exception) {
            _error.value = e.message ?: "No se pudo completar la acción."
        }
    }
}
