package com.sportec.sporthub.ui.screens.establecimiento

import androidx.lifecycle.ViewModel
import com.sportec.sporthub.domain.Actividad
import com.sportec.sporthub.domain.FranjaHoraria
import com.sportec.sporthub.domain.Negocios
import com.sportec.sporthub.domain.Reservas
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private val PRIMERA_FECHA_DEMO: LocalDate = LocalDate.parse("2026-09-22")

data class HorariosServicioUiState(
    val actividad: Actividad? = null,
    val fechas: List<String> = emptyList(),
    val fechaSeleccionada: String = "",
    val franjas: List<FranjaHoraria> = emptyList(),
    val bloqueados: List<Int> = emptyList(),
    val error: String? = null
)

class HorariosServicioViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HorariosServicioUiState())
    val uiState: StateFlow<HorariosServicioUiState> = _uiState.asStateFlow()

    fun cargar(actividadId: String) {
        val actividad = Negocios.actividadDe(actividadId) ?: return
        val fechas = (0..4).map { PRIMERA_FECHA_DEMO.plusDays(it.toLong()).toString() }
        val primera = fechas.first()
        _uiState.value = HorariosServicioUiState(
            actividad = actividad,
            fechas = fechas,
            fechaSeleccionada = primera,
            franjas = Reservas.listarFranjas(actividad, primera),
            bloqueados = Negocios.listarBloqueos(actividadId, primera)
        )
    }

    fun seleccionarFecha(fecha: String) {
        val actividad = _uiState.value.actividad ?: return
        _uiState.update {
            it.copy(
                fechaSeleccionada = fecha,
                franjas = Reservas.listarFranjas(actividad, fecha),
                bloqueados = Negocios.listarBloqueos(actividad.id, fecha)
            )
        }
    }

    fun alternarBloqueo(operadorId: String, hora: Int) {
        val estado = _uiState.value
        val actividad = estado.actividad ?: return
        try {
            if (hora in estado.bloqueados) {
                Negocios.desbloquearHorario(operadorId, actividad.id, estado.fechaSeleccionada, hora)
            } else {
                Negocios.bloquearHorario(operadorId, actividad.id, estado.fechaSeleccionada, hora)
            }
            seleccionarFecha(estado.fechaSeleccionada)
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message ?: "No se pudo actualizar el horario.") }
        }
    }
}
