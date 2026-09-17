package com.sportec.sporthub.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private val ACTIVOS_POSTULANTE = setOf(EstadoPostulante.FILA, EstadoPostulante.REVISION, EstadoPostulante.PAGO)
private val VIGENTE = setOf(EstadoTransferencia.PUBLICADA, EstadoTransferencia.TRAMITE)

/** Motor de reservas transferibles: publicar, postular, aprobar y pagar (HU-D13 a HU-D17, HU-E10). */
object Transferencias {

    private var secuencia = 700
    private fun uid(prefijo: String) = prefijo + (++secuencia)

    private val _transferencias = MutableStateFlow(DatosMock.transferencias)
    val transferencias: StateFlow<List<Transferencia>> = _transferencias.asStateFlow()

    fun obtener(id: String): Transferencia? = _transferencias.value.firstOrNull { it.id == id }

    fun deReserva(reservaId: String): Transferencia? {
        val propias = _transferencias.value.filter { it.reservaId == reservaId }
        return propias.firstOrNull { it.estado in VIGENTE } ?: propias.lastOrNull()
    }

    fun listarPublicadas(): List<Transferencia> = _transferencias.value.filter { it.estado in VIGENTE }

    fun listarDeUsuario(usuarioId: String): List<Transferencia> =
        _transferencias.value.filter { it.propietarioId == usuarioId || it.postulantes.any { p -> p.usuarioId == usuarioId } }

    fun listarDeNegocio(negocioId: String): List<Transferencia> {
        val actividadesNegocio = Negocios.actividadesDe(negocioId).map { it.id }.toSet()
        return _transferencias.value.filter { Reservas.obtenerReserva(it.reservaId)?.actividadId in actividadesNegocio }
    }

    fun publicar(reservaId: String, usuarioId: String): Transferencia {
        val reserva = Reservas.obtenerReserva(reservaId) ?: throw IllegalArgumentException("La reserva ya no existe.")
        if (reserva.usuarioId != usuarioId || reserva.estado != EstadoReserva.CONFIRMADA) throw IllegalStateException("La reserva no es transferible en este estado.")
        if (!reserva.politica.transferable) throw IllegalStateException("Esta reserva no admite transferencia.")
        if (Reservas.ahora() >= Reservas.inicioMillis(reserva.fecha, reserva.hora)) throw IllegalStateException("El horario ya inició.")
        if (_transferencias.value.any { it.reservaId == reservaId && it.estado in VIGENTE }) {
            throw IllegalStateException("Ya existe una publicación activa para esta reserva.")
        }
        val nueva = Transferencia(uid("t"), reservaId, usuarioId, reserva.pagoId ?: "", EstadoTransferencia.PUBLICADA)
        _transferencias.value = _transferencias.value + nueva
        return nueva
    }

    private fun avanzar(transferenciaId: String) {
        val transferencia = obtener(transferenciaId) ?: return
        if (transferencia.postulantes.any { it.estado == EstadoPostulante.PAGO }) return
        val siguiente = transferencia.postulantes.firstOrNull { it.estado in setOf(EstadoPostulante.FILA, EstadoPostulante.REVISION) } ?: return
        if (siguiente.estado == EstadoPostulante.REVISION) return
        val reserva = Reservas.obtenerReserva(transferencia.reservaId)!!
        val actividad = Negocios.actividadDe(reserva.actividadId)!!
        _transferencias.value = _transferencias.value.map {
            if (it.id == transferenciaId) it.copy(postulantes = it.postulantes.map { p -> if (p.id == siguiente.id) p.copy(estado = EstadoPostulante.REVISION) else p }) else it
        }
        Notificaciones.enviar(Negocios.operadorDe(actividad.negocioId), "Interesado por revisar", "Alguien quiere tomar una reserva publicada.", "transfer", transferenciaId)
    }

    fun unirse(transferenciaId: String, usuarioId: String) {
        val transferencia = obtener(transferenciaId) ?: throw IllegalArgumentException("La publicación ya no existe.")
        val reserva = Reservas.obtenerReserva(transferencia.reservaId)!!
        if (transferencia.estado !in VIGENTE || Reservas.ahora() >= Reservas.inicioMillis(reserva.fecha, reserva.hora)) {
            throw IllegalStateException("Esta publicación ya no acepta interesados.")
        }
        if (usuarioId == transferencia.propietarioId) throw IllegalStateException("No puedes tomar tu propia publicación.")
        if (transferencia.postulantes.any { it.usuarioId == usuarioId && it.estado in ACTIVOS_POSTULANTE }) throw IllegalStateException("Ya estás en esta fila.")
        val nuevo = Postulante(uid("st"), usuarioId, EstadoPostulante.FILA)
        _transferencias.value = _transferencias.value.map { if (it.id == transferenciaId) it.copy(postulantes = it.postulantes + nuevo) else it }
        avanzar(transferenciaId)
    }

