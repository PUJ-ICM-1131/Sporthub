package com.sportec.sporthub.ui.screens.comun

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos

import com.sportec.sporthub.utils.Validaciones
import androidx.lifecycle.ViewModel
import com.sportec.sporthub.domain.Cuenta
import com.sportec.sporthub.domain.Negocios
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class EditarPerfilUiState(
    val nombre: String = "",
    val telefono: String = "",
    val fotoUri: String? = null,
    val tieneNegocio: Boolean = false,
    val nombreNegocio: String = "",
    val nit: String = "",
    val descripcion: String = "",
    val contacto: String = "",
    val direccion: String = "",
    val error: String? = null,
    val guardada: Cuenta? = null
)

class EditarPerfilViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EditarPerfilUiState())
    val uiState: StateFlow<EditarPerfilUiState> = _uiState.asStateFlow()

    fun cargar(cuenta: Cuenta) {
        if (_uiState.value.guardada != null) return
        val negocio = cuenta.negocioId?.let { Negocios.negocioDe(it) }
        _uiState.value = EditarPerfilUiState(
            nombre = cuenta.nombre,
            telefono = cuenta.telefono.orEmpty(),
            fotoUri = cuenta.fotoUri,
            tieneNegocio = negocio != null,
            nombreNegocio = negocio?.nombre.orEmpty(),
            nit = negocio?.nit.orEmpty(),
            descripcion = negocio?.descripcion.orEmpty(),
            contacto = negocio?.contacto.orEmpty(),
            direccion = negocio?.direccion.orEmpty()
        )
    }

    fun onNombreCambiado(v: String) = _uiState.update { it.copy(nombre = v, error = null) }
    fun onTelefonoCambiado(v: String) = _uiState.update { it.copy(telefono = v, error = null) }
    fun onFotoCambiada(v: String) = _uiState.update { it.copy(fotoUri = v) }

    fun onNombreNegocioCambiado(v: String) = _uiState.update { it.copy(nombreNegocio = v, error = null) }
    fun onNitCambiado(v: String) = _uiState.update { it.copy(nit = v, error = null) }
    fun onDescripcionCambiada(v: String) = _uiState.update { it.copy(descripcion = v, error = null) }
    fun onContactoCambiado(v: String) = _uiState.update { it.copy(contacto = v, error = null) }
    fun onDireccionCambiada(v: String) = _uiState.update { it.copy(direccion = v, error = null) }

    fun guardar(cuenta: Cuenta) {
        val estado = _uiState.value
        if (estado.nombre.isBlank()) {
            _uiState.update { it.copy(error = Textos.obtener(R.string.texto_el_nombre_no_puede_estar_vacio)) }
            return
        }
        if (!Validaciones.telefono(estado.telefono)) {
            _uiState.update { it.copy(error = Textos.obtener(R.string.error_telefono_formato)) }
            return
        }
        val negocioId = cuenta.negocioId
        if (estado.tieneNegocio && negocioId != null) {
            try {
                Negocios.actualizarFicha(
                    negocioId = negocioId,
                    operadorId = cuenta.id,
                    nombre = estado.nombreNegocio,
                    nit = estado.nit,
                    descripcion = estado.descripcion,
                    contacto = estado.contacto,
                    direccion = estado.direccion
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: Textos.obtener(R.string.texto_no_se_pudo_guardar_la_ficha)) }
                return
            }
        }
        _uiState.update {
            it.copy(guardada = cuenta.copy(nombre = estado.nombre.trim(), telefono = estado.telefono.trim().ifBlank { null }, fotoUri = estado.fotoUri))
        }
    }
}
