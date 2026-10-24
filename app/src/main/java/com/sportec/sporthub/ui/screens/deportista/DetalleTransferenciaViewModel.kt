package com.sportec.sporthub.ui.screens.deportista

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos

import androidx.lifecycle.ViewModel
import com.sportec.sporthub.domain.ActividadConNegocio
import com.sportec.sporthub.domain.Catalogo
import com.sportec.sporthub.domain.Reserva
import com.sportec.sporthub.domain.Reservas
import com.sportec.sporthub.domain.Transferencia
import com.sportec.sporthub.domain.Transferencias
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DetalleTransferenciaUiState(
    val transferencia: Transferencia? = null,
    val reserva: Reserva? = null,
    val item: ActividadConNegocio? = null,
    val error: String? = null
)

class DetalleTransferenciaViewModel : ViewModel() {

    private var transferenciaId: String? = null

    private val _uiState = MutableStateFlow(DetalleTransferenciaUiState())
    val uiState: StateFlow<DetalleTransferenciaUiState> = _uiState.asStateFlow()

    fun cargar(id: String) {
        transferenciaId = id
        refrescar()
    }

    fun aceptarInteresado(propietarioId: String) = ejecutar { Transferencias.aceptarInteresado(transferenciaId!!, propietarioId) }

    fun aprobar(operadorId: String) = ejecutar { Transferencias.aprobar(transferenciaId!!, operadorId) }

    fun rechazar(postulanteId: String, motivo: String, operadorId: String) = ejecutar {
        Transferencias.rechazar(transferenciaId!!, postulanteId, motivo, operadorId)
    }

    fun unirse(usuarioId: String) = ejecutar { Transferencias.unirse(transferenciaId!!, usuarioId) }

    fun retirar(usuarioId: String) = ejecutar { Transferencias.retirarPublicacion(transferenciaId!!, usuarioId) }

    fun pagar(usuarioId: String, medio: String, onExito: (String) -> Unit) {
        try {
            val reserva = Transferencias.pagar(transferenciaId!!, usuarioId, medio)
            _uiState.update { it.copy(error = null) }
            onExito(reserva.id)
        } catch (e: Exception) {
            val mensaje = e.message ?: Textos.obtener(R.string.texto_no_se_pudo_procesar_el_pago)
            _uiState.update { it.copy(error = mensaje) }
        }
    }

    private fun refrescar(error: String? = null) {
        val transferencia = transferenciaId?.let { Transferencias.obtener(it) }
        val reserva = transferencia?.let { Reservas.obtenerReserva(it.reservaId) }
        val item = reserva?.let { runCatching { Catalogo.obtener(it.actividadId) }.getOrNull() }
        _uiState.update {
            it.copy(transferencia = transferencia, reserva = reserva, item = item, error = error)
        }
    }

    private fun ejecutar(accion: () -> Unit) {
        try {
            accion()
            refrescar()
        } catch (e: Exception) {
            refrescar(e.message ?: Textos.obtener(R.string.texto_no_se_pudo_completar_la_accion))
        }
    }
}
