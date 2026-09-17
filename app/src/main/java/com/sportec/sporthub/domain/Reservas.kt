package com.sportec.sporthub.domain

import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private val ZONA = ZoneId.of("America/Bogota")
private val ACTIVAS = setOf(EstadoSolicitud.FILA, EstadoSolicitud.REVISION, EstadoSolicitud.PAGO)

data class FranjaHoraria(val hora: Int, val cuposLibres: Int)

/**
 * Motor de reglas de reservas en memoria: cola de solicitudes por cupo,
 * aprobación/rechazo, pago simulado y cancelación con reembolso.
 *
 * ponytail: sin persistencia ni corrutina de barrido en segundo plano; el
 * vencimiento de turnos se revisa "al vuelo" (sweep) cuando se consulta cada
 * solicitud.
 */
object Reservas {

    private var secuencia = 100
    private fun uid(prefijo: String) = prefijo + (++secuencia)

    private val _solicitudes = MutableStateFlow(DatosMock.solicitudes)
    val solicitudes: StateFlow<List<Solicitud>> = _solicitudes.asStateFlow()

    private val _reservas = MutableStateFlow(DatosMock.reservas)
    val reservas: StateFlow<List<Reserva>> = _reservas.asStateFlow()

    private val _pagos = MutableStateFlow(DatosMock.pagos)
    private val _reembolsos = MutableStateFlow<List<Reembolso>>(emptyList())

    private val _notificaciones = MutableStateFlow(DatosMock.notificaciones)
    val notificaciones: StateFlow<List<Notificacion>> = _notificaciones.asStateFlow()

    fun ahora(): Long = System.currentTimeMillis()

    fun inicioMillis(fecha: String, hora: Int): Long =
        LocalDate.parse(fecha).atTime(hora, 0).atZone(ZONA).toInstant().toEpochMilli()

    fun finMillis(fecha: String, hora: Int, duracionHoras: Int): Long =
        inicioMillis(fecha, hora) + duracionHoras * 3_600_000L

    private fun solapan(fecha: String, hora: Int, duracion: Int, fecha2: String, hora2: Int, duracion2: Int): Boolean =
        inicioMillis(fecha, hora) < finMillis(fecha2, hora2, duracion2) &&
            inicioMillis(fecha2, hora2) < finMillis(fecha, hora, duracion)

    private fun operadorDe(negocioId: String): String? =
        DatosMock.cuentas.firstOrNull { it.negocioId == negocioId }?.id

    private fun notificar(usuarioId: String?, titulo: String, texto: String, ruta: String?, argumento: String?) {
        if (usuarioId == null) return
        _notificaciones.value = _notificaciones.value + Notificacion(uid("n"), usuarioId, titulo, texto, ruta, argumento)
    }

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

    fun listarFranjas(actividad: Actividad, fecha: String, horas: IntRange = 6..21): List<FranjaHoraria> =
        horas.map { FranjaHoraria(it, cuposLibres(actividad, fecha, it)) }

    private fun avanzarCola(actividadId: String, fecha: String, hora: Int) {
        val actividad = DatosMock.actividadPorId(actividadId) ?: return
        var libres = cuposLibres(actividad, fecha, hora)
        val candidatas = _solicitudes.value
            .filter { it.actividadId == actividadId && it.fecha == fecha && it.hora == hora && it.estado in ACTIVAS }
            .sortedBy { it.orden }
        val actualizadas = _solicitudes.value.toMutableList()
        for (candidata in candidatas) {
            if (candidata.estado == EstadoSolicitud.PAGO) continue
            val indice = actualizadas.indexOfFirst { it.id == candidata.id }
            if (libres > 0) {
                if (candidata.estado != EstadoSolicitud.REVISION) {
                    notificar(operadorDe(actividad.negocioId), "Nuevo candidato por revisar", "Hay una solicitud esperando tu aprobación.", "request", candidata.id)
                }
                actualizadas[indice] = candidata.copy(estado = EstadoSolicitud.REVISION)
                libres--
            } else {
                actualizadas[indice] = candidata.copy(estado = EstadoSolicitud.FILA)
            }
        }
        _solicitudes.value = actualizadas
    }

