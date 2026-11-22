package com.sportec.sporthub.domain

import com.sportec.sporthub.utils.FotosLocales
import com.sportec.sporthub.R
import com.sportec.sporthub.data.Condicion
import com.sportec.sporthub.data.Escritura
import com.sportec.sporthub.data.FotosRemotas
import com.sportec.sporthub.data.Mapeos
import com.sportec.sporthub.data.Nube
import com.sportec.sporthub.utils.Textos

import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private val ZONA = ZoneId.of("America/Bogota")
private val ACTIVAS = setOf(EstadoSolicitud.FILA, EstadoSolicitud.REVISION, EstadoSolicitud.PAGO)

private const val ORIGEN_ORDEN_MS = 1_767_225_600_000L

data class FranjaHoraria(val hora: Int, val cuposLibres: Int)

object Reservas {

    private fun uid(prefijo: String): String = prefijo + java.util.UUID.randomUUID().toString()

    private val _solicitudes = MutableStateFlow<List<Solicitud>>(emptyList())
    val solicitudes: StateFlow<List<Solicitud>> = _solicitudes.asStateFlow()

    private val _reservas = MutableStateFlow<List<Reserva>>(emptyList())
    val reservas: StateFlow<List<Reserva>> = _reservas.asStateFlow()

    private val _pagos = MutableStateFlow<List<Pago>>(emptyList())
    val pagos: StateFlow<List<Pago>> = _pagos.asStateFlow()

    private val _reembolsos = MutableStateFlow<List<Reembolso>>(emptyList())
    val reembolsos: StateFlow<List<Reembolso>> = _reembolsos.asStateFlow()

    private var ocupacion: Map<String, Int> = emptyMap()

    internal fun cargarSolicitudes(lista: List<Solicitud>) { _solicitudes.value = lista }
    internal fun cargarPagos(lista: List<Pago>) { _pagos.value = lista }
    internal fun cargarReembolsos(lista: List<Reembolso>) { _reembolsos.value = lista }
    internal fun cargarOcupacion(mapa: Map<String, Int>) { ocupacion = mapa }
    internal fun cargarReservas(lista: List<Reserva>) {
        _reservas.value = lista.map { reserva ->
            if (reserva.fotoLlegada != null) reserva
            else reserva.copy(fotoLlegada = FotosLocales.obtener("reserva", reserva.id + ":" + reserva.usuarioId))
        }
    }
    internal fun vaciar() {
        _solicitudes.value = emptyList(); _reservas.value = emptyList()
        _pagos.value = emptyList(); _reembolsos.value = emptyList(); ocupacion = emptyMap()
    }

    fun ahora(): Long = System.currentTimeMillis()

    fun inicioMillis(fecha: String, hora: Int): Long =
        LocalDate.parse(fecha).atTime(hora, 0).atZone(ZONA).toInstant().toEpochMilli()

    fun finMillis(fecha: String, hora: Int, duracionHoras: Int): Long =
        inicioMillis(fecha, hora) + duracionHoras * 3_600_000L

    private fun solapan(fecha: String, hora: Int, duracion: Int, fecha2: String, hora2: Int, duracion2: Int): Boolean =
        inicioMillis(fecha, hora) < finMillis(fecha2, hora2, duracion2) &&
            inicioMillis(fecha2, hora2) < finMillis(fecha, hora, duracion)

    private fun claveOcupacion(actividadId: String, fecha: String, hora: Int) = "${actividadId}_${fecha}_$hora"

    private fun negocioDe(actividadId: String): String = Negocios.actividadDe(actividadId)?.negocioId.orEmpty()

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

