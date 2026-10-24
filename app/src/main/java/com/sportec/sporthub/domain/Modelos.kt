package com.sportec.sporthub.domain

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos

enum class Rol(val etiqueta: String) {
    DEPORTISTA(Textos.obtener(R.string.texto_deportista)),
    ESTABLECIMIENTO(Textos.obtener(R.string.texto_establecimiento)),
    ADMINISTRADOR(Textos.obtener(R.string.texto_administrador))
}

data class Cuenta(
    val id: String,
    val nombre: String,
    val email: String,
    val rol: Rol,
    val telefono: String? = null,
    val negocioId: String? = null,
    val activa: Boolean = true,
    val fotoUri: String? = null,
    val uid: String = ""
)

enum class EstadoNegocio(val etiqueta: String) {
    VALIDADO(Textos.obtener(R.string.texto_validado)),
    PENDIENTE(Textos.obtener(R.string.texto_pendiente)),
    RECHAZADO(Textos.obtener(R.string.texto_rechazado))
}

data class CambioSensible(
    val nombre: String,
    val nit: String
)

data class Negocio(
    val id: String,
    val nombre: String,
    val nit: String,
    val contacto: String,
    val direccion: String,
    val descripcion: String,
    val estado: EstadoNegocio,
    val latitud: Double,
    val longitud: Double,
    val cambioPendiente: CambioSensible? = null
)

enum class Segmento(val etiqueta: String) {
    DEPORTE(Textos.obtener(R.string.texto_deporte)),
    BIENESTAR(Textos.obtener(R.string.texto_bienestar))
}

data class Categoria(
    val nombre: String,
    val segmento: Segmento
)

enum class TipoActividad(val etiqueta: String) {
    ESPACIO(Textos.obtener(R.string.texto_espacio_completo)),
    CLASE(Textos.obtener(R.string.texto_clase_grupal))
}

data class Politica(
    val payMinutes: Int = 10,
    val cancelHours: Int = 2,
    val refundPercent: Int = 80,
    val refundHours: Int = 48,
    val transferable: Boolean = true,
    val extra: String = ""
)

data class Actividad(
    val id: String,
    val negocioId: String,
    val nombre: String,
    val categoria: String,
    val tipo: TipoActividad,
    val precio: Int,
    val duracionMinutos: Int,
    val capacidad: Int,
    val descripcion: String,
    val politica: Politica = Politica(),
    val horaApertura: Int = 6,
    val horaCierre: Int = 21,
    val fotoUri: String? = null,
    val fotos: List<String> = listOfNotNull(fotoUri)
)

data class ActividadConNegocio(
    val actividad: Actividad,
    val negocio: Negocio
)

enum class EstadoSolicitud(val etiqueta: String) {
    FILA(Textos.obtener(R.string.texto_en_fila)),
    REVISION(Textos.obtener(R.string.texto_en_revision)),
    PAGO(Textos.obtener(R.string.texto_pago_pendiente)),
    ATENDIDA(Textos.obtener(R.string.texto_atendida)),
    VENCIDA(Textos.obtener(R.string.texto_vencida)),
    INCOMPATIBLE(Textos.obtener(R.string.texto_incompatible)),
    RECHAZADA(Textos.obtener(R.string.texto_rechazada)),
    RETIRADA(Textos.obtener(R.string.texto_retirada))
}

data class Solicitud(
    val id: String,
    val usuarioId: String,
    val actividadId: String,
    val fecha: String,
    val hora: Int,
    val duracionHoras: Int,
    val orden: Int,
    val estado: EstadoSolicitud,
    val precio: Int,
    val politica: Politica,
    val deadline: Long? = null
)

enum class EstadoReserva(val etiqueta: String) {
    CONFIRMADA(Textos.obtener(R.string.texto_confirmada)),
    CANCELADA(Textos.obtener(R.string.texto_cancelada))
}

data class Reserva(
    val id: String,
    val usuarioId: String,
    val actividadId: String,
    val fecha: String,
    val hora: Int,
    val duracionHoras: Int,
    val precio: Int,
    val estado: EstadoReserva,
    val pagoId: String?,
    val cerrada: Boolean = false,
    val politica: Politica,
    val llegada: Boolean = false,
    val fotoLlegada: String? = null
)

enum class EstadoPago { EXITOSO, FALLIDO }

data class Pago(
    val id: String,
    val usuarioId: String,
    val monto: Int,
    val estado: EstadoPago,
    val medio: String,
    val reservaId: String? = null,
    val transferenciaId: String? = null
)

enum class EstadoReembolso(val etiqueta: String) {
    PENDIENTE(Textos.obtener(R.string.texto_pendiente)),
    REALIZADO(Textos.obtener(R.string.texto_realizada)),
    FALLIDO(Textos.obtener(R.string.texto_fallido))
}

data class Reembolso(
    val id: String,
    val reservaId: String,
    val usuarioId: String,
    val pagoId: String,
    val monto: Int,
    val causa: String,
    val plazo: Long,
    val estado: EstadoReembolso = EstadoReembolso.PENDIENTE
)

enum class EstadoPostulante(val etiqueta: String) {
    FILA(Textos.obtener(R.string.texto_en_fila)),
    REVISION(Textos.obtener(R.string.texto_en_revision)),
    PAGO(Textos.obtener(R.string.texto_pago_pendiente)),
    ATENDIDA(Textos.obtener(R.string.texto_atendida)),
    VENCIDA(Textos.obtener(R.string.texto_vencida)),
    INCOMPATIBLE(Textos.obtener(R.string.texto_incompatible)),
    RETIRADA(Textos.obtener(R.string.texto_retirada))
}

data class Postulante(
    val id: String,
    val usuarioId: String,
    val estado: EstadoPostulante,
    val deadline: Long? = null,
    val aceptadoPorPropietario: Boolean = false
)

enum class EstadoTransferencia(val etiqueta: String) {
    PUBLICADA(Textos.obtener(R.string.texto_publicada)),
    TRAMITE(Textos.obtener(R.string.texto_en_tramite)),
    COMPLETADA(Textos.obtener(R.string.texto_completada)),
    RETIRADA(Textos.obtener(R.string.texto_retirada)),
    VENCIDA(Textos.obtener(R.string.texto_vencida))
}

data class Transferencia(
    val id: String,
    val reservaId: String,
    val propietarioId: String,
    val pagoId: String,
    val estado: EstadoTransferencia,
    val postulantes: List<Postulante> = emptyList()
)

data class Comunidad(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val creadorId: String,
    val adminId: String,
    val miembros: List<String>,
    val fotoUri: String? = null
)

data class Mensaje(
    val id: String,
    val autorId: String,
    val texto: String
)

data class Chat(
    val id: String,
    val miembros: List<String> = emptyList(),
    val comunidadId: String? = null,
    val mensajes: List<Mensaje>
)

data class Resena(
    val id: String,
    val reservaId: String,
    val usuarioId: String,
    val puntaje: Int,
    val comentario: String,
    val respuesta: String? = null
)

data class Notificacion(
    val id: String,
    val usuarioId: String,
    val titulo: String,
    val texto: String,
    val ruta: String?,
    val argumento: String?,
    val leida: Boolean = false
)

enum class EstadoReporte { PENDIENTE, RESUELTO }

data class Reporte(
    val id: String,
    val usuarioId: String,
    val objetivo: String,
    val contexto: String,
    val motivo: String,
    val autorId: String,
    val objetivoId: String,
    val tipoObjetivo: String,
    val estado: EstadoReporte = EstadoReporte.PENDIENTE
)
