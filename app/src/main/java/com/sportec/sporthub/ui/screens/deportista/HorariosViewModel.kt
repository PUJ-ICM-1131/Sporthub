package com.sportec.sporthub.ui.screens.deportista

import androidx.lifecycle.ViewModel
import com.sportec.sporthub.domain.Actividad
import com.sportec.sporthub.domain.Catalogo
import com.sportec.sporthub.domain.FranjaHoraria
import com.sportec.sporthub.domain.Reservas
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private val PRIMERA_FECHA_DEMO: LocalDate = LocalDate.parse("2026-09-22")

data class HorariosUiState(
    val error: String? = null,
    val actividad: Actividad? = null,
    val fechas: List<String> = emptyList(),
    val fechaSeleccionada: String = "",
    val franjas: List<FranjaHoraria> = emptyList(),
    val horaSeleccionada: Int? = null
)

class HorariosViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HorariosUiState())
    val uiState: StateFlow<HorariosUiState> = _uiState.asStateFlow()

    private var actividadId: String? = null

    fun cargar(id: String) {
        if (actividadId == id) return
        actividadId = id
        _uiState.value = try {
            val item = Catalogo.obtener(id)
            val fechas = (0..4).map { PRIMERA_FECHA_DEMO.plusDays(it.toLong()).toString() }
            val primera = fechas.first()
            HorariosUiState(
                actividad = item.actividad,
                fechas = fechas,
                fechaSeleccionada = primera,
                franjas = Reservas.listarFranjas(item.actividad, primera)
            )
        } catch (e: Exception) {
            HorariosUiState(error = e.message ?: "No se pudo cargar el servicio.")
        }
    }

    fun seleccionarFecha(fecha: String) {
        val actividad = _uiState.value.actividad ?: return
        _uiState.update {
            it.copy(
                fechaSeleccionada = fecha,
                horaSeleccionada = null,
                franjas = Reservas.listarFranjas(actividad, fecha)
            )
        }
    }

    fun seleccionarHora(hora: Int) {
        _uiState.update { it.copy(horaSeleccionada = hora) }
    }
}