    fun aprobar(transferenciaId: String, operadorId: String) {
        val transferencia = obtener(transferenciaId) ?: throw IllegalArgumentException("La publicación ya no existe.")
        val reserva = Reservas.obtenerReserva(transferencia.reservaId)!!
        val actividad = Negocios.actividadDe(reserva.actividadId)!!
        if (Negocios.operadorDe(actividad.negocioId) != operadorId) throw IllegalStateException("Esta publicación corresponde a otra cuenta de negocio.")
        val candidato = transferencia.postulantes.firstOrNull { it.estado == EstadoPostulante.REVISION }
            ?: throw IllegalStateException("No hay un interesado por aprobar.")
        val deadline = minOf(Reservas.ahora() + reserva.politica.payMinutes * 60_000L, Reservas.inicioMillis(reserva.fecha, reserva.hora))
        _transferencias.value = _transferencias.value.map {
            if (it.id == transferenciaId) it.copy(
                estado = EstadoTransferencia.TRAMITE,
                postulantes = it.postulantes.map { p -> if (p.id == candidato.id) p.copy(estado = EstadoPostulante.PAGO, deadline = deadline) else p }
            ) else it
        }
        Notificaciones.enviar(candidato.usuarioId, "Transferencia aprobada", "Paga dentro del plazo para recibir la reserva.", "transfer", transferenciaId)
    }

    fun rechazar(transferenciaId: String, postulanteId: String, motivo: String, operadorId: String) {
        val transferencia = obtener(transferenciaId) ?: throw IllegalArgumentException("La publicación ya no existe.")
        val reserva = Reservas.obtenerReserva(transferencia.reservaId)!!
        val actividad = Negocios.actividadDe(reserva.actividadId)!!
        if (Negocios.operadorDe(actividad.negocioId) != operadorId) throw IllegalStateException("Esta publicación corresponde a otra cuenta de negocio.")
        val postulante = transferencia.postulantes.firstOrNull { it.id == postulanteId } ?: throw IllegalArgumentException("El interesado ya no existe.")
        _transferencias.value = _transferencias.value.map {
            if (it.id == transferenciaId) it.copy(postulantes = it.postulantes.map { p -> if (p.id == postulanteId) p.copy(estado = EstadoPostulante.RETIRADA) else p }) else it
        }
        Notificaciones.enviar(postulante.usuarioId, "Interés rechazado", motivo.ifBlank { "El establecimiento no aprobó tu interés." }, "transfer", transferenciaId)
        avanzar(transferenciaId)
    }

    fun pagar(transferenciaId: String, usuarioId: String, medio: String): Reserva {
        val transferencia = obtener(transferenciaId) ?: throw IllegalArgumentException("La publicación ya no existe.")
        val postulante = transferencia.postulantes.firstOrNull { it.usuarioId == usuarioId && it.estado == EstadoPostulante.PAGO }
            ?: throw IllegalStateException("No tienes un turno de pago vigente.")
        if (postulante.deadline != null && Reservas.ahora() >= postulante.deadline) throw IllegalStateException("El plazo de pago venció.")
        val reserva = Reservas.obtenerReserva(transferencia.reservaId)!!
        val nuevoPago = Reservas.registrarPago(usuarioId, reserva.precio, medio)
        val monto = Math.round(reserva.precio * reserva.politica.refundPercent / 100.0).toInt()
        if (monto > 0) {
            Reservas.registrarReembolso(reserva.id, transferencia.propietarioId, transferencia.pagoId, monto, "transferencia", reserva.politica.refundHours)
        }
        val reservaActualizada = Reservas.cambiarTitular(reserva.id, usuarioId, nuevoPago.id)
        _transferencias.value = _transferencias.value.map {
            if (it.id == transferenciaId) it.copy(
                estado = EstadoTransferencia.COMPLETADA,
                postulantes = it.postulantes.map { p ->
                    when {
                        p.id == postulante.id -> p.copy(estado = EstadoPostulante.ATENDIDA)
                        p.estado in setOf(EstadoPostulante.FILA, EstadoPostulante.REVISION) -> p.copy(estado = EstadoPostulante.RETIRADA)
                        else -> p
                    }
                }
            ) else it
        }
        Notificaciones.enviar(usuarioId, "Ahora la reserva es tuya", "El pago fue exitoso.", "reservation", reserva.id)
        Notificaciones.enviar(transferencia.propietarioId, "Transferencia completada", "Puedes consultar la devolución aplicable.", "transfer", transferenciaId)
        return reservaActualizada
    }

    fun retirarPublicacion(transferenciaId: String, usuarioId: String) {
        val transferencia = obtener(transferenciaId) ?: throw IllegalArgumentException("La publicación ya no existe.")
        if (transferencia.propietarioId != usuarioId) throw IllegalStateException("No puedes retirar esta publicación.")
        if (transferencia.postulantes.any { it.estado == EstadoPostulante.PAGO }) throw IllegalStateException("Hay un turno de pago activo; espera su resultado.")
        _transferencias.value = _transferencias.value.map { if (it.id == transferenciaId) it.copy(estado = EstadoTransferencia.RETIRADA) else it }
    }
}
