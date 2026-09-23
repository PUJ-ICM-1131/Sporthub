package com.sportec.sporthub.domain

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos

import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private val ZONA = ZoneId.of("America/Bogota")
private val ACTIVAS = setOf(EstadoSolicitud.FILA, EstadoSolicitud.REVISION, EstadoSolicitud.PAGO)

data class FranjaHoraria(val hora: Int, val cuposLibres: Int)

object Reservas {

    private var secuencia = 100
    private fun uid(prefijo: String) = prefijo + (++secuencia)

    private val _solicitudes = MutableStateFlow(DatosMock.solicitudes)
    val solicitudes: StateFlow<List<Solicitud>> = _solicitudes.asStateFlow()

    private val _reservas = MutableStateFlow(DatosMock.reservas)
    val reservas: StateFlow<List<Reserva>> = _reservas.asStateFlow()

    private val _pagos = MutableStateFlow(DatosMock.pagos)
    val pagos: StateFlow<List<Pago>> = _pagos.asStateFlow()

    private val _reembolsos = MutableStateFlow<List<Reembolso>>(emptyList())
    val reembolsos: StateFlow<List<Reembolso>> = _reembolsos.asStateFlow()

    fun ahora(): Long = System.currentTimeMillis()

    fun inicioMillis(fecha: String, hora: Int): Long =
        LocalDate.parse(fecha).atTime(hora, 0).atZone(ZONA).toInstant().toEpochMilli()

    fun finMillis(fecha: String, hora: Int, duracionHoras: Int): Long =
        inicioMillis(fecha, hora) + duracionHoras * 3_600_000L

    private fun solapan(fecha: String, hora: Int, duracion: Int, fecha2: String, hora2: Int, duracion2: Int): Boolean =
        inicioMillis(fecha, hora) < finMillis(fecha2, hora2, duracion2) &&
            inicioMillis(fecha2, hora2) < finMillis(fecha, hora, duracion)

    fun cuposLibres(actividad: Actividad, fecha: String, hora: Int): Int {
        val duracion = (actividad.duracionMinutos / 60).coerceAtLeast(1)
        val ocupadosPorReserva = _reservas.value.count {
            it.actividadId == actividad.id && it.estado == EstadoReserva.CONFIRMADA && !it.cerrada &&
                solapan(it.fecha, it.hora, it.duracionHoras, fecha, hora, duracion)
        }
        val ocupadosPorPago = _solicitudes.value.count {
            it.actividadId == actividad.id && it.estado == EstadoSolicitud.PAGO &&
                solapan(it.fecha, it.hora, it.duracionHoras, fecha, hora, duracion)
        }
        return actividad.capacidad - ocupadosPorReserva - ocupadosPorPago
    }

    fun listarFranjas(actividad: Actividad, fecha: String): List<FranjaHoraria> =
        (actividad.horaApertura until actividad.horaCierre).map { FranjaHoraria(it, cuposLibres(actividad, fecha, it)) }

    fun tieneCompromisos(actividadId: String): Boolean =
        _reservas.value.any { it.actividadId == actividadId && it.estado == EstadoReserva.CONFIRMADA && !it.cerrada } ||
            _solicitudes.value.any { it.actividadId == actividadId && it.estado == EstadoSolicitud.PAGO }

