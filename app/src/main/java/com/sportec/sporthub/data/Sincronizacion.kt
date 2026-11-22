package com.sportec.sporthub.data

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.sportec.sporthub.domain.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

object Sincronizacion {
    private const val ETIQUETA = "Sincronizacion"

    private var padre: CoroutineScope? = null
    private var cuenta: Cuenta? = null
    private var trabajo: Job? = null
    private var trabajoFotos: Job? = null

    fun iniciar(scope: CoroutineScope, actual: Cuenta) {
        padre = scope
        cuenta = actual
        trabajoFotos?.cancel()
        trabajoFotos = scope.launch {
            FotosRemotas.version.drop(1).collectLatest {
                delay(400)
                arrancar()
            }
        }
        arrancar()
    }

    fun reiniciar() {
        if (cuenta != null) arrancar()
    }

    fun detener() {
        trabajo?.cancel()
        trabajoFotos?.cancel()
        trabajo = null
        trabajoFotos = null
        cuenta = null
        padre = null
        Negocios.vaciar(); Reservas.vaciar(); Transferencias.vaciar(); Mensajes.vaciar()
        Comunidades.vaciar(); Notificaciones.vaciar(); Resenas.vaciar(); Reportes.vaciar()
    }

    private class Union<T>(private val clave: (T) -> String, private val salida: (List<T>) -> Unit) {
        private val partes = LinkedHashMap<String, List<T>>()
        fun poner(fuente: String, lista: List<T>) {
            partes[fuente] = lista
            salida(partes.values.flatten().distinctBy(clave))
        }
    }

    private fun <T> escuchar(
        ambito: CoroutineScope, consulta: Query, nombre: String,
        convertir: (DocumentSnapshot) -> T?, destino: (List<T>) -> Unit
    ) {
        consulta.snapshots()
            .map { s -> s.documents.mapNotNull(convertir) }
            .onEach(destino)
            .catch { Log.w(ETIQUETA, "Falló la escucha de $nombre", it) }
            .launchIn(ambito)
    }

    private fun arrancar() {
        val scope = padre ?: return
        val c = cuenta ?: return
        trabajo?.cancel()
        val job = SupervisorJob(scope.coroutineContext[Job])
        trabajo = job
        escuchas(CoroutineScope(scope.coroutineContext + job), c)
    }

    private fun escuchas(ambito: CoroutineScope, c: Cuenta) {
        val db = Nube.db
        val uid = c.uid.ifBlank { c.id }

        escuchar(ambito, db.collection("negocios"), "negocios", Mapeos::negocio, Negocios::cargarNegocios)
        escuchar(ambito, db.collection("actividades"), "actividades", Mapeos::actividad, Negocios::cargarActividades)
        escuchar(ambito, db.collection("ocupacion"), "ocupacion", { it }) { docs ->
            Reservas.cargarOcupacion(docs.associate { it.id to (it.getLong("n") ?: 0L).toInt() })
        }
        escuchar(ambito, db.collection("resenas"), "resenas", Mapeos::resena, Resenas::cargar)

        escuchar(ambito, db.collection("notificaciones").whereEqualTo("usuarioId", uid), "notificaciones",
            Mapeos::notificacion, Notificaciones::cargar)

        escucharComunidades(ambito, uid)
        escucharChats(ambito, uid)

        when (c.rol) {
            Rol.DEPORTISTA -> escucharDeportista(ambito, uid)
            Rol.ESTABLECIMIENTO -> escucharEstablecimiento(ambito, c.negocioId)
            Rol.ADMINISTRADOR -> escuchar(ambito, db.collection("reportes"), "reportes", Mapeos::reporte, Reportes::cargar)
        }
    }

    private fun escucharDeportista(ambito: CoroutineScope, uid: String) {
        val db = Nube.db
        escuchar(ambito, db.collection("solicitudes").whereEqualTo("usuarioId", uid), "solicitudes",
            Mapeos::solicitud, Reservas::cargarSolicitudes)

        val reservas = Union<Reserva>({ it.id }, Reservas::cargarReservas)
        escuchar(ambito, db.collection("reservas").whereEqualTo("usuarioId", uid), "reservas propias",
            Mapeos::reserva) { reservas.poner("propias", it) }
        escuchar(ambito, db.collection("reservas").whereEqualTo("enTransferencia", true), "reservas en transferencia",
            Mapeos::reserva) { reservas.poner("transferencias", it) }

        escuchar(ambito, db.collection("pagos").whereEqualTo("usuarioId", uid), "pagos", Mapeos::pago, Reservas::cargarPagos)
        escuchar(ambito, db.collection("reembolsos").whereEqualTo("usuarioId", uid), "reembolsos",
            Mapeos::reembolso, Reservas::cargarReembolsos)

        val transferencias = Union<Mapeos.TransferenciaLeida>({ it.transferencia.id }, Transferencias::cargar)
        escuchar(ambito, db.collection("transferencias").whereIn("estado", listOf("PUBLICADA", "TRAMITE")),
            "transferencias vigentes", Mapeos::transferencia) { transferencias.poner("vigentes", it) }
        escuchar(ambito, db.collection("transferencias").whereEqualTo("propietarioId", uid),
            "transferencias propias", Mapeos::transferencia) { transferencias.poner("propias", it) }
        escuchar(ambito, db.collection("transferencias").whereArrayContains("postulantesUids", uid),
            "transferencias en las que participo", Mapeos::transferencia) { transferencias.poner("postulante", it) }
    }

