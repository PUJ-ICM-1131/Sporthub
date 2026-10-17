package com.sportec.sporthub.ui.screens.comun

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos

import androidx.lifecycle.ViewModel
import com.sportec.sporthub.domain.Comunidades
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CrearComunidadUiState(
    val nombre: String = "",
    val descripcion: String = "",
    val fotoUri: String? = null,
    val error: String? = null,
    val creadaId: String? = null
)

class CrearComunidadViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CrearComunidadUiState())
    val uiState: StateFlow<CrearComunidadUiState> = _uiState.asStateFlow()

    fun onNombreCambiado(v: String) = _uiState.update { it.copy(nombre = v, error = null) }
    fun onDescripcionCambiada(v: String) = _uiState.update { it.copy(descripcion = v, error = null) }

    fun onFotoCambiada(v: String) = _uiState.update { it.copy(fotoUri = v) }

    fun crear(creadorId: String) {
        val estado = _uiState.value
        try {
            val comunidad = Comunidades.crear(estado.nombre, estado.descripcion, creadorId, estado.fotoUri)
            _uiState.update { it.copy(creadaId = comunidad.id) }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message ?: Textos.obtener(R.string.texto_no_se_pudo_crear_la_comunidad)) }
        }
    }
}
