package com.sportec.sporthub.domain

enum class Rol(val etiqueta: String) {
    DEPORTISTA("Deportista"),
    ESTABLECIMIENTO("Establecimiento"),
    ADMINISTRADOR("Administrador")
}

data class Cuenta(
    val id: String,
    val nombre: String,
    val email: String,
    val contrasena: String,
    val rol: Rol,
    val telefono: String? = null,
    val negocioId: String? = null,
    val activa: Boolean = true
)

enum class EstadoNegocio(val etiqueta: String) {
    VALIDADO("Validado"),
    PENDIENTE("Pendiente"),
    RECHAZADO("Rechazado")
}

data class Negocio(
    val id: String,
    val nombre: String,
    val nit: String,
    val contacto: String,
    val direccion: String,
    val descripcion: String,
    val estado: EstadoNegocio,
    val latitud: Double,
    val longitud: Double
)

enum class Segmento(val etiqueta: String) {
    DEPORTE("Deporte"),
    BIENESTAR("Bienestar")
}

data class Categoria(
    val nombre: String,
    val segmento: Segmento
)

enum class TipoActividad(val etiqueta: String) {
    ESPACIO("Espacio completo"),
    CLASE("Clase grupal")
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
    val politica: Politica = Politica()
)

data class ActividadConNegocio(
    val actividad: Actividad,
    val negocio: Negocio
)

enum class EstadoSolicitud(val etiqueta: String) {
    FILA("En fila"),
    REVISION("En revisión"),
    PAGO("Pago pendiente"),
    ATENDIDA("Atendida"),
    VENCIDA("Vencida"),
    INCOMPATIBLE("Incompatible"),
    RECHAZADA("Rechazada"),
    RETIRADA("Retirada")
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
    CONFIRMADA("Confirmada"),
    CANCELADA("Cancelada")
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
    val politica: Politica
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

enum class EstadoReembolso { PENDIENTE, REALIZADO, FALLIDO }

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

enum class EstadoPostulante { FILA, REVISION, PAGO, ATENDIDA, VENCIDA, INCOMPATIBLE, RETIRADA }

data class Postulante(
    val id: String,
    val usuarioId: String,
    val estado: EstadoPostulante,
    val deadline: Long? = null
)

enum class EstadoTransferencia(val etiqueta: String) {
    PUBLICADA("Publicada"),
    TRAMITE("En trámite"),
    COMPLETADA("Completada"),
    RETIRADA("Retirada"),
    VENCIDA("Vencida")
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
    val miembros: List<String>
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
