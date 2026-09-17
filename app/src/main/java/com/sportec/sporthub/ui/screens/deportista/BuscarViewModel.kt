package com.sportec.sporthub.ui.screens.deportista

import androidx.lifecycle.ViewModel
import com.sportec.sporthub.domain.ActividadConNegocio
import com.sportec.sporthub.domain.Catalogo
import com.sportec.sporthub.domain.Categoria
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class BuscarUiState(
    val texto: String = "",
    val categoriaSeleccionada: String? = null,
    val categorias: List<Categoria> = emptyList(),
    val resultados: List<ActividadConNegocio> = emptyList()
)

class BuscarViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BuscarUiState())
    val uiState: StateFlow<BuscarUiState> = _uiState.asStateFlow()

    init {
        buscar()
    }

    fun onTextoCambiado(v: String) {
        _uiState.update { it.copy(texto = v) }
        buscar()
    }

    fun seleccionarCategoria(categoria: String?) {
        _uiState.update { it.copy(categoriaSeleccionada = categoria) }
        buscar()
    }

    private fun buscar() {
        val estado = _uiState.value
        val resultado = Catalogo.explorar(estado.categoriaSeleccionada)
        val texto = estado.texto.trim()
        val filtrados = resultado.actividades.filter {
            texto.isBlank() ||
                it.actividad.nombre.contains(texto, ignoreCase = true) ||
                it.negocio.nombre.contains(texto, ignoreCase = true) ||
                it.actividad.categoria.contains(texto, ignoreCase = true)
        }
        _uiState.update { it.copy(categorias = resultado.categorias, resultados = filtrados) }
    }
}
