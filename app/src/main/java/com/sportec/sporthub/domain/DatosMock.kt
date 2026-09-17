package com.sportec.sporthub.domain

object DatosMock {

    const val CONTRASENA_DEMO = "Demo1234"

    val categorias: List<Categoria> = listOf(
        Categoria("Fútbol", Segmento.DEPORTE),
        Categoria("Pádel", Segmento.DEPORTE),
        Categoria("Tenis", Segmento.DEPORTE),
        Categoria("Baloncesto", Segmento.DEPORTE),
        Categoria("Natación", Segmento.DEPORTE),
        Categoria("Squash", Segmento.DEPORTE),
        Categoria("Gimnasios", Segmento.BIENESTAR),
        Categoria("CrossFit", Segmento.BIENESTAR),
        Categoria("Yoga", Segmento.BIENESTAR),
        Categoria("Pilates", Segmento.BIENESTAR),
        Categoria("Boxeo", Segmento.BIENESTAR),
        Categoria("Entrenamiento funcional", Segmento.BIENESTAR)
    )

    val cuentas: List<Cuenta> = listOf(
        Cuenta("juan", "Juan Carlos", "juan@demo.test", CONTRASENA_DEMO, Rol.DEPORTISTA, telefono = "300 123 4567"),
        Cuenta("camila", "Camila Torres", "camila@demo.test", CONTRASENA_DEMO, Rol.DEPORTISTA),
        Cuenta("negocio", "Distrito Activo", "negocio@demo.test", CONTRASENA_DEMO, Rol.ESTABLECIMIENTO, negocioId = "b1"),
        Cuenta("admin", "Ana Administración", "admin@demo.test", CONTRASENA_DEMO, Rol.ADMINISTRADOR)
    )

    val negocios: List<Negocio> = listOf(
        Negocio("b1", "Distrito Activo", "900123456", "300 555 0123", "Calle 120 # 15-40, Bogotá", "Espacios para moverte a tu ritmo. Deporte y bienestar en el norte de Bogotá.", EstadoNegocio.VALIDADO, 4.70, -74.04),
        Negocio("b2", "Movimiento Chapinero", "901222333", "300 444 3322", "Carrera 7 # 60-12, Bogotá", "Clases de bienestar.", EstadoNegocio.PENDIENTE, 4.64, -74.06),
        Negocio("b3", "Verde Yoga", "909000003", "300 555 0103", "Calle 70, Bogotá", "Establecimiento ficticio con actividades de deporte y bienestar.", EstadoNegocio.VALIDADO, 4.659, -74.071),
        Negocio("b4", "Canchas del Parque", "909000004", "300 555 0104", "Carrera 30, Bogotá", "Establecimiento ficticio con actividades de deporte y bienestar.", EstadoNegocio.VALIDADO, 4.660, -74.098),
        Negocio("b5", "Raqueta Norte", "909000005", "300 555 0105", "Calle 145, Bogotá", "Establecimiento ficticio con actividades de deporte y bienestar.", EstadoNegocio.VALIDADO, 4.706, -74.093),
        Negocio("b6", "Vital Occidente", "909000006", "300 555 0106", "Occidente, Bogotá", "Establecimiento ficticio con actividades de deporte y bienestar.", EstadoNegocio.VALIDADO, 4.67, -74.184),
        Negocio("b7", "Club Sabana", "909000007", "300 555 0107", "Sabana, Bogotá", "Establecimiento ficticio con actividades de deporte y bienestar.", EstadoNegocio.VALIDADO, 4.805, -74.124)
    )

    private val nombresBase = listOf(
        "Cancha Horizonte", "Pádel entre amigos", "Tenis al aire libre", "Cancha Central",
        "Piscina · Sesión libre", "Squash · Cancha 1", "Entrena a tu ritmo", "CrossFit en equipo",
        "Yoga al atardecer", "Pilates esencial", "Boxeo · Técnica inicial", "Muévete con energía"
    )

    private val preciosBase = listOf(
        90000, 60000, 45000, 65000, 40000, 35000, 28000, 32000, 25000, 30000, 30000, 24000
    )

    private val actividadesBase: List<Actividad> = categorias.mapIndexed { i, categoria ->
        val esEspacio = i < 6
        Actividad(
            id = "a$i",
            negocioId = "b1",
            nombre = nombresBase[i],
            categoria = categoria.nombre,
            tipo = if (esEspacio) TipoActividad.ESPACIO else TipoActividad.CLASE,
            precio = preciosBase[i],
            duracionMinutos = 120,
            capacidad = if (esEspacio) 1 else 12,
            descripcion = if (esEspacio) "Tu espacio completo para compartir una sesión de deporte." else "Una clase guiada para reconectar con tu movimiento.",
            politica = Politica(extra = "Llega con ropa cómoda. Cuida los espacios y sigue las indicaciones del equipo.")
        )
    }

