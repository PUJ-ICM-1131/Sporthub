package com.sportec.sporthub.ui.screens.deportista

import androidx.lifecycle.ViewModel
import com.sportec.sporthub.domain.ActividadConNegocio
import com.sportec.sporthub.domain.Catalogo
import com.sportec.sporthub.domain.Reservas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ResumenSolicitudUiState(
    val error: String? = null,
    val item: ActividadConNegocio? = null
)

class ResumenSolicitudViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ResumenSolicitudUiState())
    val uiState: StateFlow<ResumenSolicitudUiState> = _uiState.asStateFlow()

    private var actividadId: String? = null

    fun cargar(id: String) {
        if (actividadId == id) return
        actividadId = id
        _uiState.value = try {
            ResumenSolicitudUiState(item = Catalogo.obtener(id))
        } catch (e: Exception) {
            ResumenSolicitudUiState(error = e.message)
        }
    }

    fun enviar(usuarioId: String, fecha: String, hora: Int, onExito: (solicitudId: String) -> Unit) {
        val actividad = _uiState.value.item?.actividad ?: return
        try {
            val solicitud = Reservas.enviarSolicitud(usuarioId, actividad.id, fecha, hora)
            onExito(solicitud.id)
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message ?: "No se pudo enviar la solicitud.") }
        }
    }
}