    fun enviarSolicitud(usuarioId: String, actividadId: String, fecha: String, hora: Int): Solicitud {
        val actividad = DatosMock.actividadPorId(actividadId)
            ?: throw IllegalArgumentException("El servicio no está disponible.")
        if (inicioMillis(fecha, hora) <= ahora()) {
            throw IllegalStateException("Este horario ya inició.")
        }
        val duracion = (actividad.duracionMinutos / 60).coerceAtLeast(1)
        if (_solicitudes.value.any { it.usuarioId == usuarioId && it.estado in ACTIVAS && it.actividadId == actividadId && it.fecha == fecha && it.hora == hora }) {
            throw IllegalStateException("Ya tienes una solicitud para este horario. Consulta Mis reservas.")
        }
        val nueva = Solicitud(
            id = uid("s"),
            usuarioId = usuarioId,
            actividadId = actividadId,
            fecha = fecha,
            hora = hora,
            duracionHoras = duracion,
            orden = secuencia,
            estado = EstadoSolicitud.FILA,
            precio = actividad.precio,
            politica = actividad.politica
        )
        _solicitudes.value = _solicitudes.value + nueva
        avanzarCola(actividadId, fecha, hora)
        notificar(operadorDe(actividad.negocioId), "Nueva solicitud", "Hay una nueva solicitud para ${actividad.nombre}.", "request", nueva.id)
        return _solicitudes.value.first { it.id == nueva.id }
    }

    private fun sweepSiVencida(solicitud: Solicitud): Solicitud {
        val vencida = solicitud.estado in ACTIVAS &&
            ((solicitud.estado == EstadoSolicitud.PAGO && solicitud.deadline?.let { ahora() >= it } == true) ||
                ahora() >= inicioMillis(solicitud.fecha, solicitud.hora))
        if (!vencida) return solicitud
        val actualizada = solicitud.copy(estado = EstadoSolicitud.VENCIDA)
        _solicitudes.value = _solicitudes.value.map { if (it.id == solicitud.id) actualizada else it }
        avanzarCola(solicitud.actividadId, solicitud.fecha, solicitud.hora)
        return actualizada
    }

    fun obtenerSolicitud(id: String): Solicitud? =
        _solicitudes.value.firstOrNull { it.id == id }?.let { sweepSiVencida(it) }

    fun obtenerReserva(id: String): Reserva? = _reservas.value.firstOrNull { it.id == id }

    fun listarSolicitudesUsuario(usuarioId: String): List<Solicitud> =
        _solicitudes.value.filter { it.usuarioId == usuarioId }.map { sweepSiVencida(it) }

    fun listarReservasUsuario(usuarioId: String): List<Reserva> =
        _reservas.value.filter { it.usuarioId == usuarioId }

    fun aprobarSolicitud(id: String, operadorId: String): Solicitud {
        val solicitud = obtenerSolicitud(id) ?: throw IllegalArgumentException("La solicitud ya no existe.")
        val actividad = DatosMock.actividadPorId(solicitud.actividadId)!!
        if (operadorDe(actividad.negocioId) != operadorId) {
            throw IllegalStateException("Esta solicitud corresponde a otra cuenta de negocio.")
        }
        if (solicitud.estado != EstadoSolicitud.REVISION) {
            throw IllegalStateException("Esta solicitud ya no está en revisión.")
        }
        val actualizada = solicitud.copy(
            estado = EstadoSolicitud.PAGO,
            deadline = minOf(ahora() + solicitud.politica.payMinutes * 60_000L, inicioMillis(solicitud.fecha, solicitud.hora))
        )
        _solicitudes.value = _solicitudes.value.map { if (it.id == id) actualizada else it }
        notificar(solicitud.usuarioId, "Solicitud aceptada · puedes pagar", "Revisa el plazo antes de que venza tu turno.", "request", id)
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
        val solicitud = obtenerSolicitud(id) ?: throw IllegalArgumentException("La solicitud ya no existe.")
        val actividad = DatosMock.actividadPorId(solicitud.actividadId)!!
        if (rechazadaPor != null && operadorDe(actividad.negocioId) != rechazadaPor) {
            throw IllegalStateException("Solo el establecimiento puede rechazar.")
        }
        if (retiradaPor != null && solicitud.usuarioId != retiradaPor) {
            throw IllegalStateException("No tienes acceso a esta solicitud.")
        }
        if (solicitud.estado !in ACTIVAS) throw IllegalStateException("La solicitud ya no está activa.")
        val nuevoEstado = if (rechazadaPor != null) EstadoSolicitud.RECHAZADA else EstadoSolicitud.RETIRADA
        _solicitudes.value = _solicitudes.value.map { if (it.id == id) it.copy(estado = nuevoEstado) else it }
        notificar(solicitud.usuarioId, if (rechazadaPor != null) "Solicitud rechazada" else "Solicitud retirada", motivo.ifBlank { "El turno quedó liberado." }, "request", id)
        avanzarCola(solicitud.actividadId, solicitud.fecha, solicitud.hora)
    }

