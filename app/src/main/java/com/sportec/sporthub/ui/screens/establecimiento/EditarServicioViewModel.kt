package com.sportec.sporthub.ui.screens.establecimiento

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos

import androidx.lifecycle.ViewModel
import com.sportec.sporthub.domain.Actividad
import com.sportec.sporthub.domain.Categoria
import com.sportec.sporthub.domain.DatosMock
import com.sportec.sporthub.domain.Negocios
import com.sportec.sporthub.domain.Politica
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
    val fotoUri: String? = null,
    val horaApertura: Int = 6,
    val horaCierre: Int = 21,
    val payMinutes: String = "10",
    val cancelHours: String = "2",
    val refundPercent: String = "80",
    val refundHours: String = "48",
    val transferable: Boolean = true,
    val extra: String = "",
    val error: String? = null,
    val guardado: Actividad? = null
)

class EditarServicioViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EditarServicioUiState())
    val uiState: StateFlow<EditarServicioUiState> = _uiState.asStateFlow()

    val categorias: List<Categoria> = DatosMock.categorias
    val horas: List<Int> = (0..24).toList()

    private var actividadId: String? = null

    fun cargar(id: String) {
        if (id == "nuevo") {
            actividadId = null
            _uiState.value = EditarServicioUiState()
            return
        }
        actividadId = id
        val actividad = Negocios.actividadDe(id) ?: return
        val politica = actividad.politica
        _uiState.value = EditarServicioUiState(
            esNuevo = false,
            nombre = actividad.nombre,
            categoria = actividad.categoria,
            tipo = actividad.tipo,
            precio = actividad.precio.toString(),
            duracionMinutos = actividad.duracionMinutos.toString(),
            capacidad = actividad.capacidad.toString(),
            descripcion = actividad.descripcion,
            fotoUri = actividad.fotoUri,
            horaApertura = actividad.horaApertura,
            horaCierre = actividad.horaCierre,
            payMinutes = politica.payMinutes.toString(),
            cancelHours = politica.cancelHours.toString(),
            refundPercent = politica.refundPercent.toString(),
            refundHours = politica.refundHours.toString(),
            transferable = politica.transferable,
            extra = politica.extra
        )
    }

    fun onNombreCambiado(v: String) = _uiState.update { it.copy(nombre = v, error = null) }
    fun onCategoriaCambiada(v: String) = _uiState.update { it.copy(categoria = v) }
    fun onTipoCambiado(v: TipoActividad) = _uiState.update { it.copy(tipo = v, capacidad = if (v == TipoActividad.ESPACIO) "1" else it.capacidad) }
    fun onPrecioCambiado(v: String) = _uiState.update { it.copy(precio = v.filter(Char::isDigit), error = null) }
    fun onDuracionCambiada(v: String) = _uiState.update { it.copy(duracionMinutos = v.filter(Char::isDigit), error = null) }
    fun onCapacidadCambiada(v: String) = _uiState.update { it.copy(capacidad = v.filter(Char::isDigit), error = null) }
    fun onDescripcionCambiada(v: String) = _uiState.update { it.copy(descripcion = v) }
    fun onFotoCambiada(v: String) = _uiState.update { it.copy(fotoUri = v) }

    fun onHoraAperturaCambiada(v: Int) = _uiState.update { it.copy(horaApertura = v, error = null) }
    fun onHoraCierreCambiada(v: Int) = _uiState.update { it.copy(horaCierre = v, error = null) }

    fun onPayMinutesCambiado(v: String) = _uiState.update { it.copy(payMinutes = v.filter(Char::isDigit), error = null) }
    fun onCancelHoursCambiado(v: String) = _uiState.update { it.copy(cancelHours = v.filter(Char::isDigit), error = null) }
    fun onRefundPercentCambiado(v: String) = _uiState.update { it.copy(refundPercent = v.filter(Char::isDigit), error = null) }
    fun onRefundHoursCambiado(v: String) = _uiState.update { it.copy(refundHours = v.filter(Char::isDigit), error = null) }
    fun onTransferableCambiado(v: Boolean) = _uiState.update { it.copy(transferable = v) }
    fun onExtraCambiado(v: String) = _uiState.update { it.copy(extra = v) }

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
                descripcion = estado.descripcion.trim(),
                horaApertura = estado.horaApertura,
                horaCierre = estado.horaCierre,
                politica = Politica(
                    payMinutes = estado.payMinutes.toIntOrNull() ?: 0,
                    cancelHours = estado.cancelHours.toIntOrNull() ?: -1,
                    refundPercent = estado.refundPercent.toIntOrNull() ?: -1,
                    refundHours = estado.refundHours.toIntOrNull() ?: 0,
                    transferable = estado.transferable,
                    extra = estado.extra.trim()
                ),
                fotoUri = estado.fotoUri
            )
            actividadId = actividad.id
            _uiState.update { it.copy(esNuevo = false, guardado = actividad) }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message ?: Textos.obtener(R.string.texto_no_se_pudo_guardar_el_servicio)) }
        }
    }
}