        val ocupadosEnNube = (0 until duracion).maxOfOrNull { ocupacion[claveOcupacion(actividad.id, fecha, hora + it)] ?: 0 } ?: 0
        return actividad.capacidad - maxOf(ocupadosPorReserva + ocupadosPorPago, ocupadosEnNube)
    }

    fun listarFranjas(actividad: Actividad, fecha: String): List<FranjaHoraria> =
        (actividad.horaApertura until actividad.horaCierre).map { FranjaHoraria(it, cuposLibres(actividad, fecha, it)) }

    fun tieneCompromisos(actividadId: String): Boolean =
        _reservas.value.any { it.actividadId == actividadId && it.estado == EstadoReserva.CONFIRMADA && !it.cerrada } ||
            _solicitudes.value.any { it.actividadId == actividadId && it.estado == EstadoSolicitud.PAGO }

    private fun ajustarOcupacion(actividadId: String, fecha: String, hora: Int, duracionHoras: Int, delta: Long) {
        (0 until duracionHoras).forEach { i ->
            Nube.escribir(Escritura.Mezclar(
                "ocupacion/${claveOcupacion(actividadId, fecha, hora + i)}",
                mapOf("n" to Nube.sumar(delta), "actividadId" to actividadId)
            ))
        }
    }

    private fun condicionesDeCupo(actividad: Actividad, fecha: String, hora: Int, duracionHoras: Int): List<Condicion> =
        (0 until duracionHoras).map {
            Condicion.Menor("ocupacion/${claveOcupacion(actividad.id, fecha, hora + it)}", "n", actividad.capacidad.toLong())
        }

    fun enviarSolicitud(usuarioId: String, actividadId: String, fecha: String, hora: Int): Solicitud = Nube.agrupar {
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
            orden = ((ahora() - ORIGEN_ORDEN_MS) / 1000).toInt(),
            estado = EstadoSolicitud.REVISION,
            precio = actividad.precio,
            politica = actividad.politica
        )
        _solicitudes.value = _solicitudes.value + nueva
        Nube.escribir(Escritura.Crear("solicitudes/${nueva.id}", Mapeos.mapa(nueva, actividad.negocioId)))
        Notificaciones.evento(usuarioId, R.string.aviso_solicitud_enviada, "request", nueva.id)
        Notificaciones.evento(Negocios.operadorDe(actividad.negocioId), R.string.aviso_solicitud_recibida)
        nueva
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
        val condiciones = listOf(Condicion.Igual("solicitudes/$id", "estado", EstadoSolicitud.REVISION.name)) +
            condicionesDeCupo(actividad, solicitud.fecha, solicitud.hora, solicitud.duracionHoras)
        return Nube.agrupar(*condiciones.toTypedArray()) {
            val actualizada = solicitud.copy(
                estado = EstadoSolicitud.PAGO,
                deadline = minOf(ahora() + solicitud.politica.payMinutes * 60_000L, inicioMillis(solicitud.fecha, solicitud.hora))
            )
            _solicitudes.value = _solicitudes.value.map { if (it.id == id) actualizada else it }
            Nube.escribir(Escritura.Actualizar("solicitudes/$id",
                mapOf("estado" to actualizada.estado.name, "deadline" to actualizada.deadline)))
            ajustarOcupacion(actividad.id, solicitud.fecha, solicitud.hora, solicitud.duracionHoras, +1)
            Notificaciones.evento(solicitud.usuarioId, R.string.aviso_solicitud_aprobada, "request", id)
            actualizada
        }
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
        Nube.agrupar(Condicion.Igual("solicitudes/$id", "estado", solicitud.estado.name)) {
            Notificaciones.evento(solicitud.usuarioId, if (rechazadaPor != null) R.string.aviso_solicitud_rechazada else R.string.aviso_solicitud_retirada, "request", id)
            Notificaciones.evento(Negocios.operadorDe(actividad.negocioId), R.string.aviso_solicitud_actualizada)
            val nuevoEstado = if (rechazadaPor != null) EstadoSolicitud.RECHAZADA else EstadoSolicitud.RETIRADA
            _solicitudes.value = _solicitudes.value.map { if (it.id == id) it.copy(estado = nuevoEstado) else it }
            Nube.escribir(Escritura.Actualizar("solicitudes/$id", mapOf("estado" to nuevoEstado.name)))

            if (solicitud.estado == EstadoSolicitud.PAGO) {
                ajustarOcupacion(actividad.id, solicitud.fecha, solicitud.hora, solicitud.duracionHoras, -1)
            }
        }
    }

    fun registrarPago(usuarioId: String, monto: Int, medio: String, id: String = uid("p")): Pago {
        val pago = Pago(id, usuarioId, monto, EstadoPago.EXITOSO, medio)
        _pagos.value = _pagos.value + pago
        Notificaciones.evento(usuarioId, R.string.aviso_pago_simulado)
        return pago
    }

    fun registrarReembolso(
        reservaId: String, usuarioId: String, pagoId: String, monto: Int, causa: String, refundHours: Int,
        id: String = uid("ref")
    ): Reembolso {
        val reembolso = Reembolso(
            id = id,
            reservaId = reservaId,
            usuarioId = usuarioId,
            pagoId = pagoId,
            monto = monto,
            causa = causa,
            plazo = ahora() + refundHours * 3_600_000L
        )
        _reembolsos.value = _reembolsos.value + reembolso
        val negocioId = obtenerReserva(reservaId)?.let { negocioDe(it.actividadId) }.orEmpty()
        Nube.escribir(Escritura.Crear("reembolsos/$id", Mapeos.mapa(reembolso, negocioId)))
        Notificaciones.evento(usuarioId, R.string.aviso_reembolso_pendiente)
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
        Negocios.actividadDe(reserva.actividadId)!!
        Nube.agrupar(Condicion.Igual("reembolsos/$id", "estado", EstadoReembolso.PENDIENTE.name)) {
            val actualizado = reembolso.copy(estado = if (exitoso) EstadoReembolso.REALIZADO else EstadoReembolso.FALLIDO)
            _reembolsos.value = _reembolsos.value.map { if (it.id == id) actualizado else it }
            Nube.escribir(Escritura.Actualizar("reembolsos/$id", mapOf("estado" to actualizado.estado.name)))
            Notificaciones.evento(reembolso.usuarioId, if (exitoso) R.string.aviso_reembolso_ok else R.string.aviso_reembolso_fallo)
        }
    }

    fun pagarSolicitud(id: String, medio: String, usuarioId: String): Reserva {
        val solicitud = obtenerSolicitud(id) ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_la_solicitud_ya_no_existe))
        if (solicitud.usuarioId != usuarioId || solicitud.estado != EstadoSolicitud.PAGO) {
            throw IllegalStateException(Textos.obtener(R.string.texto_no_hay_un_turno_de_pago_vigente))
        }

        return Nube.agrupar(Condicion.Igual("solicitudes/$id", "estado", EstadoSolicitud.PAGO.name)) {
            val negocioId = negocioDe(solicitud.actividadId)
            val reservaId = "r$id"
            val pago = registrarPago(usuarioId, solicitud.precio, medio, id = "p$id")
            val reserva = Reserva(
                id = reservaId,
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
            val pagoFinal = pago.copy(reservaId = reserva.id)
            _pagos.value = _pagos.value.map { if (it.id == pago.id) pagoFinal else it }
            _reservas.value = _reservas.value + reserva
            _solicitudes.value = _solicitudes.value.map { if (it.id == id) it.copy(estado = EstadoSolicitud.ATENDIDA) else it }
            Nube.escribir(
                Escritura.Crear("pagos/${pago.id}", Mapeos.mapa(pagoFinal, negocioId)),
                Escritura.Crear("reservas/${reserva.id}", Mapeos.mapa(reserva, negocioId)),
                Escritura.Actualizar("solicitudes/$id", mapOf("estado" to EstadoSolicitud.ATENDIDA.name))
            )
            Notificaciones.evento(usuarioId, R.string.aviso_reserva_confirmada, "reservation", reserva.id)
            Notificaciones.evento(Negocios.operadorDe(negocioId), R.string.aviso_pago_recibido)
            reserva
        }
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
        Nube.agrupar(Condicion.Igual("reservas/$id", "estado", EstadoReserva.CONFIRMADA.name)) {
            _reservas.value = _reservas.value.map { if (it.id == id) it.copy(estado = EstadoReserva.CANCELADA) else it }
            Nube.escribir(Escritura.Actualizar("reservas/$id",
                mapOf("estado" to EstadoReserva.CANCELADA.name, "enTransferencia" to false)))
            ajustarOcupacion(reserva.actividadId, reserva.fecha, reserva.hora, reserva.duracionHoras, -1)
            val monto = Math.round(reserva.precio * reserva.politica.refundPercent / 100.0).toInt()
            if (monto > 0 && reserva.pagoId != null) {
                registrarReembolso(id, usuarioId, reserva.pagoId, monto, Textos.obtener(R.string.texto_cancelacion),
                    reserva.politica.refundHours, id = "ref_c_$id")
            }
            val actividad = Negocios.actividadDe(reserva.actividadId)!!
            Notificaciones.evento(usuarioId, R.string.aviso_reserva_cancelada, "reservation", id)
            Notificaciones.evento(Negocios.operadorDe(actividad.negocioId), R.string.aviso_reserva_cancelada)
        }
    }

    fun cambiarTitular(reservaId: String, nuevoUsuarioId: String, nuevoPagoId: String): Reserva {
        val reserva = obtenerReserva(reservaId) ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_la_reserva_ya_no_existe))
        val actualizada = reserva.copy(usuarioId = nuevoUsuarioId, pagoId = nuevoPagoId, llegada = false, fotoLlegada = null)
        _reservas.value = _reservas.value.map { if (it.id == reservaId) actualizada else it }
        _pagos.value = _pagos.value.map { if (it.id == nuevoPagoId) it.copy(reservaId = reservaId) else it }
        Nube.escribir(Escritura.Actualizar("reservas/$reservaId", mapOf(
            "usuarioId" to nuevoUsuarioId, "pagoId" to nuevoPagoId, "llegada" to false,
            "fotoLlegada" to null, "enTransferencia" to false
        )))
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
        Nube.escribir(Escritura.Actualizar("reservas/$reservaId", mapOf("llegada" to true)))
    }

    fun adjuntarFotoLlegada(reservaId: String, usuarioId: String, uri: String) {
        val reserva = obtenerReserva(reservaId)
            ?: throw IllegalArgumentException(Textos.obtener(R.string.reserva_no_encontrada))
        if (reserva.usuarioId != usuarioId) {
            throw IllegalArgumentException(Textos.obtener(R.string.reserva_no_es_tuya))
        }
        FotosLocales.asociar("reserva", reservaId + ":" + usuarioId, uri)
        _reservas.value = _reservas.value.map {
            if (it.id == reservaId) it.copy(llegada = true, fotoLlegada = uri) else it
        }
        Nube.escribir(Escritura.Actualizar("reservas/$reservaId", mapOf("llegada" to true)))

        Nube.ejecutar(R.string.nube_error_foto) {
            val ref = FotosRemotas.subir(uri, reservaId)
            Nube.escribir(Escritura.Actualizar("reservas/$reservaId", mapOf("fotoLlegada" to ref)))
        }
    }

    fun cerrarActividad(reservaId: String, operadorId: String) {
        val reserva = obtenerReserva(reservaId) ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_la_reserva_ya_no_existe))
        Negocios.actividadDe(reserva.actividadId)!!
        if (ahora() < finMillis(reserva.fecha, reserva.hora, reserva.duracionHoras)) throw IllegalStateException(Textos.obtener(R.string.texto_el_horario_todavia_no_ha_finalizado))
        if (reserva.cerrada) throw IllegalStateException(Textos.obtener(R.string.texto_esta_actividad_ya_fue_cerrada))
        Nube.agrupar(Condicion.Igual("reservas/$reservaId", "cerrada", false)) {
            _reservas.value = _reservas.value.map { if (it.id == reservaId) it.copy(cerrada = true) else it }
            Nube.escribir(Escritura.Actualizar("reservas/$reservaId", mapOf("cerrada" to true)))
            Notificaciones.evento(reserva.usuarioId, R.string.aviso_reserva_cerrada, "reservation", reservaId)
        }
    }

    fun enviarAvisoSalida(reservaId: String, operadorId: String) {
        val reserva = obtenerReserva(reservaId) ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_la_reserva_ya_no_existe))
        val actividad = Negocios.actividadDe(reserva.actividadId)!!
        require(Negocios.operadorDe(actividad.negocioId) == operadorId) { Textos.obtener(R.string.fotos_sin_acceso) }
        Notificaciones.evento(reserva.usuarioId, R.string.aviso_salida, "reservation", reservaId)
    }
}