    fun pagarSolicitud(id: String, medio: String, usuarioId: String): Reserva {
        val solicitud = obtenerSolicitud(id) ?: throw IllegalArgumentException("La solicitud ya no existe.")
        if (solicitud.usuarioId != usuarioId || solicitud.estado != EstadoSolicitud.PAGO) {
            throw IllegalStateException("No hay un turno de pago vigente.")
        }
        val pago = Pago(uid("p"), usuarioId, solicitud.precio, EstadoPago.EXITOSO, medio)
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
        _pagos.value = _pagos.value + pago.copy(reservaId = reserva.id)
        _reservas.value = _reservas.value + reserva
        _solicitudes.value = _solicitudes.value.map { if (it.id == id) it.copy(estado = EstadoSolicitud.ATENDIDA) else it }
        val actividad = DatosMock.actividadPorId(solicitud.actividadId)!!
        notificar(usuarioId, "Reserva confirmada", "Tu pago fue exitoso. Ya puedes consultar tu reserva.", "reservation", reserva.id)
        notificar(operadorDe(actividad.negocioId), "Pago recibido", "Se confirmó ${actividad.nombre}.", "reservation", reserva.id)
        avanzarCola(solicitud.actividadId, solicitud.fecha, solicitud.hora)
        return reserva
    }

    fun cancelarReserva(id: String, motivo: String, usuarioId: String) {
        val reserva = obtenerReserva(id) ?: throw IllegalArgumentException("La reserva ya no existe.")
        if (reserva.usuarioId != usuarioId || reserva.estado != EstadoReserva.CONFIRMADA || reserva.cerrada) {
            throw IllegalStateException("No puedes cancelar esta reserva.")
        }
        val limite = inicioMillis(reserva.fecha, reserva.hora) - reserva.politica.cancelHours * 3_600_000L
        if (ahora() > limite) {
            throw IllegalStateException("El plazo de cancelación establecido ya terminó.")
        }
        _reservas.value = _reservas.value.map { if (it.id == id) it.copy(estado = EstadoReserva.CANCELADA) else it }
        val monto = Math.round(reserva.precio * reserva.politica.refundPercent / 100.0).toInt()
        if (monto > 0 && reserva.pagoId != null) {
            _reembolsos.value = _reembolsos.value + Reembolso(
                id = uid("ref"),
                reservaId = id,
                usuarioId = usuarioId,
                pagoId = reserva.pagoId,
                monto = monto,
                causa = "cancelación",
                plazo = ahora() + reserva.politica.refundHours * 3_600_000L
            )
        }
        val actividad = DatosMock.actividadPorId(reserva.actividadId)!!
        notificar(operadorDe(actividad.negocioId), "Reserva cancelada", "Revisa la devolución aplicable.", "reservation", id)
    }

    fun marcarNotificacionLeida(id: String) {
        _notificaciones.value = _notificaciones.value.map { if (it.id == id) it.copy(leida = true) else it }
    }
}