    private fun escucharEstablecimiento(ambito: CoroutineScope, negocioId: String?) {
        if (negocioId.isNullOrBlank()) return
        val db = Nube.db
        fun consulta(coleccion: String) = db.collection(coleccion).whereEqualTo("negocioId", negocioId)
        escuchar(ambito, consulta("solicitudes"), "solicitudes del negocio", Mapeos::solicitud, Reservas::cargarSolicitudes)
        escuchar(ambito, consulta("reservas"), "reservas del negocio", Mapeos::reserva, Reservas::cargarReservas)
        escuchar(ambito, consulta("pagos"), "pagos del negocio", Mapeos::pago, Reservas::cargarPagos)
        escuchar(ambito, consulta("reembolsos"), "reembolsos del negocio", Mapeos::reembolso, Reservas::cargarReembolsos)
        escuchar(ambito, consulta("transferencias"), "transferencias del negocio", Mapeos::transferencia, Transferencias::cargar)
    }

    private fun escucharComunidades(ambito: CoroutineScope, uid: String) {
        val db = Nube.db
        val comunidades = Union<Comunidad>({ it.id }, Comunidades::cargarComunidades)
        escuchar(ambito, db.collection("comunidades").whereArrayContains("miembros", uid), "comunidades",
            Mapeos::comunidad) { comunidades.poner("miembro", it) }

        val invitaciones = Union<InvitacionComunidad>({ it.id }, Comunidades::cargarInvitaciones)
        val comunidadesInvitadas = mutableMapOf<String, Job>()
        db.collection("invitaciones").whereEqualTo("invitadoId", uid).snapshots()
            .map { s -> s.documents.mapNotNull(Mapeos::invitacion) }
            .onEach { recibidas ->
                invitaciones.poner("recibidas", recibidas)

                val pendientes = recibidas.filter { it.estado == EstadoInvitacion.PENDIENTE }.map { it.comunidadId }.toSet()
                (comunidadesInvitadas.keys - pendientes).forEach { comunidadesInvitadas.remove(it)?.cancel() }
                (pendientes - comunidadesInvitadas.keys).forEach { id ->
                    comunidadesInvitadas[id] = db.collection("comunidades").document(id).snapshots()
                        .onEach { doc ->
                            val lista = listOfNotNull(Mapeos.comunidad(doc).takeIf { doc.exists() })
                            comunidades.poner("invitada:$id", lista)
                        }
                        .catch { Log.w(ETIQUETA, "No se pudo leer la comunidad $id", it) }
                        .launchIn(ambito)
                }
            }
            .catch { Log.w(ETIQUETA, "Falló la escucha de invitaciones", it) }
            .launchIn(ambito)
        escuchar(ambito, db.collection("invitaciones").whereEqualTo("remitenteId", uid), "invitaciones enviadas",
            Mapeos::invitacion) { invitaciones.poner("enviadas", it) }
    }

    private fun escucharChats(ambito: CoroutineScope, uid: String) {
        val db = Nube.db
        val mensajesPorChat = mutableMapOf<String, Job>()
        db.collection("chats").whereArrayContains("miembros", uid).snapshots()
            .map { s -> s.documents.map(Mapeos::chat) }
            .onEach { chats ->
                Mensajes.cargarChats(chats)
                val ids = chats.map { it.id }.toSet()
                (mensajesPorChat.keys - ids).forEach { mensajesPorChat.remove(it)?.cancel() }
                (ids - mensajesPorChat.keys).forEach { id ->
                    mensajesPorChat[id] = db.collection("chats").document(id).collection("mensajes")
                        .orderBy("creadoEn").snapshots()
                        .map { s -> s.documents.mapNotNull(Mapeos::mensaje) }
                        .onEach { Mensajes.cargarMensajes(id, it) }
                        .catch { Log.w(ETIQUETA, "Falló la escucha de mensajes de $id", it) }
                        .launchIn(ambito)
                }
            }
            .catch { Log.w(ETIQUETA, "Falló la escucha de chats", it) }
            .launchIn(ambito)
    }
}
