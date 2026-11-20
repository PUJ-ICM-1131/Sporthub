package com.sportec.sporthub.data

import com.google.firebase.firestore.DocumentSnapshot
import com.sportec.sporthub.domain.*

/** Conversión explícita entre los modelos del dominio y los documentos de Firestore. */
object Mapeos {

    private fun DocumentSnapshot.texto(c: String): String? = getString(c)
    private fun DocumentSnapshot.entero(c: String): Int = getLong(c)?.toInt() ?: 0
    private fun DocumentSnapshot.largo(c: String): Long? = getLong(c)
    private inline fun <reified E : Enum<E>> DocumentSnapshot.enumerado(c: String): E? =
        getString(c)?.let { n -> enumValues<E>().firstOrNull { it.name == n } }

    @Suppress("UNCHECKED_CAST")
    private fun DocumentSnapshot.lista(c: String): List<String> =
        (get(c) as? List<*>)?.filterIsInstance<String>() ?: emptyList()

    // ---------- Política ----------
    private fun politica(m: Map<*, *>?): Politica {
        if (m == null) return Politica()
        return Politica(
            payMinutes = (m["payMinutes"] as? Number)?.toInt() ?: 10,
            cancelHours = (m["cancelHours"] as? Number)?.toInt() ?: 2,
            refundPercent = (m["refundPercent"] as? Number)?.toInt() ?: 80,
            refundHours = (m["refundHours"] as? Number)?.toInt() ?: 48,
            transferable = m["transferable"] as? Boolean ?: true,
            extra = m["extra"] as? String ?: ""
        )
    }

    private fun mapa(p: Politica): Map<String, Any?> = mapOf(
        "payMinutes" to p.payMinutes, "cancelHours" to p.cancelHours, "refundPercent" to p.refundPercent,
        "refundHours" to p.refundHours, "transferable" to p.transferable, "extra" to p.extra
    )

    // ---------- Negocios y actividades ----------
    fun negocio(d: DocumentSnapshot): Negocio? {
        val estado = d.enumerado<EstadoNegocio>("estado") ?: return null
        val cambio = (d.get("cambioPendiente") as? Map<*, *>)?.let {
            CambioSensible(it["nombre"] as? String ?: "", it["nit"] as? String ?: "")
        }
        return Negocio(
            id = d.id, nombre = d.texto("nombre").orEmpty(), nit = d.texto("nit").orEmpty(),
            contacto = d.texto("contacto").orEmpty(), direccion = d.texto("direccion").orEmpty(),
            descripcion = d.texto("descripcion").orEmpty(), estado = estado,
            latitud = d.getDouble("latitud") ?: 0.0, longitud = d.getDouble("longitud") ?: 0.0,
            cambioPendiente = cambio
        )
    }

    fun mapa(n: Negocio): Map<String, Any?> = mapOf(
        "nombre" to n.nombre, "nit" to n.nit, "contacto" to n.contacto, "direccion" to n.direccion,
        "descripcion" to n.descripcion, "estado" to n.estado.name,
        "latitud" to n.latitud, "longitud" to n.longitud,
        "cambioPendiente" to n.cambioPendiente?.let { mapOf("nombre" to it.nombre, "nit" to it.nit) }
    )

    fun actividad(d: DocumentSnapshot): Actividad? {
        val tipo = d.enumerado<TipoActividad>("tipo") ?: return null
        val fotos = d.lista("fotos").mapNotNull { FotosRemotas.resolver(it) }
        return Actividad(
            id = d.id, negocioId = d.texto("negocioId").orEmpty(), nombre = d.texto("nombre").orEmpty(),
            categoria = d.texto("categoria").orEmpty(), tipo = tipo, precio = d.entero("precio"),
            duracionMinutos = d.entero("duracionMinutos"), capacidad = d.entero("capacidad"),
            descripcion = d.texto("descripcion").orEmpty(), politica = politica(d.get("politica") as? Map<*, *>),
            horaApertura = d.entero("horaApertura"), horaCierre = d.entero("horaCierre"),
            fotoUri = fotos.firstOrNull(), fotos = fotos
        )
    }

