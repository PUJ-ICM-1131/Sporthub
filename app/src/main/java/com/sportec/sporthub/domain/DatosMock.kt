package com.sportec.sporthub.domain

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos

object DatosMock {
    private val hoy = java.time.LocalDate.now(java.time.ZoneId.of("America/Bogota"))

    val CONTRASENA_DEMO = Textos.obtener(R.string.texto_demo1234)

    val categorias: List<Categoria> = listOf(
        Categoria(Textos.obtener(R.string.texto_futbol), Segmento.DEPORTE),
        Categoria(Textos.obtener(R.string.texto_padel), Segmento.DEPORTE),
        Categoria(Textos.obtener(R.string.texto_tenis), Segmento.DEPORTE),
        Categoria(Textos.obtener(R.string.texto_baloncesto), Segmento.DEPORTE),
        Categoria(Textos.obtener(R.string.texto_natacion), Segmento.DEPORTE),
        Categoria(Textos.obtener(R.string.texto_squash), Segmento.DEPORTE),
        Categoria(Textos.obtener(R.string.texto_gimnasios), Segmento.BIENESTAR),
        Categoria(Textos.obtener(R.string.texto_crossfit), Segmento.BIENESTAR),
        Categoria(Textos.obtener(R.string.texto_yoga), Segmento.BIENESTAR),
        Categoria(Textos.obtener(R.string.texto_pilates), Segmento.BIENESTAR),
        Categoria(Textos.obtener(R.string.texto_boxeo), Segmento.BIENESTAR),
        Categoria(Textos.obtener(R.string.texto_entrenamiento_funcional), Segmento.BIENESTAR)
    )

    val negocios: List<Negocio> = listOf(
        Negocio("b1", Textos.obtener(R.string.texto_distrito_activo), Textos.obtener(R.string.texto_900123456), Textos.obtener(R.string.texto_300_555_0123), Textos.obtener(R.string.texto_calle_120_15_40_bogota), Textos.obtener(R.string.texto_espacios_para_moverte_a_tu_ritmo_deporte_y_bienestar), EstadoNegocio.VALIDADO, 4.70, -74.04),
        Negocio("b2", Textos.obtener(R.string.texto_movimiento_chapinero), Textos.obtener(R.string.texto_901222333), Textos.obtener(R.string.texto_300_444_3322), Textos.obtener(R.string.texto_carrera_7_60_12_bogota), Textos.obtener(R.string.texto_clases_de_bienestar), EstadoNegocio.PENDIENTE, 4.64, -74.06),
        Negocio("b3", Textos.obtener(R.string.texto_verde_yoga), Textos.obtener(R.string.texto_909000003), Textos.obtener(R.string.texto_300_555_0103), Textos.obtener(R.string.texto_calle_70_bogota), Textos.obtener(R.string.texto_establecimiento_ficticio_con_actividades_de_deporte_y_bienestar), EstadoNegocio.VALIDADO, 4.659, -74.071),
        Negocio("b4", Textos.obtener(R.string.texto_canchas_del_parque), Textos.obtener(R.string.texto_909000004), Textos.obtener(R.string.texto_300_555_0104), Textos.obtener(R.string.texto_carrera_30_bogota), Textos.obtener(R.string.texto_establecimiento_ficticio_con_actividades_de_deporte_y_bienestar), EstadoNegocio.VALIDADO, 4.660, -74.098),
        Negocio("b5", Textos.obtener(R.string.texto_raqueta_norte), Textos.obtener(R.string.texto_909000005), Textos.obtener(R.string.texto_300_555_0105), Textos.obtener(R.string.texto_calle_145_bogota), Textos.obtener(R.string.texto_establecimiento_ficticio_con_actividades_de_deporte_y_bienestar), EstadoNegocio.VALIDADO, 4.706, -74.093),
        Negocio("b6", Textos.obtener(R.string.texto_vital_occidente), Textos.obtener(R.string.texto_909000006), Textos.obtener(R.string.texto_300_555_0106), Textos.obtener(R.string.texto_occidente_bogota), Textos.obtener(R.string.texto_establecimiento_ficticio_con_actividades_de_deporte_y_bienestar), EstadoNegocio.VALIDADO, 4.67, -74.184),
        Negocio("b7", Textos.obtener(R.string.texto_club_sabana), Textos.obtener(R.string.texto_909000007), Textos.obtener(R.string.texto_300_555_0107), Textos.obtener(R.string.texto_sabana_bogota), Textos.obtener(R.string.texto_establecimiento_ficticio_con_actividades_de_deporte_y_bienestar), EstadoNegocio.VALIDADO, 4.805, -74.124)
    )

