package com.sportec.sporthub.ui.screens.acceso

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportec.sporthub.R
import com.sportec.sporthub.domain.Autenticacion
import com.sportec.sporthub.utils.Textos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

data class RecuperarUiState(
    val cargando: Boolean = false,
    val error: String? = null,
    val enviadoA: String? = null
)

class RecuperarViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RecuperarUiState())
    val uiState: StateFlow<RecuperarUiState> = _uiState.asStateFlow()

    fun enviar(email: String) {
        if (_uiState.value.cargando) return
        _uiState.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            try {
                Autenticacion.enviarRecuperacion(email)
                _uiState.update { it.copy(cargando = false, enviadoA = email.trim()) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = e.message ?: Textos.obtener(R.string.texto_el_servicio_no_esta_disponible_intenta_de_nuevo))
                }
            }
        }
    }

    fun limpiarError() = _uiState.update { it.copy(error = null) }

    fun envioAtendido() = _uiState.update { it.copy(enviadoA = null) }
}
