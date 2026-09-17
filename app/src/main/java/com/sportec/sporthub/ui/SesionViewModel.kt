package com.sportec.sporthub.ui

import androidx.lifecycle.ViewModel
import com.sportec.sporthub.data.model.Cuenta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SesionViewModel : ViewModel() {

    private val _cuenta = MutableStateFlow<Cuenta?>(null)
    val cuenta: StateFlow<Cuenta?> = _cuenta.asStateFlow()

    fun iniciar(cuenta: Cuenta) {
        _cuenta.value = cuenta
    }

    fun cerrar() {
        _cuenta.value = null
    }
}