    /** [refsFotos] son referencias remotas ya subidas (ver [FotosRemotas]). */
    fun mapa(a: Actividad, refsFotos: List<String>): Map<String, Any?> = mapOf(
        "negocioId" to a.negocioId, "nombre" to a.nombre, "categoria" to a.categoria, "tipo" to a.tipo.name,
        "precio" to a.precio, "duracionMinutos" to a.duracionMinutos, "capacidad" to a.capacidad,
        "descripcion" to a.descripcion, "politica" to mapa(a.politica),
        "horaApertura" to a.horaApertura, "horaCierre" to a.horaCierre, "fotos" to refsFotos
    )

    // ---------- Solicitudes y reservas ----------
    fun solicitud(d: DocumentSnapshot): Solicitud? {
        val estado = d.enumerado<EstadoSolicitud>("estado") ?: return null
        return Solicitud(
            id = d.id, usuarioId = d.texto("usuarioId").orEmpty(), actividadId = d.texto("actividadId").orEmpty(),
            fecha = d.texto("fecha").orEmpty(), hora = d.entero("hora"), duracionHoras = d.entero("duracionHoras"),
            orden = d.entero("orden"), estado = estado, precio = d.entero("precio"),
            politica = politica(d.get("politica") as? Map<*, *>), deadline = d.largo("deadline")
        )
    }

    fun mapa(s: Solicitud, negocioId: String): Map<String, Any?> = mapOf(
        "usuarioId" to s.usuarioId, "actividadId" to s.actividadId, "negocioId" to negocioId,
        "fecha" to s.fecha, "hora" to s.hora, "duracionHoras" to s.duracionHoras, "orden" to s.orden,
        "estado" to s.estado.name, "precio" to s.precio, "politica" to mapa(s.politica), "deadline" to s.deadline,
        "creadaEn" to Nube.ahora()
    )

    fun reserva(d: DocumentSnapshot): Reserva? {
        val estado = d.enumerado<EstadoReserva>("estado") ?: return null
        return Reserva(
            id = d.id, usuarioId = d.texto("usuarioId").orEmpty(), actividadId = d.texto("actividadId").orEmpty(),
            fecha = d.texto("fecha").orEmpty(), hora = d.entero("hora"), duracionHoras = d.entero("duracionHoras"),
            precio = d.entero("precio"), estado = estado, pagoId = d.texto("pagoId"),
            cerrada = d.getBoolean("cerrada") ?: false, politica = politica(d.get("politica") as? Map<*, *>),
            llegada = d.getBoolean("llegada") ?: false,
            fotoLlegada = d.texto("fotoLlegada")?.let { FotosRemotas.resolver(it) }
        )
    }

    fun mapa(r: Reserva, negocioId: String, enTransferencia: Boolean = false): Map<String, Any?> = mapOf(
        "usuarioId" to r.usuarioId, "actividadId" to r.actividadId, "negocioId" to negocioId,
        "fecha" to r.fecha, "hora" to r.hora, "duracionHoras" to r.duracionHoras, "precio" to r.precio,
        "estado" to r.estado.name, "pagoId" to r.pagoId, "cerrada" to r.cerrada,
        "politica" to mapa(r.politica), "llegada" to r.llegada, "fotoLlegada" to null,
        "enTransferencia" to enTransferencia, "creadaEn" to Nube.ahora()
    )

    // ---------- Pagos y reembolsos ----------
    fun pago(d: DocumentSnapshot): Pago? {
        val estado = d.enumerado<EstadoPago>("estado") ?: return null
        return Pago(
            id = d.id, usuarioId = d.texto("usuarioId").orEmpty(), monto = d.entero("monto"), estado = estado,
            medio = d.texto("medio").orEmpty(), reservaId = d.texto("reservaId"), transferenciaId = d.texto("transferenciaId")
        )
    }

    fun mapa(p: Pago, negocioId: String): Map<String, Any?> = mapOf(
        "usuarioId" to p.usuarioId, "monto" to p.monto, "estado" to p.estado.name, "medio" to p.medio,
        "reservaId" to p.reservaId, "transferenciaId" to p.transferenciaId, "negocioId" to negocioId,
        "creadoEn" to Nube.ahora()
    )

