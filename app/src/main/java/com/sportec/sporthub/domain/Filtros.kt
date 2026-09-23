package com.sportec.sporthub.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CriteriosBusqueda(
    val categoria: String? = null,
    val precioMaximo: Int? = null,
    val radioKm: Int? = null
)

object Filtros {

    val RADIOS = listOf(2, 5, 10, 20)

    private val _criterios = MutableStateFlow(CriteriosBusqueda())
    val criterios: StateFlow<CriteriosBusqueda> = _criterios.asStateFlow()

    fun aplicar(criterios: CriteriosBusqueda) {
        _criterios.value = criterios
    }

    fun limpiar() {
        _criterios.value = CriteriosBusqueda()
    }

    fun cumple(item: ActividadConNegocio, origen: Origen): Boolean {
        val actuales = _criterios.value
        val precio = actuales.precioMaximo
        if (precio != null && item.actividad.precio > precio) return false
        val radio = actuales.radioKm
        if (radio != null) {
            val distancia = Ubicacion.distanciaKm(origen, item.negocio.latitud, item.negocio.longitud)
            if (distancia > radio) return false
        }
        return true
    }
}