    private val nombresBase = listOf(
        Textos.obtener(R.string.texto_cancha_horizonte), Textos.obtener(R.string.texto_padel_entre_amigos), Textos.obtener(R.string.texto_tenis_al_aire_libre), Textos.obtener(R.string.texto_cancha_central),
        Textos.obtener(R.string.texto_piscina_sesion_libre), Textos.obtener(R.string.texto_squash_cancha_1), Textos.obtener(R.string.texto_entrena_a_tu_ritmo), Textos.obtener(R.string.texto_crossfit_en_equipo),
        Textos.obtener(R.string.texto_yoga_al_atardecer), Textos.obtener(R.string.texto_pilates_esencial), Textos.obtener(R.string.texto_boxeo_tecnica_inicial), Textos.obtener(R.string.texto_muevete_con_energia)
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
            descripcion = if (esEspacio) Textos.obtener(R.string.texto_tu_espacio_completo_para_compartir_una_sesion_de_deporte) else Textos.obtener(R.string.texto_una_clase_guiada_para_reconectar_con_tu_movimiento),
            politica = Politica(extra = Textos.obtener(R.string.texto_llega_con_ropa_comoda_cuida_los_espacios_y_sigue))
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
                nombre = Textos.obtener(R.string.formato_dos_datos, original.categoria, negocio.nombre),
                precio = original.precio + incremento
            )
        }
    }

    val solicitudes: List<Solicitud> = listOf(
        Solicitud("s1", "juan", "a1", hoy.toString(), 10, 2, 1, EstadoSolicitud.REVISION, 60000, Politica(extra = Textos.obtener(R.string.texto_llega_con_ropa_comoda))),
        Solicitud("s2", "camila", "a1", hoy.toString(), 10, 2, 2, EstadoSolicitud.FILA, 60000, Politica(extra = Textos.obtener(R.string.texto_llega_con_ropa_comoda)))
    )

    val reservas: List<Reserva> = listOf(
        Reserva("r1", "juan", "a2", hoy.plusDays(1).toString(), 14, 2, 45000, EstadoReserva.CONFIRMADA, "p1", politica = Politica(extra = Textos.obtener(R.string.texto_trae_tu_raqueta))),
        Reserva("r2", "juan", "a8", hoy.minusDays(1).toString(), 16, 2, 25000, EstadoReserva.CONFIRMADA, "p2", cerrada = true, politica = Politica()),
        Reserva("r3", "camila", "a9", hoy.plusDays(1).toString(), 18, 2, 30000, EstadoReserva.CONFIRMADA, "p3", politica = Politica())
    )

    val pagos: List<Pago> = listOf(
        Pago("p1", "juan", 45000, EstadoPago.EXITOSO, Textos.obtener(R.string.texto_tarjeta_1234), reservaId = "r1"),
        Pago("p2", "juan", 25000, EstadoPago.EXITOSO, Textos.obtener(R.string.texto_billetera_4567), reservaId = "r2"),
        Pago("p3", "camila", 30000, EstadoPago.EXITOSO, Textos.obtener(R.string.texto_transferencia_1122), reservaId = "r3")
    )

    val transferencias: List<Transferencia> = listOf(
        Transferencia("t1", "r3", "camila", "p3", EstadoTransferencia.PUBLICADA, listOf(Postulante("st1", "juan", EstadoPostulante.REVISION))),
        Transferencia("t2", "r1", "juan", "p1", EstadoTransferencia.PUBLICADA)
    )

    val comunidades: List<Comunidad> = listOf(
        Comunidad("g1", Textos.obtener(R.string.texto_bogota_se_mueve), Textos.obtener(R.string.texto_deporte_bienestar_y_planes_entre_amigos), "juan", "juan", listOf("juan", "camila"))
    )

    val chats: List<Chat> = listOf(
        Chat("c1", miembros = listOf("juan", "camila"), mensajes = listOf(
            Mensaje("m1", "camila", Textos.obtener(R.string.texto_hola_miramos_una_clase_de_yoga_para_manana)),
            Mensaje("m2", "juan", Textos.obtener(R.string.texto_si_voy_a_revisar_los_horarios))
        )),
        Chat("c2", miembros = listOf("juan", "negocio"), mensajes = listOf(
            Mensaje("m3", "negocio", Textos.obtener(R.string.texto_hola_juan_podemos_ayudarte_con_horarios_y_servicios))
        )),
        Chat("g1", comunidadId = "g1", mensajes = listOf(
            Mensaje("m4", "camila", Textos.obtener(R.string.texto_bienvenidos_compartamos_nuestros_proximos_planes))
        ))
    )

    val notificaciones: List<Notificacion> = listOf(
        Notificacion("n1", "juan", Textos.obtener(R.string.texto_tu_solicitud_esta_en_revision), Textos.obtener(R.string.texto_distrito_activo_revisara_tu_solicitud_de_padel), "request", "s1")
    )

    val reportes: List<Reporte> = listOf(
        Reporte("rep1", "camila", Textos.obtener(R.string.texto_mensaje_m2), Textos.obtener(R.string.texto_si_voy_a_revisar_los_horarios), Textos.obtener(R.string.texto_problema_de_interaccion), "juan", "m2", "mensaje")
    )

    val resenas: List<Resena> = listOf(
        Resena("res1", "r2", "juan", 5, Textos.obtener(R.string.texto_excelente_energia_en_la_clase_repito_la_proxima_semana))
    )

    fun actividadPorId(id: String): Actividad? = actividades.firstOrNull { it.id == id }
}