    fun reembolso(d: DocumentSnapshot): Reembolso? {
        val estado = d.enumerado<EstadoReembolso>("estado") ?: return null
        return Reembolso(
            id = d.id, reservaId = d.texto("reservaId").orEmpty(), usuarioId = d.texto("usuarioId").orEmpty(),
            pagoId = d.texto("pagoId").orEmpty(), monto = d.entero("monto"), causa = d.texto("causa").orEmpty(),
            plazo = d.largo("plazo") ?: 0L, estado = estado
        )
    }

    fun mapa(r: Reembolso, negocioId: String): Map<String, Any?> = mapOf(
        "reservaId" to r.reservaId, "usuarioId" to r.usuarioId, "pagoId" to r.pagoId, "monto" to r.monto,
        "causa" to r.causa, "plazo" to r.plazo, "estado" to r.estado.name, "negocioId" to negocioId,
        "creadoEn" to Nube.ahora()
    )

    // ---------- Transferencias ----------
    class TransferenciaLeida(val transferencia: Transferencia, val version: Long)

    fun transferencia(d: DocumentSnapshot): TransferenciaLeida? {
        val estado = d.enumerado<EstadoTransferencia>("estado") ?: return null
        val postulantes = (d.get("postulantes") as? List<*>).orEmpty().mapNotNull { item ->
            val m = item as? Map<*, *> ?: return@mapNotNull null
            val est = (m["estado"] as? String)?.let { n -> EstadoPostulante.values().firstOrNull { it.name == n } }
                ?: return@mapNotNull null
            Postulante(
                id = m["id"] as? String ?: return@mapNotNull null,
                usuarioId = m["usuarioId"] as? String ?: return@mapNotNull null,
                estado = est, deadline = (m["deadline"] as? Number)?.toLong(),
                aceptadoPorPropietario = m["aceptadoPorPropietario"] as? Boolean ?: false
            )
        }
        val t = Transferencia(
            id = d.id, reservaId = d.texto("reservaId").orEmpty(), propietarioId = d.texto("propietarioId").orEmpty(),
            pagoId = d.texto("pagoId").orEmpty(), estado = estado, postulantes = postulantes
        )
        return TransferenciaLeida(t, d.largo("version") ?: 0L)
    }

    /** [version] es la que se escribe (la anterior + 1). */
    fun mapa(t: Transferencia, negocioId: String, version: Long): Map<String, Any?> = mapOf(
        "reservaId" to t.reservaId, "propietarioId" to t.propietarioId, "pagoId" to t.pagoId,
        "estado" to t.estado.name, "negocioId" to negocioId, "version" to version,
        "postulantes" to t.postulantes.map {
            mapOf(
                "id" to it.id, "usuarioId" to it.usuarioId, "estado" to it.estado.name,
                "deadline" to it.deadline, "aceptadoPorPropietario" to it.aceptadoPorPropietario
            )
        },
        "postulantesUids" to t.postulantes.map { it.usuarioId }.distinct()
    )

    // ---------- Comunidades, invitaciones, chats ----------
    fun comunidad(d: DocumentSnapshot): Comunidad? {
        val nombre = d.texto("nombre") ?: return null
        return Comunidad(
            id = d.id, nombre = nombre, descripcion = d.texto("descripcion").orEmpty(),
            creadorId = d.texto("creadorId").orEmpty(), adminId = d.texto("adminId").orEmpty(),
            miembros = d.lista("miembros"), fotoUri = d.texto("foto")?.let { FotosRemotas.resolver(it) }
        )
    }

    fun mapa(c: Comunidad, refFoto: String?): Map<String, Any?> = mapOf(
        "nombre" to c.nombre, "descripcion" to c.descripcion, "creadorId" to c.creadorId, "adminId" to c.adminId,
        "miembros" to c.miembros, "foto" to refFoto
    )

    fun invitacion(d: DocumentSnapshot): InvitacionComunidad? {
        val estado = d.enumerado<EstadoInvitacion>("estado") ?: return null
        return InvitacionComunidad(
            id = d.id, comunidadId = d.texto("comunidadId").orEmpty(), invitadoId = d.texto("invitadoId").orEmpty(),
            remitenteId = d.texto("remitenteId").orEmpty(), estado = estado
        )
    }

    fun mapa(i: InvitacionComunidad): Map<String, Any?> = mapOf(
        "comunidadId" to i.comunidadId, "invitadoId" to i.invitadoId, "remitenteId" to i.remitenteId,
        "estado" to i.estado.name, "actualizadaEn" to Nube.ahora()
    )

