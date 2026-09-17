package com.sportec.sporthub.ui.screens.deportista

import androidx.lifecycle.ViewModel
import com.sportec.sporthub.domain.Resenas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ResenaUiState(
    val puntaje: Int = 5,
    val comentario: String = "",
    val error: String? = null,
    val enviada: Boolean = false
)

class ResenaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ResenaUiState())
    val uiState: StateFlow<ResenaUiState> = _uiState.asStateFlow()

    fun onPuntajeCambiado(v: Int) = _uiState.update { it.copy(puntaje = v, error = null) }
    fun onComentarioCambiado(v: String) = _uiState.update { it.copy(comentario = v, error = null) }

    fun enviar(reservaId: String, usuarioId: String) {
        val estado = _uiState.value
        try {
            Resenas.crear(reservaId, usuarioId, estado.puntaje, estado.comentario.trim())
            _uiState.update { it.copy(enviada = true) }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message ?: "No se pudo enviar la reseña.") }
        }
    }
}
