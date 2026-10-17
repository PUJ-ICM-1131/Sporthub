package com.sportec.sporthub.domain

import com.sportec.sporthub.utils.FotosLocales
import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos

import com.sportec.sporthub.utils.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


object Negocios {

    private fun uid(prefijo: String) = prefijo + java.util.UUID.randomUUID().toString()

    private val _negocios = MutableStateFlow(DatosMock.negocios)
    val negocios: StateFlow<List<Negocio>> = _negocios.asStateFlow()

    private val _actividades = MutableStateFlow(DatosMock.actividades.map { it.copy(fotoUri = FotosLocales.obtenerGaleria(it.id).firstOrNull() ?: it.fotoUri, fotos = FotosLocales.obtenerGaleria(it.id).ifEmpty { it.fotos }) })
    val actividades: StateFlow<List<Actividad>> = _actividades.asStateFlow()

    fun negocioDe(id: String): Negocio? = _negocios.value.firstOrNull { it.id == id }

    fun actividadDe(id: String): Actividad? = _actividades.value.firstOrNull { it.id == id }

    fun actividadesDe(negocioId: String): List<Actividad> = _actividades.value.filter { it.negocioId == negocioId }

    fun operadorDe(negocioId: String): String? = Cuentas.todas().firstOrNull { it.negocioId == negocioId }?.id

    fun crear(nombre: String, nit: String, contacto: String, direccion: String): Negocio {
        if (nombre.isBlank()) throw IllegalArgumentException(Textos.obtener(R.string.texto_el_nombre_del_negocio_es_obligatorio))
        if (direccion.isBlank()) throw IllegalArgumentException(Textos.obtener(R.string.texto_la_direccion_es_obligatoria))
        if (!Validaciones.nit(nit)) throw IllegalArgumentException(Textos.obtener(R.string.error_nit_formato))
        if (!Validaciones.telefono(contacto)) throw IllegalArgumentException(Textos.obtener(R.string.error_telefono_formato))
        val negocio = Negocio(
            id = uid("b"), nombre = nombre.trim(), nit = nit.trim(), contacto = contacto.trim(),
            direccion = direccion.trim(), descripcion = "", estado = EstadoNegocio.PENDIENTE,
            latitud = 4.6486, longitud = -74.0813
        )
        _negocios.value = _negocios.value + negocio
        return negocio
    }