    private val plantillasPorNegocio = mapOf(
        "b3" to listOf(8, 9),
        "b4" to listOf(0, 3),
        "b5" to listOf(1, 2, 5),
        "b6" to listOf(6, 7, 10),
        "b7" to listOf(4, 11)
    )

    val actividades: List<Actividad> = actividadesBase + plantillasPorNegocio.flatMap { (negocioId, indices) ->
        val negocio = negocios.first { it.id == negocioId }
        val incremento = negocioId.drop(1).toInt() * 1000
        indices.map { i ->
            val original = actividadesBase[i]
            original.copy(
                id = "${negocioId}a$i",
                negocioId = negocioId,
                nombre = "${original.categoria} · ${negocio.nombre}",
                precio = original.precio + incremento
            )
        }
    }

    val solicitudes: List<Solicitud> = listOf(
        Solicitud("s1", "juan", "a1", "2026-09-22", 10, 2, 1, EstadoSolicitud.REVISION, 60000, Politica(extra = "Llega con ropa cómoda.")),
        Solicitud("s2", "camila", "a1", "2026-09-22", 10, 2, 2, EstadoSolicitud.FILA, 60000, Politica(extra = "Llega con ropa cómoda."))
    )

    val reservas: List<Reserva> = listOf(
        Reserva("r1", "juan", "a2", "2026-09-23", 14, 2, 45000, EstadoReserva.CONFIRMADA, "p1", politica = Politica(extra = "Trae tu raqueta.")),
        Reserva("r2", "juan", "a8", "2026-09-21", 16, 2, 25000, EstadoReserva.CONFIRMADA, "p2", cerrada = true, politica = Politica()),
        Reserva("r3", "camila", "a9", "2026-09-23", 18, 2, 30000, EstadoReserva.CONFIRMADA, "p3", politica = Politica())
    )

    val pagos: List<Pago> = listOf(
        Pago("p1", "juan", 45000, EstadoPago.EXITOSO, "Tarjeta ·•• 1234", reservaId = "r1"),
        Pago("p2", "juan", 25000, EstadoPago.EXITOSO, "Billetera ·•• 4567", reservaId = "r2"),
        Pago("p3", "camila", 30000, EstadoPago.EXITOSO, "Transferencia ·•• 1122", reservaId = "r3")
    )

    val transferencias: List<Transferencia> = listOf(
        Transferencia("t1", "r3", "camila", "p3", EstadoTransferencia.PUBLICADA, listOf(Postulante("st1", "juan", EstadoPostulante.REVISION))),
        Transferencia("t2", "r1", "juan", "p1", EstadoTransferencia.PUBLICADA)
    )

    val comunidades: List<Comunidad> = listOf(
        Comunidad("g1", "Bogotá se mueve", "Deporte, bienestar y planes entre amigos.", "juan", "juan", listOf("juan", "camila"))
    )

    val chats: List<Chat> = listOf(
        Chat("c1", miembros = listOf("juan", "camila"), mensajes = listOf(
            Mensaje("m1", "camila", "¡Hola! ¿Miramos una clase de yoga para mañana?"),
            Mensaje("m2", "juan", "Sí, voy a revisar los horarios.")
        )),
        Chat("c2", miembros = listOf("juan", "negocio"), mensajes = listOf(
            Mensaje("m3", "negocio", "Hola, Juan. Podemos ayudarte con horarios y servicios.")
        )),
        Chat("g1", comunidadId = "g1", mensajes = listOf(
            Mensaje("m4", "camila", "¡Bienvenidos! Compartamos nuestros próximos planes.")
        ))
    )

    val notificaciones: List<Notificacion> = listOf(
        Notificacion("n1", "juan", "Tu solicitud está en revisión", "Distrito Activo revisará tu solicitud de pádel.", "request", "s1")
    )

    val reportes: List<Reporte> = listOf(
        Reporte("rep1", "camila", "Mensaje m2", "Sí, voy a revisar los horarios.", "Problema de interacción", "juan", "m2", "mensaje")
    )

    val resenas: List<Resena> = listOf(
        Resena("res1", "r2", "juan", 5, "Excelente energía en la clase, repito la próxima semana.")
    )

    fun actividadPorId(id: String): Actividad? = actividades.firstOrNull { it.id == id }
}
