package com.sportec.sporthub.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

interface RutaAcceso : NavKey

@Serializable data object Bienvenida : RutaAcceso
@Serializable data object Login : RutaAcceso
@Serializable data object Registro : RutaAcceso
@Serializable data object RegistroDeportista : RutaAcceso
@Serializable data object RegistroEstablecimiento : RutaAcceso
@Serializable data object Recuperar : RutaAcceso
@Serializable data object EnlaceRecuperacion : RutaAcceso
@Serializable data object NuevaContrasena : RutaAcceso

@Serializable data object Explorar : NavKey
@Serializable data object MisReservas : NavKey
@Serializable data object Mensajes : NavKey
@Serializable data object Perfil : NavKey
@Serializable data object Gestion : NavKey
@Serializable data object Servicios : NavKey
@Serializable data object Pagos : NavKey
@Serializable data object Validaciones : NavKey
@Serializable data object Reportes : NavKey

@Serializable data object Buscar : NavKey
@Serializable data object Filtros : NavKey
@Serializable data object Mapa : NavKey
@Serializable data object Origen : NavKey
@Serializable data class DetalleActividad(val id: String) : NavKey
@Serializable data class DetalleEstablecimiento(val id: String) : NavKey
@Serializable data class Horarios(val actividadId: String) : NavKey

@Serializable data class ResumenSolicitud(val actividadId: String, val fecha: String, val hora: Int) : NavKey
@Serializable data class DetalleSolicitud(val id: String) : NavKey
@Serializable data class Pago(val referencia: String) : NavKey
@Serializable data class PagoExitoso(val referencia: String) : NavKey
@Serializable data class DetalleReserva(val id: String) : NavKey
@Serializable data class CancelarReserva(val id: String) : NavKey
@Serializable data object Reembolsos : NavKey
@Serializable data class DetalleReembolso(val id: String) : NavKey

@Serializable data class PublicarTransferencia(val reservaId: String) : NavKey
@Serializable data object Transferencias : NavKey
@Serializable data class DetalleTransferencia(val id: String) : NavKey
@Serializable data object MisTransferencias : NavKey

@Serializable data class Chat(val id: String) : NavKey
@Serializable data object NuevoChat : NavKey
@Serializable data object CrearComunidad : NavKey
@Serializable data class Comunidad(val id: String) : NavKey
@Serializable data class EditarComunidad(val id: String) : NavKey
@Serializable data class Invitar(val comunidadId: String) : NavKey
@Serializable data class Sucesion(val comunidadId: String) : NavKey
@Serializable data object Contactos : NavKey

@Serializable data class Permiso(val tipo: String) : NavKey
@Serializable data class Ruta(val negocioId: String) : NavKey
@Serializable data class Brujula(val negocioId: String) : NavKey
@Serializable data class CheckIn(val reservaId: String) : NavKey
@Serializable data class FotoLlegada(val reservaId: String) : NavKey
@Serializable data class Resena(val reservaId: String) : NavKey
@Serializable data object Notificaciones : NavKey
@Serializable data class Reportar(val objetivo: String) : NavKey
@Serializable data object EditarPerfil : NavKey

@Serializable data object ValidacionNegocio : NavKey
@Serializable data object EditarNegocio : NavKey
@Serializable data class EditarServicio(val id: String) : NavKey
@Serializable data class HorariosServicio(val actividadId: String) : NavKey
@Serializable data class Condiciones(val actividadId: String) : NavKey
@Serializable data class MotivoRechazo(val solicitudId: String) : NavKey
@Serializable data object Agenda : NavKey
@Serializable data object ResenasNegocio : NavKey
@Serializable data class ResponderResena(val id: String) : NavKey
@Serializable data class MotivoTransferencia(val transferenciaId: String) : NavKey

@Serializable data class DetalleValidacion(val negocioId: String) : NavKey
@Serializable data class DetalleReporte(val id: String) : NavKey
