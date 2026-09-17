package com.sportec.sporthub.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BloqueoHorario(val actividadId: String, val fecha: String, val hora: Int)

/**
 * Catálogo mutable de negocios y sus servicios: ficha, validación, alta/edición
 * de actividades, condiciones y bloqueos de horario (HU-E01 a HU-E05).
 */
object Negocios {

    private var secuencia = 900
    private fun uid(prefijo: String) = prefijo + (++secuencia)

    private val _negocios = MutableStateFlow(DatosMock.negocios)
    val negocios: StateFlow<List<Negocio>> = _negocios.asStateFlow()

    private val _actividades = MutableStateFlow(DatosMock.actividades)
    val actividades: StateFlow<List<Actividad>> = _actividades.asStateFlow()

    private val _bloqueos = MutableStateFlow<List<BloqueoHorario>>(emptyList())

    fun negocioDe(id: String): Negocio? = _negocios.value.firstOrNull { it.id == id }

    fun actividadDe(id: String): Actividad? = _actividades.value.firstOrNull { it.id == id }

    fun actividadesDe(negocioId: String): List<Actividad> = _actividades.value.filter { it.negocioId == negocioId }

    fun operadorDe(negocioId: String): String? = DatosMock.cuentas.firstOrNull { it.negocioId == negocioId }?.id

    private fun exigirOperador(negocioId: String, operadorId: String) {
        if (operadorDe(negocioId) != operadorId) throw IllegalStateException("Esta ficha corresponde a otra cuenta de negocio.")
    }

    fun solicitarValidacion(negocioId: String, operadorId: String): Negocio {
        val negocio = negocioDe(negocioId) ?: throw IllegalArgumentException("El negocio no existe.")
        exigirOperador(negocioId, operadorId)
        val nuevoEstado = if (negocio.nombre.isNotBlank() && negocio.nit.isNotBlank()) EstadoNegocio.VALIDADO else EstadoNegocio.PENDIENTE
        val actualizado = negocio.copy(estado = nuevoEstado)
        _negocios.value = _negocios.value.map { if (it.id == negocioId) actualizado else it }
        return actualizado
    }

    fun actualizarFicha(negocioId: String, operadorId: String, descripcion: String, contacto: String, direccion: String): Negocio {
        val negocio = negocioDe(negocioId) ?: throw IllegalArgumentException("El negocio no existe.")
        exigirOperador(negocioId, operadorId)
        if (direccion.isBlank()) throw IllegalArgumentException("La dirección no puede estar vacía.")
        val actualizado = negocio.copy(descripcion = descripcion, contacto = contacto, direccion = direccion)
        _negocios.value = _negocios.value.map { if (it.id == negocioId) actualizado else it }
        return actualizado
    }

    fun guardarServicio(
        operadorId: String,
        negocioId: String,
        id: String?,
        nombre: String,
        categoria: String,
        tipo: TipoActividad,
        precio: Int,
        duracionMinutos: Int,
        capacidad: Int,
        descripcion: String
    ): Actividad {
        exigirOperador(negocioId, operadorId)
        if (negocioDe(negocioId)?.estado != EstadoNegocio.VALIDADO) throw IllegalStateException("El negocio debe estar validado para publicar servicios.")
        if (nombre.isBlank()) throw IllegalArgumentException("El nombre es obligatorio.")
        if (precio <= 0) throw IllegalArgumentException("El precio debe ser mayor a cero.")
        if (duracionMinutos <= 0) throw IllegalArgumentException("La duración debe ser mayor a cero.")
        if (capacidad <= 0) throw IllegalArgumentException("La capacidad debe ser mayor a cero.")

        val existente = id?.let { actividadDe(it) }
        if (existente != null) {
            val cambiaCompromiso = existente.duracionMinutos != duracionMinutos || existente.capacidad != capacidad || existente.tipo != tipo
            if (cambiaCompromiso && Reservas.tieneCompromisos(existente.id)) {
                throw IllegalStateException("No puedes cambiar duración, modalidad o capacidad con compromisos vigentes.")
            }
            val actualizada = existente.copy(
                nombre = nombre, categoria = categoria, tipo = tipo, precio = precio,
                duracionMinutos = duracionMinutos, capacidad = capacidad, descripcion = descripcion
            )
            _actividades.value = _actividades.value.map { if (it.id == existente.id) actualizada else it }
            return actualizada
        }
        val nueva = Actividad(
            id = uid("a"), negocioId = negocioId, nombre = nombre, categoria = categoria, tipo = tipo,
            precio = precio, duracionMinutos = duracionMinutos, capacidad = capacidad, descripcion = descripcion
        )
        _actividades.value = _actividades.value + nueva
        return nueva
    }

    fun actualizarPolitica(operadorId: String, actividadId: String, politica: Politica): Actividad {
        val actividad = actividadDe(actividadId) ?: throw IllegalArgumentException("El servicio no existe.")
        exigirOperador(actividad.negocioId, operadorId)
        if (politica.payMinutes <= 0) throw IllegalArgumentException("El plazo de pago debe ser mayor a cero.")
        if (politica.cancelHours < 0) throw IllegalArgumentException("La anticipación de cancelación no puede ser negativa.")
        if (politica.refundPercent !in 0..100) throw IllegalArgumentException("El porcentaje de devolución debe estar entre 0 y 100.")
        if (politica.refundHours <= 0) throw IllegalArgumentException("El plazo de devolución debe ser mayor a cero.")
        val actualizada = actividad.copy(politica = politica)
        _actividades.value = _actividades.value.map { if (it.id == actividadId) actualizada else it }
        return actualizada
    }

    fun estaBloqueado(actividadId: String, fecha: String, hora: Int): Boolean =
        _bloqueos.value.any { it.actividadId == actividadId && it.fecha == fecha && it.hora == hora }

    fun listarBloqueos(actividadId: String, fecha: String): List<Int> =
        _bloqueos.value.filter { it.actividadId == actividadId && it.fecha == fecha }.map { it.hora }

    fun bloquearHorario(operadorId: String, actividadId: String, fecha: String, hora: Int) {
        val actividad = actividadDe(actividadId) ?: throw IllegalArgumentException("El servicio no existe.")
        exigirOperador(actividad.negocioId, operadorId)
        if (Reservas.cuposLibres(actividad, fecha, hora) < actividad.capacidad) {
            throw IllegalStateException("Este horario ya tiene compromisos; no puede bloquearse.")
        }
        if (!estaBloqueado(actividadId, fecha, hora)) {
            _bloqueos.value = _bloqueos.value + BloqueoHorario(actividadId, fecha, hora)
        }
    }

    fun desbloquearHorario(operadorId: String, actividadId: String, fecha: String, hora: Int) {
        val actividad = actividadDe(actividadId) ?: throw IllegalArgumentException("El servicio no existe.")
        exigirOperador(actividad.negocioId, operadorId)
        _bloqueos.value = _bloqueos.value.filterNot { it.actividadId == actividadId && it.fecha == fecha && it.hora == hora }
    }
}
