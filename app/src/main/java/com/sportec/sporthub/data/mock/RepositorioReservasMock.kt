package com.sportec.sporthub.data.mock

import com.sportec.sporthub.data.model.Actividad
import com.sportec.sporthub.data.model.EstadoPago
import com.sportec.sporthub.data.model.EstadoReserva
import com.sportec.sporthub.data.model.EstadoSolicitud
import com.sportec.sporthub.data.model.Notificacion
import com.sportec.sporthub.data.model.Pago
import com.sportec.sporthub.data.model.Reembolso
import com.sportec.sporthub.data.model.Reserva
import com.sportec.sporthub.data.model.Solicitud
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private val ZONA = ZoneId.of("America/Bogota")
private val ACTIVAS = setOf(EstadoSolicitud.FILA, EstadoSolicitud.REVISION, EstadoSolicitud.PAGO)

data class FranjaHoraria(val hora: Int, val cuposLibres: Int)

/**
 * Motor de reglas de reservas en memoria, equivalente al objeto `S` y las
 * funciones submitRequest/approveRequest/payRequest/etc. de model.js.
 *
 * ponytail: estado en un MutableStateFlow por lista, sin persistencia ni
 * corrutina de barrido en segundo plano. El vencimiento de turnos se revisa
 * "al vuelo" (sweep) cuando se abre cada solicitud/reserva. Si se necesita
 * un contador en vivo, agregar un ticker en la pantalla de pago.
 */
object RepositorioReservasMock {

    var simularError: Boolean = false

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

    /** Cupos libres de [actividad] en [fecha]/[hora], descontando reservas confirmadas y pagos en curso. */
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
        val actividad = DatosMock.actividades.first { it.id == actividadId }
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

