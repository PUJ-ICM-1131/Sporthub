package com.sportec.sporthub.ui.screens.deportista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportec.sporthub.data.mock.FranjaHoraria
import com.sportec.sporthub.data.mock.RepositorioCatalogoMock
import com.sportec.sporthub.data.mock.RepositorioReservasMock
import com.sportec.sporthub.data.model.Actividad
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val PRIMERA_FECHA_DEMO: LocalDate = LocalDate.parse("2026-09-22")

data class HorariosUiState(
    val cargando: Boolean = true,
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
        viewModelScope.launch {
            RepositorioCatalogoMock.obtener(id)
                .onSuccess { item ->
                    val fechas = (0..4).map { PRIMERA_FECHA_DEMO.plusDays(it.toLong()).toString() }
                    val primera = fechas.first()
                    _uiState.value = HorariosUiState(
                        cargando = false,
                        actividad = item.actividad,
                        fechas = fechas,
                        fechaSeleccionada = primera,
                        franjas = RepositorioReservasMock.listarFranjas(item.actividad, primera)
                    )
                }
                .onFailure { error ->
                    _uiState.update { it.copy(cargando = false, error = error.message ?: "No se pudo cargar el servicio.") }
                }
        }
    }

    fun seleccionarFecha(fecha: String) {
        val actividad = _uiState.value.actividad ?: return
        _uiState.update {
            it.copy(
                fechaSeleccionada = fecha,
                horaSeleccionada = null,
                franjas = RepositorioReservasMock.listarFranjas(actividad, fecha)
            )
        }
    }

    fun seleccionarHora(hora: Int) {
        _uiState.update { it.copy(horaSeleccionada = hora) }
    }
}