    /** Chat sin mensajes; los mensajes viven en la subcolección `mensajes`. */
    fun chat(d: DocumentSnapshot): Chat {
        val comunidad = d.texto("comunidadId")
        return Chat(
            id = d.id, miembros = if (comunidad == null) d.lista("miembros") else emptyList(),
            comunidadId = comunidad, mensajes = emptyList()
        )
    }

    fun mapaChat(c: Chat, miembros: List<String>): Map<String, Any?> = mapOf(
        "miembros" to miembros, "comunidadId" to c.comunidadId, "creadoEn" to Nube.ahora()
    )

    fun mensaje(d: DocumentSnapshot): Mensaje? {
        val autor = d.texto("autorId") ?: return null
        return Mensaje(id = d.id, autorId = autor, texto = d.texto("texto").orEmpty())
    }

    fun mapa(m: Mensaje): Map<String, Any?> = mapOf(
        "autorId" to m.autorId, "texto" to m.texto, "creadoEn" to Nube.ahora()
    )

    fun marcaTiempo(d: DocumentSnapshot, campo: String): Long =
        d.getTimestamp(campo)?.toDate()?.time ?: Long.MAX_VALUE

    // ---------- Reseñas, notificaciones, reportes ----------
    class ResenaLeida(val resena: Resena, val negocioId: String)

    fun resena(d: DocumentSnapshot): ResenaLeida? {
        val reservaId = d.texto("reservaId") ?: return null
        return ResenaLeida(
            Resena(
                id = d.id, reservaId = reservaId, usuarioId = d.texto("usuarioId").orEmpty(),
                puntaje = d.entero("puntaje"), comentario = d.texto("comentario").orEmpty(), respuesta = d.texto("respuesta")
            ),
            d.texto("negocioId").orEmpty()
        )
    }

    fun mapa(r: Resena, negocioId: String): Map<String, Any?> = mapOf(
        "reservaId" to r.reservaId, "usuarioId" to r.usuarioId, "puntaje" to r.puntaje,
        "comentario" to r.comentario, "respuesta" to r.respuesta, "negocioId" to negocioId,
        "creadaEn" to Nube.ahora()
    )

    /** La marca de tiempo sirve para ordenar de la más reciente a la más antigua. */
    fun notificacion(d: DocumentSnapshot): Pair<Notificacion, Long>? {
        val destinatario = d.texto("usuarioId") ?: return null
        return Pair(
            Notificacion(
                id = d.id, usuarioId = destinatario, titulo = d.texto("titulo").orEmpty(),
                texto = d.texto("texto").orEmpty(), ruta = d.texto("ruta"), argumento = d.texto("argumento"),
                leida = d.getBoolean("leida") ?: false
            ),
            marcaTiempo(d, "creadaEn")
        )
    }

    fun mapa(n: Notificacion, autorId: String): Map<String, Any?> = mapOf(
        "usuarioId" to n.usuarioId, "titulo" to n.titulo, "texto" to n.texto, "ruta" to n.ruta,
        "argumento" to n.argumento, "leida" to n.leida, "autorId" to autorId, "creadaEn" to Nube.ahora()
    )

    fun reporte(d: DocumentSnapshot): Reporte? {
        val estado = d.enumerado<EstadoReporte>("estado") ?: return null
        return Reporte(
            id = d.id, usuarioId = d.texto("usuarioId").orEmpty(), objetivo = d.texto("objetivo").orEmpty(),
            contexto = d.texto("contexto").orEmpty(), motivo = d.texto("motivo").orEmpty(),
            autorId = d.texto("autorId").orEmpty(), objetivoId = d.texto("objetivoId").orEmpty(),
            tipoObjetivo = d.texto("tipoObjetivo").orEmpty(), estado = estado
        )
    }

    fun mapa(r: Reporte): Map<String, Any?> = mapOf(
        "usuarioId" to r.usuarioId, "objetivo" to r.objetivo, "contexto" to r.contexto, "motivo" to r.motivo,
        "autorId" to r.autorId, "objetivoId" to r.objetivoId, "tipoObjetivo" to r.tipoObjetivo,
        "estado" to r.estado.name, "creadoEn" to Nube.ahora()
    )
}
