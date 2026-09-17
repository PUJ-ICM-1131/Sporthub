package com.sportec.sporthub.ui.screens.establecimiento

import androidx.lifecycle.ViewModel
import com.sportec.sporthub.domain.Actividad
import com.sportec.sporthub.domain.Categoria
import com.sportec.sporthub.domain.DatosMock
import com.sportec.sporthub.domain.Negocios
import com.sportec.sporthub.domain.TipoActividad
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class EditarServicioUiState(
    val esNuevo: Boolean = true,
    val nombre: String = "",
    val categoria: String = DatosMock.categorias.first().nombre,
    val tipo: TipoActividad = TipoActividad.ESPACIO,
    val precio: String = "",
    val duracionMinutos: String = "120",
    val capacidad: String = "1",
    val descripcion: String = "",
    val error: String? = null,
    val guardado: Actividad? = null
)

class EditarServicioViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EditarServicioUiState())
    val uiState: StateFlow<EditarServicioUiState> = _uiState.asStateFlow()

    val categorias: List<Categoria> = DatosMock.categorias

    private var actividadId: String? = null

    fun cargar(id: String) {
        if (id == "nuevo") {
            actividadId = null
            _uiState.value = EditarServicioUiState()
            return
        }
        actividadId = id
        val actividad = Negocios.actividadDe(id) ?: return
        _uiState.value = EditarServicioUiState(
            esNuevo = false,
            nombre = actividad.nombre,
            categoria = actividad.categoria,
            tipo = actividad.tipo,
            precio = actividad.precio.toString(),
            duracionMinutos = actividad.duracionMinutos.toString(),
            capacidad = actividad.capacidad.toString(),
            descripcion = actividad.descripcion
        )
    }

    fun onNombreCambiado(v: String) = _uiState.update { it.copy(nombre = v, error = null) }
    fun onCategoriaCambiada(v: String) = _uiState.update { it.copy(categoria = v) }
    fun onTipoCambiado(v: TipoActividad) = _uiState.update { it.copy(tipo = v, capacidad = if (v == TipoActividad.ESPACIO) "1" else it.capacidad) }
    fun onPrecioCambiado(v: String) = _uiState.update { it.copy(precio = v.filter(Char::isDigit), error = null) }
    fun onDuracionCambiada(v: String) = _uiState.update { it.copy(duracionMinutos = v.filter(Char::isDigit), error = null) }
    fun onCapacidadCambiada(v: String) = _uiState.update { it.copy(capacidad = v.filter(Char::isDigit), error = null) }
    fun onDescripcionCambiada(v: String) = _uiState.update { it.copy(descripcion = v) }

    fun guardar(negocioId: String, operadorId: String) {
        val estado = _uiState.value
        try {
            val actividad = Negocios.guardarServicio(
                operadorId = operadorId,
                negocioId = negocioId,
                id = actividadId,
                nombre = estado.nombre.trim(),
                categoria = estado.categoria,
                tipo = estado.tipo,
                precio = estado.precio.toIntOrNull() ?: 0,
                duracionMinutos = estado.duracionMinutos.toIntOrNull() ?: 0,
                capacidad = estado.capacidad.toIntOrNull() ?: 0,
                descripcion = estado.descripcion.trim()
            )
            actividadId = actividad.id
            _uiState.update { it.copy(esNuevo = false, guardado = actividad) }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message ?: "No se pudo guardar el servicio.") }
        }
    }
}