    suspend fun enviarSolicitud(usuarioId: String, actividadId: String, fecha: String, hora: Int): Result<Solicitud> {
        delay(500)
        if (simularError) return Result.failure(ErrorMock("No se pudo enviar la solicitud."))
        val actividad = DatosMock.actividades.firstOrNull { it.id == actividadId }
            ?: return Result.failure(ErrorMock("El servicio no está disponible."))
        if (inicioMillis(fecha, hora) <= ahora()) {
            return Result.failure(ErrorMock("Este horario ya inició."))
        }
        val duracion = (actividad.duracionMinutos / 60).coerceAtLeast(1)
        if (_solicitudes.value.any { it.usuarioId == usuarioId && it.estado in ACTIVAS && it.actividadId == actividadId && it.fecha == fecha && it.hora == hora }) {
            return Result.failure(ErrorMock("Ya tienes una solicitud para este horario. Consulta Mis reservas."))
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
        return Result.success(_solicitudes.value.first { it.id == nueva.id })
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

    suspend fun aprobarSolicitud(id: String, operadorId: String): Result<Solicitud> {
        delay(400)
        val solicitud = obtenerSolicitud(id) ?: return Result.failure(ErrorMock("La solicitud ya no existe."))
        val actividad = DatosMock.actividades.first { it.id == solicitud.actividadId }
        if (operadorDe(actividad.negocioId) != operadorId) {
            return Result.failure(ErrorMock("Esta solicitud corresponde a otra cuenta de negocio."))
        }
        if (solicitud.estado != EstadoSolicitud.REVISION) {
            return Result.failure(ErrorMock("Esta solicitud ya no está en revisión."))
        }
        val actualizada = solicitud.copy(
            estado = EstadoSolicitud.PAGO,
            deadline = minOf(ahora() + solicitud.politica.payMinutes * 60_000L, inicioMillis(solicitud.fecha, solicitud.hora))
        )
        _solicitudes.value = _solicitudes.value.map { if (it.id == id) actualizada else it }
        notificar(solicitud.usuarioId, "Solicitud aceptada · puedes pagar", "Revisa el plazo antes de que venza tu turno.", "request", id)
        return Result.success(actualizada)
    }

    suspend fun rechazarSolicitud(id: String, motivo: String, operadorId: String): Result<Unit> =
        retirarOrechazar(id, motivo, rechazadaPor = operadorId)

    suspend fun retirarSolicitud(id: String, motivo: String, usuarioId: String): Result<Unit> =
        retirarOrechazar(id, motivo, retiradaPor = usuarioId)

    private suspend fun retirarOrechazar(
        id: String,
        motivo: String,
        rechazadaPor: String? = null,
        retiradaPor: String? = null
    ): Result<Unit> {
        delay(400)
        val solicitud = obtenerSolicitud(id) ?: return Result.failure(ErrorMock("La solicitud ya no existe."))
        val actividad = DatosMock.actividades.first { it.id == solicitud.actividadId }
        if (rechazadaPor != null && operadorDe(actividad.negocioId) != rechazadaPor) {
            return Result.failure(ErrorMock("Solo el establecimiento puede rechazar."))
        }
        if (retiradaPor != null && solicitud.usuarioId != retiradaPor) {
            return Result.failure(ErrorMock("No tienes acceso a esta solicitud."))
        }
        if (solicitud.estado !in ACTIVAS) return Result.failure(ErrorMock("La solicitud ya no está activa."))
        val nuevoEstado = if (rechazadaPor != null) EstadoSolicitud.RECHAZADA else EstadoSolicitud.RETIRADA
        _solicitudes.value = _solicitudes.value.map { if (it.id == id) it.copy(estado = nuevoEstado) else it }
        notificar(solicitud.usuarioId, if (rechazadaPor != null) "Solicitud rechazada" else "Solicitud retirada", motivo.ifBlank { "El turno quedó liberado." }, "request", id)
        avanzarCola(solicitud.actividadId, solicitud.fecha, solicitud.hora)
        return Result.success(Unit)
    }

    suspend fun pagarSolicitud(id: String, medio: String, usuarioId: String): Result<Reserva> {
        delay(700)
        val solicitud = obtenerSolicitud(id) ?: return Result.failure(ErrorMock("La solicitud ya no existe."))
        if (solicitud.usuarioId != usuarioId || solicitud.estado != EstadoSolicitud.PAGO) {
            return Result.failure(ErrorMock("No hay un turno de pago vigente."))
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
        val actividad = DatosMock.actividades.first { it.id == solicitud.actividadId }
        notificar(usuarioId, "Reserva confirmada", "Tu pago fue exitoso. Ya puedes consultar tu reserva.", "reservation", reserva.id)
        notificar(operadorDe(actividad.negocioId), "Pago recibido", "Se confirmó ${actividad.nombre}.", "reservation", reserva.id)
        avanzarCola(solicitud.actividadId, solicitud.fecha, solicitud.hora)
        return Result.success(reserva)
    }

    suspend fun cancelarReserva(id: String, motivo: String, usuarioId: String): Result<Unit> {
        delay(500)
        val reserva = obtenerReserva(id) ?: return Result.failure(ErrorMock("La reserva ya no existe."))
        if (reserva.usuarioId != usuarioId || reserva.estado != EstadoReserva.CONFIRMADA || reserva.cerrada) {
            return Result.failure(ErrorMock("No puedes cancelar esta reserva."))
        }
        val limite = inicioMillis(reserva.fecha, reserva.hora) - reserva.politica.cancelHours * 3_600_000L
        if (ahora() > limite) {
            return Result.failure(ErrorMock("El plazo de cancelación establecido ya terminó."))
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
        val actividad = DatosMock.actividades.first { it.id == reserva.actividadId }
        notificar(operadorDe(actividad.negocioId), "Reserva cancelada", "Revisa la devolución aplicable.", "reservation", id)
        return Result.success(Unit)
    }

    fun marcarNotificacionLeida(id: String) {
        _notificaciones.value = _notificaciones.value.map { if (it.id == id) it.copy(leida = true) else it }
    }
}
