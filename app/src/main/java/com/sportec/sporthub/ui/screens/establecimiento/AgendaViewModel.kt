package com.sportec.sporthub.ui.screens.establecimiento

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos

import androidx.lifecycle.ViewModel
import com.sportec.sporthub.domain.Reserva
import com.sportec.sporthub.domain.Reservas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AgendaViewModel : ViewModel() {

    private var negocioId: String? = null

    private val _reservas = MutableStateFlow<List<Reserva>>(emptyList())
    val reservas: StateFlow<List<Reserva>> = _reservas.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun cargar(negocioId: String) {
        this.negocioId = negocioId
        refrescar()
    }

    fun confirmarCierre(reservaId: String, operadorId: String) = ejecutar {
        Reservas.cerrarActividad(reservaId, operadorId)
    }

    fun enviarAvisoSalida(reservaId: String, operadorId: String) = ejecutar {
        Reservas.enviarAvisoSalida(reservaId, operadorId)
    }

    private fun refrescar() {
        _reservas.value = negocioId
            ?.let { Reservas.listarReservasNegocio(it) }
            .orEmpty()
            .sortedBy { it.fecha + it.hora }
    }

    private fun ejecutar(accion: () -> Unit) {
        try {
            accion()
            _error.value = null
        } catch (e: Exception) {
            _error.value = e.message ?: Textos.obtener(R.string.texto_no_se_pudo_completar_la_accion)
        }
        refrescar()
    }
}
