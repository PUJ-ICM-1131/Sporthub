package com.sportec.sporthub.domain

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private val ACTIVOS_POSTULANTE = setOf(EstadoPostulante.FILA, EstadoPostulante.REVISION, EstadoPostulante.PAGO)
private val VIGENTE = setOf(EstadoTransferencia.PUBLICADA, EstadoTransferencia.TRAMITE)

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
        val reserva = Reservas.obtenerReserva(reservaId) ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_la_reserva_ya_no_existe))
        if (reserva.usuarioId != usuarioId || reserva.estado != EstadoReserva.CONFIRMADA) throw IllegalStateException(Textos.obtener(R.string.texto_la_reserva_no_es_transferible_en_este_estado))
        if (!reserva.politica.transferable) throw IllegalStateException(Textos.obtener(R.string.texto_esta_reserva_no_admite_transferencia))
        if (Reservas.ahora() >= Reservas.inicioMillis(reserva.fecha, reserva.hora)) throw IllegalStateException(Textos.obtener(R.string.texto_el_horario_ya_inicio))
        if (_transferencias.value.any { it.reservaId == reservaId && it.estado in VIGENTE }) {
            throw IllegalStateException(Textos.obtener(R.string.texto_ya_existe_una_publicacion_activa_para_esta_reserva))
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
    }

    fun unirse(transferenciaId: String, usuarioId: String) {
        val transferencia = obtener(transferenciaId) ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_la_publicacion_ya_no_existe))
        val reserva = Reservas.obtenerReserva(transferencia.reservaId)!!
        if (transferencia.estado !in VIGENTE || Reservas.ahora() >= Reservas.inicioMillis(reserva.fecha, reserva.hora)) {
            throw IllegalStateException(Textos.obtener(R.string.texto_esta_publicacion_ya_no_acepta_interesados))
        }
        if (usuarioId == transferencia.propietarioId) throw IllegalStateException(Textos.obtener(R.string.texto_no_puedes_tomar_tu_propia_publicacion))
        if (transferencia.postulantes.any { it.usuarioId == usuarioId && it.estado in ACTIVOS_POSTULANTE }) throw IllegalStateException(Textos.obtener(R.string.texto_ya_estas_en_esta_fila))
        val nuevo = Postulante(uid("st"), usuarioId, EstadoPostulante.FILA)
        _transferencias.value = _transferencias.value.map { if (it.id == transferenciaId) it.copy(postulantes = it.postulantes + nuevo) else it }
        avanzar(transferenciaId)
    }

    fun aprobar(transferenciaId: String, operadorId: String) {
        val transferencia = obtener(transferenciaId) ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_la_publicacion_ya_no_existe))
        val reserva = Reservas.obtenerReserva(transferencia.reservaId)!!
        val actividad = Negocios.actividadDe(reserva.actividadId)!!
        val candidato = transferencia.postulantes.firstOrNull { it.estado == EstadoPostulante.REVISION }
            ?: throw IllegalStateException(Textos.obtener(R.string.texto_no_hay_un_interesado_por_aprobar))
        val deadline = minOf(Reservas.ahora() + reserva.politica.payMinutes * 60_000L, Reservas.inicioMillis(reserva.fecha, reserva.hora))
        _transferencias.value = _transferencias.value.map {
            if (it.id == transferenciaId) it.copy(
                estado = EstadoTransferencia.TRAMITE,
                postulantes = it.postulantes.map { p -> if (p.id == candidato.id) p.copy(estado = EstadoPostulante.PAGO, deadline = deadline) else p }
            ) else it
        }
    }

    fun rechazar(transferenciaId: String, postulanteId: String, motivo: String, operadorId: String) {
        val transferencia = obtener(transferenciaId) ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_la_publicacion_ya_no_existe))
        val reserva = Reservas.obtenerReserva(transferencia.reservaId)!!
        val actividad = Negocios.actividadDe(reserva.actividadId)!!
        val postulante = transferencia.postulantes.firstOrNull { it.id == postulanteId } ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_el_interesado_ya_no_existe))
        _transferencias.value = _transferencias.value.map {
            if (it.id == transferenciaId) it.copy(postulantes = it.postulantes.map { p -> if (p.id == postulanteId) p.copy(estado = EstadoPostulante.RETIRADA) else p }) else it
        }
        avanzar(transferenciaId)
    }

    fun pagar(transferenciaId: String, usuarioId: String, medio: String): Reserva {
        val transferencia = obtener(transferenciaId) ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_la_publicacion_ya_no_existe))
        val postulante = transferencia.postulantes.firstOrNull { it.usuarioId == usuarioId && it.estado == EstadoPostulante.PAGO }
            ?: throw IllegalStateException(Textos.obtener(R.string.texto_no_tienes_un_turno_de_pago_vigente))
        if (postulante.deadline != null && Reservas.ahora() >= postulante.deadline) throw IllegalStateException(Textos.obtener(R.string.texto_el_plazo_de_pago_vencio))
        val reserva = Reservas.obtenerReserva(transferencia.reservaId)!!
        val nuevoPago = Reservas.registrarPago(usuarioId, reserva.precio, medio)
        val monto = Math.round(reserva.precio * reserva.politica.refundPercent / 100.0).toInt()
        if (monto > 0) {
            Reservas.registrarReembolso(reserva.id, transferencia.propietarioId, transferencia.pagoId, monto, Textos.obtener(R.string.texto_transferencia), reserva.politica.refundHours)
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
        return reservaActualizada
    }

    fun retirarPublicacion(transferenciaId: String, usuarioId: String) {
        val transferencia = obtener(transferenciaId) ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_la_publicacion_ya_no_existe))
        if (transferencia.propietarioId != usuarioId) throw IllegalStateException(Textos.obtener(R.string.texto_no_puedes_retirar_esta_publicacion))
        if (transferencia.postulantes.any { it.estado == EstadoPostulante.PAGO }) throw IllegalStateException(Textos.obtener(R.string.texto_hay_un_turno_de_pago_activo_espera_su_resultado))
        _transferencias.value = _transferencias.value.map { if (it.id == transferenciaId) it.copy(estado = EstadoTransferencia.RETIRADA) else it }
    }
}
