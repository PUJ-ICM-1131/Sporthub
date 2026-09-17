package com.sportec.sporthub.ui.screens.deportista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportec.sporthub.data.mock.RepositorioCatalogoMock
import com.sportec.sporthub.data.model.ActividadConNegocio
import com.sportec.sporthub.data.model.Categoria
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExplorarUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val categorias: List<Categoria> = emptyList(),
    val categoriaSeleccionada: String? = null,
    val actividades: List<ActividadConNegocio> = emptyList()
)

class ExplorarViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ExplorarUiState())
    val uiState: StateFlow<ExplorarUiState> = _uiState.asStateFlow()

    private var carga: Job? = null

    init {
        cargar()
    }

    fun seleccionarCategoria(nombre: String?) {
        if (_uiState.value.categoriaSeleccionada == nombre) return
        _uiState.update { it.copy(categoriaSeleccionada = nombre) }
        cargar()
    }

    fun cargar() {
        carga?.cancel()
        _uiState.update { it.copy(cargando = true, error = null) }
        carga = viewModelScope.launch {
            RepositorioCatalogoMock.explorar(_uiState.value.categoriaSeleccionada)
                .onSuccess { resultado ->
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            categorias = resultado.categorias,
                            actividades = resultado.actividades
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(cargando = false, error = error.message ?: "No se pudo cargar el catálogo.")
                    }
                }
        }
    }
}
