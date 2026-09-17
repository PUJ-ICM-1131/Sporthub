package com.sportec.sporthub.ui.screens.deportista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportec.sporthub.domain.ActividadConNegocio
import com.sportec.sporthub.domain.Catalogo
import com.sportec.sporthub.domain.Reserva
import com.sportec.sporthub.domain.Reservas
import com.sportec.sporthub.domain.Transferencia
import com.sportec.sporthub.domain.Transferencias
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DetalleTransferenciaUiState(
    val transferencia: Transferencia? = null,
    val reserva: Reserva? = null,
    val item: ActividadConNegocio? = null,
    val error: String? = null
)

class DetalleTransferenciaViewModel : ViewModel() {

    private val _transferenciaId = MutableStateFlow<String?>(null)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<DetalleTransferenciaUiState> = combine(
        _transferenciaId, Transferencias.transferencias, _error
    ) { id, _, error ->
        val transferencia = id?.let { Transferencias.obtener(it) }
        val reserva = transferencia?.let { Reservas.obtenerReserva(it.reservaId) }
        val item = reserva?.let { runCatching { Catalogo.obtener(it.actividadId) }.getOrNull() }
        DetalleTransferenciaUiState(transferencia, reserva, item, error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DetalleTransferenciaUiState())

    fun cargar(id: String) {
        _transferenciaId.value = id
    }

    fun aprobar(operadorId: String) = ejecutar { Transferencias.aprobar(_transferenciaId.value!!, operadorId) }

    fun rechazar(postulanteId: String, motivo: String, operadorId: String) = ejecutar {
        Transferencias.rechazar(_transferenciaId.value!!, postulanteId, motivo, operadorId)
    }

    fun unirse(usuarioId: String) = ejecutar { Transferencias.unirse(_transferenciaId.value!!, usuarioId) }

    fun retirar(usuarioId: String) = ejecutar { Transferencias.retirarPublicacion(_transferenciaId.value!!, usuarioId) }

    fun pagar(usuarioId: String, medio: String, onExito: (String) -> Unit) {
        try {
            val reserva = Transferencias.pagar(_transferenciaId.value!!, usuarioId, medio)
            _error.value = null
            onExito(reserva.id)
        } catch (e: Exception) {
            _error.value = e.message ?: "No se pudo procesar el pago."
        }
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