    fun enviarSolicitud(usuarioId: String, actividadId: String, fecha: String, hora: Int): Solicitud {
        val actividad = Negocios.actividadDe(actividadId)
            ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_el_servicio_no_esta_disponible))
        if (inicioMillis(fecha, hora) <= ahora()) {
            throw IllegalStateException(Textos.obtener(R.string.texto_este_horario_ya_inicio))
        }
        val duracion = (actividad.duracionMinutos / 60).coerceAtLeast(1)
        if (_solicitudes.value.any { it.usuarioId == usuarioId && it.estado in ACTIVAS && it.actividadId == actividadId && it.fecha == fecha && it.hora == hora }) {
            throw IllegalStateException(Textos.obtener(R.string.texto_ya_tienes_una_solicitud_para_este_horario_consulta_mis))
        }
        val nueva = Solicitud(
            id = uid("s"),
            usuarioId = usuarioId,
            actividadId = actividadId,
            fecha = fecha,
            hora = hora,
            duracionHoras = duracion,
            orden = secuencia,
            estado = EstadoSolicitud.REVISION,
            precio = actividad.precio,
            politica = actividad.politica
        )
        _solicitudes.value = _solicitudes.value + nueva
        return _solicitudes.value.first { it.id == nueva.id }
    }

    fun obtenerSolicitud(id: String): Solicitud? =
        _solicitudes.value.firstOrNull { it.id == id }

    fun obtenerReserva(id: String): Reserva? = _reservas.value.firstOrNull { it.id == id }

    fun listarSolicitudesUsuario(usuarioId: String): List<Solicitud> =
        _solicitudes.value.filter { it.usuarioId == usuarioId }

    fun listarReservasUsuario(usuarioId: String): List<Reserva> =
        _reservas.value.filter { it.usuarioId == usuarioId }

    fun listarSolicitudesNegocio(negocioId: String): List<Solicitud> {
        val actividadesNegocio = Negocios.actividadesDe(negocioId).map { it.id }.toSet()
        return _solicitudes.value.filter { it.actividadId in actividadesNegocio }
    }

    fun listarReservasNegocio(negocioId: String): List<Reserva> {
        val actividadesNegocio = Negocios.actividadesDe(negocioId).map { it.id }.toSet()
        return _reservas.value.filter { it.actividadId in actividadesNegocio }
    }

    fun listarPagosNegocio(negocioId: String): List<Pago> {
        val actividadesNegocio = Negocios.actividadesDe(negocioId).map { it.id }.toSet()
        return _pagos.value.filter { pago -> pago.reservaId?.let { obtenerReserva(it)?.actividadId in actividadesNegocio } == true }
    }

    fun aprobarSolicitud(id: String, operadorId: String): Solicitud {
        val solicitud = obtenerSolicitud(id) ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_la_solicitud_ya_no_existe))
        val actividad = Negocios.actividadDe(solicitud.actividadId)!!
        if (solicitud.estado != EstadoSolicitud.REVISION) {
            throw IllegalStateException(Textos.obtener(R.string.texto_esta_solicitud_ya_no_esta_en_revision))
        }
        val actualizada = solicitud.copy(
            estado = EstadoSolicitud.PAGO,
            deadline = minOf(ahora() + solicitud.politica.payMinutes * 60_000L, inicioMillis(solicitud.fecha, solicitud.hora))
        )
        _solicitudes.value = _solicitudes.value.map { if (it.id == id) actualizada else it }
        return actualizada
    }

    fun rechazarSolicitud(id: String, motivo: String, operadorId: String) =
        retirarOrechazar(id, motivo, rechazadaPor = operadorId)

    fun retirarSolicitud(id: String, motivo: String, usuarioId: String) =
        retirarOrechazar(id, motivo, retiradaPor = usuarioId)

    private fun retirarOrechazar(
        id: String,
        motivo: String,
        rechazadaPor: String? = null,
        retiradaPor: String? = null
    ) {
        val solicitud = obtenerSolicitud(id) ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_la_solicitud_ya_no_existe))
        val actividad = Negocios.actividadDe(solicitud.actividadId)!!
        if (retiradaPor != null && solicitud.usuarioId != retiradaPor) {
            throw IllegalStateException(Textos.obtener(R.string.texto_no_tienes_acceso_a_esta_solicitud))
        }
        if (solicitud.estado !in ACTIVAS) throw IllegalStateException(Textos.obtener(R.string.texto_la_solicitud_ya_no_esta_activa))
        val nuevoEstado = if (rechazadaPor != null) EstadoSolicitud.RECHAZADA else EstadoSolicitud.RETIRADA
        _solicitudes.value = _solicitudes.value.map { if (it.id == id) it.copy(estado = nuevoEstado) else it }
    }

    fun registrarPago(usuarioId: String, monto: Int, medio: String): Pago {
        val pago = Pago(uid("p"), usuarioId, monto, EstadoPago.EXITOSO, medio)
        _pagos.value = _pagos.value + pago
        return pago
    }

    fun registrarReembolso(reservaId: String, usuarioId: String, pagoId: String, monto: Int, causa: String, refundHours: Int): Reembolso {
        val reembolso = Reembolso(
            id = uid("ref"),
            reservaId = reservaId,
            usuarioId = usuarioId,
            pagoId = pagoId,
            monto = monto,
            causa = causa,
            plazo = ahora() + refundHours * 3_600_000L
        )
        _reembolsos.value = _reembolsos.value + reembolso
        return reembolso
    }

    fun listarReembolsosUsuario(usuarioId: String): List<Reembolso> = _reembolsos.value.filter { it.usuarioId == usuarioId }

    fun listarReembolsosNegocio(negocioId: String): List<Reembolso> {
        val actividadesNegocio = Negocios.actividadesDe(negocioId).map { it.id }.toSet()
        return _reembolsos.value.filter { obtenerReserva(it.reservaId)?.actividadId in actividadesNegocio }
    }

    fun confirmarReembolso(id: String, operadorId: String, exitoso: Boolean) {
        val reembolso = _reembolsos.value.firstOrNull { it.id == id } ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_el_reembolso_ya_no_existe))
        if (reembolso.estado != EstadoReembolso.PENDIENTE) throw IllegalStateException(Textos.obtener(R.string.texto_este_reembolso_ya_fue_procesado))
        val reserva = obtenerReserva(reembolso.reservaId) ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_la_reserva_ya_no_existe))
        val actividad = Negocios.actividadDe(reserva.actividadId)!!
        val actualizado = reembolso.copy(estado = if (exitoso) EstadoReembolso.REALIZADO else EstadoReembolso.FALLIDO)
        _reembolsos.value = _reembolsos.value.map { if (it.id == id) actualizado else it }
        if (exitoso) {
        }
    }

    fun pagarSolicitud(id: String, medio: String, usuarioId: String): Reserva {
        val solicitud = obtenerSolicitud(id) ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_la_solicitud_ya_no_existe))
        if (solicitud.usuarioId != usuarioId || solicitud.estado != EstadoSolicitud.PAGO) {
            throw IllegalStateException(Textos.obtener(R.string.texto_no_hay_un_turno_de_pago_vigente))
        }
        val pago = registrarPago(usuarioId, solicitud.precio, medio)
        val reserva = Reserva(
            id = uid("r"),
            usuarioId = usuarioId,
            actividadId = solicitud.actividadId,
            fecha = solicitud.fecha,
            hora = solicitud.hora,
            duracionHoras = solicitud.duracionHoras,
            precio = solicitud.precio,
            estado = EstadoReserva.CONFIRMADA,
            pagoId = pago.id,
            politica = solicitud.politica
        )
        _pagos.value = _pagos.value.map { if (it.id == pago.id) it.copy(reservaId = reserva.id) else it }
        _reservas.value = _reservas.value + reserva
        _solicitudes.value = _solicitudes.value.map { if (it.id == id) it.copy(estado = EstadoSolicitud.ATENDIDA) else it }
        val actividad = Negocios.actividadDe(solicitud.actividadId)!!
        return reserva
    }

    fun cancelarReserva(id: String, motivo: String, usuarioId: String) {
        val reserva = obtenerReserva(id) ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_la_reserva_ya_no_existe))
        if (reserva.usuarioId != usuarioId || reserva.estado != EstadoReserva.CONFIRMADA || reserva.cerrada) {
            throw IllegalStateException(Textos.obtener(R.string.texto_no_puedes_cancelar_esta_reserva))
        }
        val limite = inicioMillis(reserva.fecha, reserva.hora) - reserva.politica.cancelHours * 3_600_000L
        if (ahora() > limite) {
            throw IllegalStateException(Textos.obtener(R.string.texto_el_plazo_de_cancelacion_establecido_ya_termino))
        }
        _reservas.value = _reservas.value.map { if (it.id == id) it.copy(estado = EstadoReserva.CANCELADA) else it }
        val monto = Math.round(reserva.precio * reserva.politica.refundPercent / 100.0).toInt()
        if (monto > 0 && reserva.pagoId != null) {
            registrarReembolso(id, usuarioId, reserva.pagoId, monto, Textos.obtener(R.string.texto_cancelacion), reserva.politica.refundHours)
        }
        val actividad = Negocios.actividadDe(reserva.actividadId)!!
    }

    fun cambiarTitular(reservaId: String, nuevoUsuarioId: String, nuevoPagoId: String): Reserva {
        val reserva = obtenerReserva(reservaId) ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_la_reserva_ya_no_existe))
        val actualizada = reserva.copy(usuarioId = nuevoUsuarioId, pagoId = nuevoPagoId)
        _reservas.value = _reservas.value.map { if (it.id == reservaId) actualizada else it }
        _pagos.value = _pagos.value.map { if (it.id == nuevoPagoId) it.copy(reservaId = reservaId) else it }
        return actualizada
    }

    fun registrarLlegada(reservaId: String, usuarioId: String) {
        val reserva = obtenerReserva(reservaId)
            ?: throw IllegalArgumentException(Textos.obtener(R.string.reserva_no_encontrada))
        if (reserva.usuarioId != usuarioId) {
            throw IllegalArgumentException(Textos.obtener(R.string.reserva_no_es_tuya))
        }
        _reservas.value = _reservas.value.map {
            if (it.id == reservaId) it.copy(llegada = true) else it
        }
    }

    fun adjuntarFotoLlegada(reservaId: String, usuarioId: String, uri: String) {
        val reserva = obtenerReserva(reservaId)
            ?: throw IllegalArgumentException(Textos.obtener(R.string.reserva_no_encontrada))
        if (reserva.usuarioId != usuarioId) {
            throw IllegalArgumentException(Textos.obtener(R.string.reserva_no_es_tuya))
        }
        _reservas.value = _reservas.value.map {
            if (it.id == reservaId) it.copy(llegada = true, fotoLlegada = uri) else it
        }
    }

    fun cerrarActividad(reservaId: String, operadorId: String) {
        val reserva = obtenerReserva(reservaId) ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_la_reserva_ya_no_existe))
        val actividad = Negocios.actividadDe(reserva.actividadId)!!
        if (ahora() < finMillis(reserva.fecha, reserva.hora, reserva.duracionHoras)) throw IllegalStateException(Textos.obtener(R.string.texto_el_horario_todavia_no_ha_finalizado))
        if (reserva.cerrada) throw IllegalStateException(Textos.obtener(R.string.texto_esta_actividad_ya_fue_cerrada))
        _reservas.value = _reservas.value.map { if (it.id == reservaId) it.copy(cerrada = true) else it }
    }

    fun enviarAvisoSalida(reservaId: String, operadorId: String) {
        val reserva = obtenerReserva(reservaId) ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_la_reserva_ya_no_existe))
        val actividad = Negocios.actividadDe(reserva.actividadId)!!
    }
}
