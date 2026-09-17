package com.sportec.sporthub.ui.screens.establecimiento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportec.sporthub.domain.EstadoPostulante
import com.sportec.sporthub.domain.EstadoSolicitud
import com.sportec.sporthub.domain.Reservas
import com.sportec.sporthub.domain.Solicitud
import com.sportec.sporthub.domain.Transferencia
import com.sportec.sporthub.domain.Transferencias
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

private val PENDIENTES = setOf(EstadoSolicitud.FILA, EstadoSolicitud.REVISION)

data class GestionUiState(
    val solicitudes: List<Solicitud> = emptyList(),
    val transferencias: List<Transferencia> = emptyList()
)

class GestionViewModel : ViewModel() {

    private val _negocioId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<GestionUiState> = combine(
        _negocioId, Reservas.solicitudes, Transferencias.transferencias
    ) { negocioId, _, _ ->
        if (negocioId == null) {
            GestionUiState()
        } else {
            GestionUiState(
                solicitudes = Reservas.listarSolicitudesNegocio(negocioId).filter { it.estado in PENDIENTES }.sortedBy { it.orden },
                transferencias = Transferencias.listarDeNegocio(negocioId).filter { t -> t.postulantes.any { it.estado == EstadoPostulante.REVISION } }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GestionUiState())

    fun cargar(negocioId: String) {
        _negocioId.value = negocioId
    }
}
