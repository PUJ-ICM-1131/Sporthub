package com.sportec.sporthub.ui.screens.deportista

import androidx.lifecycle.ViewModel
import com.sportec.sporthub.domain.ActividadConNegocio
import com.sportec.sporthub.domain.Catalogo
import com.sportec.sporthub.domain.Reservas
import com.sportec.sporthub.domain.Solicitud
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DetalleSolicitudUiState(
    val error: String? = null,
    val solicitud: Solicitud? = null,
    val item: ActividadConNegocio? = null
)

class DetalleSolicitudViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleSolicitudUiState())
    val uiState: StateFlow<DetalleSolicitudUiState> = _uiState.asStateFlow()

    private var solicitudId: String? = null

    fun cargar(id: String) {
        solicitudId = id
        val solicitud = Reservas.obtenerSolicitud(id)
        if (solicitud == null) {
            _uiState.value = DetalleSolicitudUiState(error = "La solicitud ya no existe.")
            return
        }
        _uiState.value = try {
            DetalleSolicitudUiState(solicitud = solicitud, item = Catalogo.obtener(solicitud.actividadId))
        } catch (e: Exception) {
            DetalleSolicitudUiState(error = e.message ?: "No se pudo cargar la solicitud.")
        }
    }

    fun aprobar(operadorId: String) = ejecutar { Reservas.aprobarSolicitud(solicitudId!!, operadorId) }

    fun rechazar(motivo: String, operadorId: String) = ejecutar { Reservas.rechazarSolicitud(solicitudId!!, motivo, operadorId) }

    fun retirar(motivo: String, usuarioId: String) = ejecutar { Reservas.retirarSolicitud(solicitudId!!, motivo, usuarioId) }

    private fun ejecutar(accion: () -> Unit) {
        val id = solicitudId ?: return
        try {
            accion()
            cargar(id)
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message ?: "No se pudo completar la acción.") }
        }
    }
}