    fun solicitarValidacion(negocioId: String, operadorId: String): Negocio {
        val negocio = negocioDe(negocioId) ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_el_negocio_no_existe))
        val nuevoEstado = if (negocio.nombre.isNotBlank() && negocio.nit.isNotBlank()) EstadoNegocio.VALIDADO else EstadoNegocio.PENDIENTE
        val actualizado = negocio.copy(estado = nuevoEstado)
        _negocios.value = _negocios.value.map { if (it.id == negocioId) actualizado else it }
        return actualizado
    }

    fun validar(negocioId: String, aprobado: Boolean): Negocio {
        val negocio = negocioDe(negocioId) ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_el_negocio_no_existe))
        if (negocio.estado != EstadoNegocio.PENDIENTE) throw IllegalStateException(Textos.obtener(R.string.texto_este_negocio_ya_fue_revisado))
        val actualizado = negocio.copy(estado = if (aprobado) EstadoNegocio.VALIDADO else EstadoNegocio.RECHAZADO)
        _negocios.value = _negocios.value.map { if (it.id == negocioId) actualizado else it }
        return actualizado
    }

    fun actualizarFicha(negocioId: String, operadorId: String, nombre: String, nit: String, descripcion: String, contacto: String, direccion: String): Negocio {
        val negocio = negocioDe(negocioId) ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_el_negocio_no_existe))
        if (nombre.isBlank()) throw IllegalArgumentException(Textos.obtener(R.string.texto_el_nombre_del_negocio_es_obligatorio))
        if (!Validaciones.nit(nit)) throw IllegalArgumentException(Textos.obtener(R.string.error_nit_formato))
        if (direccion.isBlank()) throw IllegalArgumentException(Textos.obtener(R.string.texto_la_direccion_no_puede_estar_vacia))
        if (!Validaciones.telefono(contacto)) throw IllegalArgumentException(Textos.obtener(R.string.error_telefono_formato))
        val cambiaSensible = nombre.trim() != negocio.nombre || nit.trim() != negocio.nit
        val actualizado = negocio.copy(
            descripcion = descripcion.trim(),
            contacto = contacto.trim(),
            direccion = direccion.trim(),
            cambioPendiente = if (cambiaSensible) CambioSensible(nombre.trim(), nit.trim()) else negocio.cambioPendiente
        )
        _negocios.value = _negocios.value.map { if (it.id == negocioId) actualizado else it }
        return actualizado
    }

    fun listarCambiosPendientes(): List<Negocio> = _negocios.value.filter { it.cambioPendiente != null }

    fun resolverCambioSensible(negocioId: String, aprobado: Boolean): Negocio {
        val negocio = negocioDe(negocioId) ?: throw IllegalArgumentException(Textos.obtener(R.string.texto_el_negocio_no_existe))
        val cambio = negocio.cambioPendiente ?: throw IllegalStateException(Textos.obtener(R.string.cambio_sin_solicitud))
        val actualizado = if (aprobado) {
            negocio.copy(nombre = cambio.nombre, nit = cambio.nit, cambioPendiente = null)
        } else {
            negocio.copy(cambioPendiente = null)
        }
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
        descripcion: String,
        horaApertura: Int,
        horaCierre: Int,
        politica: Politica,
        fotoUri: String? = null,
        fotos: List<String>? = null
    ): Actividad {
        if (negocioDe(negocioId)?.estado != EstadoNegocio.VALIDADO) throw IllegalStateException(Textos.obtener(R.string.texto_el_negocio_debe_estar_validado_para_publicar_servicios))
        if (nombre.isBlank()) throw IllegalArgumentException(Textos.obtener(R.string.texto_el_nombre_es_obligatorio))
        if (precio <= 0) throw IllegalArgumentException(Textos.obtener(R.string.texto_el_precio_debe_ser_mayor_a_cero))
        if (duracionMinutos <= 0) throw IllegalArgumentException(Textos.obtener(R.string.texto_la_duracion_debe_ser_mayor_a_cero))
        if (capacidad <= 0) throw IllegalArgumentException(Textos.obtener(R.string.texto_la_capacidad_debe_ser_mayor_a_cero))
        if (horaApertura !in 0..23 || horaCierre !in 1..24 || horaApertura >= horaCierre) {
            throw IllegalArgumentException(Textos.obtener(R.string.error_horario_servicio))
        }
        if (politica.payMinutes <= 0) throw IllegalArgumentException(Textos.obtener(R.string.texto_el_plazo_de_pago_debe_ser_mayor_a_cero))
        if (politica.cancelHours < 0) throw IllegalArgumentException(Textos.obtener(R.string.texto_la_anticipacion_de_cancelacion_no_puede_ser_negativa))
        if (politica.refundPercent !in 0..100) throw IllegalArgumentException(Textos.obtener(R.string.texto_el_porcentaje_de_devolucion_debe_estar_entre_0_y))
        if (politica.refundHours <= 0) throw IllegalArgumentException(Textos.obtener(R.string.texto_el_plazo_de_devolucion_debe_ser_mayor_a_cero))

        val existente = id?.let { actividadDe(it) }
        require(Cuentas.porId(operadorId)?.negocioId == negocioId && (existente == null || existente.negocioId == negocioId)) { Textos.obtener(R.string.fotos_sin_acceso) }
        val galeria = (fotos ?: listOfNotNull(fotoUri ?: existente?.fotoUri)).distinct()
        require(galeria.size <= 8) { Textos.obtener(R.string.fotos_limite) }
        if (existente != null) {
            val cambiaCompromiso = existente.duracionMinutos != duracionMinutos || existente.capacidad != capacidad || existente.tipo != tipo
            if (cambiaCompromiso && Reservas.tieneCompromisos(existente.id)) {
                throw IllegalStateException(Textos.obtener(R.string.texto_no_puedes_cambiar_duracion_modalidad_o_capacidad_con_compromisos))
            }
            val actualizada = existente.copy(
                nombre = nombre, categoria = categoria, tipo = tipo, precio = precio,
                duracionMinutos = duracionMinutos, capacidad = capacidad, descripcion = descripcion,
                horaApertura = horaApertura, horaCierre = horaCierre, politica = politica,
                fotoUri = galeria.firstOrNull(), fotos = galeria
            )
            FotosLocales.guardarGaleria(actualizada.id, galeria)
            _actividades.value = _actividades.value.map { if (it.id == existente.id) actualizada else it }
            return actualizada
        }
        val nueva = Actividad(
            id = uid("a"), negocioId = negocioId, nombre = nombre, categoria = categoria, tipo = tipo,
            precio = precio, duracionMinutos = duracionMinutos, capacidad = capacidad, descripcion = descripcion,
            horaApertura = horaApertura, horaCierre = horaCierre, politica = politica,
            fotoUri = galeria.firstOrNull(), fotos = galeria
        )
        FotosLocales.guardarGaleria(nueva.id, galeria)
        _actividades.value = _actividades.value + nueva
        return nueva
    }

}
