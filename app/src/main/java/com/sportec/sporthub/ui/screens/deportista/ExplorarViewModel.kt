package com.sportec.sporthub.ui.screens.deportista

import androidx.lifecycle.ViewModel
import com.sportec.sporthub.domain.ActividadConNegocio
import com.sportec.sporthub.domain.Catalogo
import com.sportec.sporthub.domain.Categoria
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ExplorarUiState(
    val categorias: List<Categoria> = emptyList(),
    val categoriaSeleccionada: String? = null,
    val actividades: List<ActividadConNegocio> = emptyList()
)

class ExplorarViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ExplorarUiState())
    val uiState: StateFlow<ExplorarUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    fun seleccionarCategoria(nombre: String?) {
        _uiState.update { it.copy(categoriaSeleccionada = nombre) }
        cargar()
    }

    private fun cargar() {
        val resultado = Catalogo.explorar(_uiState.value.categoriaSeleccionada)
        _uiState.update { it.copy(categorias = resultado.categorias, actividades = resultado.actividades) }
    }
}
