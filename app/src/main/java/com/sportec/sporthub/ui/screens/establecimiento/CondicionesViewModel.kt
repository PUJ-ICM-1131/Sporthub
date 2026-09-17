package com.sportec.sporthub.ui.screens.establecimiento

import androidx.lifecycle.ViewModel
import com.sportec.sporthub.domain.Negocios
import com.sportec.sporthub.domain.Politica
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CondicionesUiState(
    val payMinutes: String = "10",
    val cancelHours: String = "2",
    val refundPercent: String = "80",
    val refundHours: String = "48",
    val transferable: Boolean = true,
    val extra: String = "",
    val error: String? = null,
    val guardado: Boolean = false
)

class CondicionesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CondicionesUiState())
    val uiState: StateFlow<CondicionesUiState> = _uiState.asStateFlow()

    private var actividadId: String? = null

    fun cargar(id: String) {
        actividadId = id
        val politica = Negocios.actividadDe(id)?.politica ?: return
        _uiState.value = CondicionesUiState(
            payMinutes = politica.payMinutes.toString(),
            cancelHours = politica.cancelHours.toString(),
            refundPercent = politica.refundPercent.toString(),
            refundHours = politica.refundHours.toString(),
            transferable = politica.transferable,
            extra = politica.extra
        )
    }

    fun onPayMinutesCambiado(v: String) = _uiState.update { it.copy(payMinutes = v.filter(Char::isDigit), error = null) }
    fun onCancelHoursCambiado(v: String) = _uiState.update { it.copy(cancelHours = v.filter(Char::isDigit), error = null) }
    fun onRefundPercentCambiado(v: String) = _uiState.update { it.copy(refundPercent = v.filter(Char::isDigit), error = null) }
    fun onRefundHoursCambiado(v: String) = _uiState.update { it.copy(refundHours = v.filter(Char::isDigit), error = null) }
    fun onTransferableCambiado(v: Boolean) = _uiState.update { it.copy(transferable = v) }
    fun onExtraCambiado(v: String) = _uiState.update { it.copy(extra = v) }

    fun guardar(operadorId: String) {
        val id = actividadId ?: return
        val estado = _uiState.value
        try {
            Negocios.actualizarPolitica(
                operadorId, id,
                Politica(
                    payMinutes = estado.payMinutes.toIntOrNull() ?: 0,
                    cancelHours = estado.cancelHours.toIntOrNull() ?: 0,
                    refundPercent = estado.refundPercent.toIntOrNull() ?: 0,
                    refundHours = estado.refundHours.toIntOrNull() ?: 0,
                    transferable = estado.transferable,
                    extra = estado.extra.trim()
                )
            )
            _uiState.update { it.copy(guardado = true) }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message ?: "No se pudieron guardar las condiciones.") }
        }
    }
}
