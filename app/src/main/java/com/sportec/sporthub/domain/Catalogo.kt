package com.sportec.sporthub.domain

data class ResultadoExplorar(
    val categorias: List<Categoria>,
    val actividades: List<ActividadConNegocio>
)

object Catalogo {

    fun explorar(categoria: String?): ResultadoExplorar {
        val validados = DatosMock.negocios
            .filter { it.estado == EstadoNegocio.VALIDADO }
            .associateBy { it.id }
        val actividades = DatosMock.actividades
            .filter { categoria == null || it.categoria == categoria }
            .mapNotNull { actividad -> validados[actividad.negocioId]?.let { ActividadConNegocio(actividad, it) } }
        return ResultadoExplorar(DatosMock.categorias, actividades)
    }

    fun obtener(actividadId: String): ActividadConNegocio {
        val actividad = DatosMock.actividadPorId(actividadId)
            ?: throw IllegalArgumentException("El servicio ya no está disponible.")
        val negocio = DatosMock.negocios.firstOrNull { it.id == actividad.negocioId }
            ?: throw IllegalArgumentException("El establecimiento ya no está disponible.")
        return ActividadConNegocio(actividad, negocio)
    }
}
