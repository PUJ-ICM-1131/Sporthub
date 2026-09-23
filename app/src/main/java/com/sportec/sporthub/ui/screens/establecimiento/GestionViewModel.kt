package com.sportec.sporthub.ui.screens.establecimiento

import androidx.lifecycle.ViewModel
import com.sportec.sporthub.domain.EstadoPostulante
import com.sportec.sporthub.domain.EstadoSolicitud
import com.sportec.sporthub.domain.Reservas
import com.sportec.sporthub.domain.Solicitud
import com.sportec.sporthub.domain.Transferencia
import com.sportec.sporthub.domain.Transferencias
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private val PENDIENTES = setOf(EstadoSolicitud.FILA, EstadoSolicitud.REVISION)

data class GestionUiState(
    val solicitudes: List<Solicitud> = emptyList(),
    val transferencias: List<Transferencia> = emptyList()
)

class GestionViewModel : ViewModel() {

    private var negocioId: String? = null

    private val _uiState = MutableStateFlow(GestionUiState())
    val uiState: StateFlow<GestionUiState> = _uiState.asStateFlow()

    fun cargar(negocioId: String) {
        this.negocioId = negocioId
        refrescar()
    }

    private fun refrescar() {
        val id = negocioId
        if (id == null) {
            _uiState.update { GestionUiState() }
            return
        }
        val solicitudes = Reservas.listarSolicitudesNegocio(id)
            .filter { it.estado in PENDIENTES }
            .sortedBy { it.orden }
        val transferencias = Transferencias.listarDeNegocio(id)
            .filter { transferencia -> transferencia.postulantes.any { it.estado == EstadoPostulante.REVISION } }
        _uiState.update { it.copy(solicitudes = solicitudes, transferencias = transferencias) }
    }
}
