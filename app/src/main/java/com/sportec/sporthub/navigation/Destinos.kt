package com.sportec.sporthub.navigation

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos

import androidx.navigation3.runtime.NavKey
import com.sportec.sporthub.domain.Rol

data class Destino(
    val etiqueta: String,
    val ruta: NavKey
)

data class InfoPantalla(
    val titulo: String,
    val historias: List<String>,
    val destinos: List<Destino> = emptyList()
)

fun opcionesPerfil(rol: Rol?): List<Destino> = when (rol) {
    Rol.DEPORTISTA -> listOf(
        Destino(Textos.obtener(R.string.texto_notificaciones), Notificaciones),
        Destino(Textos.obtener(R.string.texto_reservas_transferibles), Transferencias),
        Destino(Textos.obtener(R.string.texto_mis_transferencias), MisTransferencias),
        Destino(Textos.obtener(R.string.texto_reembolsos), Reembolsos),
        Destino(Textos.obtener(R.string.texto_reportar_una_incidencia), Reportar("general"))
    )
    Rol.ESTABLECIMIENTO -> listOf(
        Destino(Textos.obtener(R.string.texto_ver_ingresos), Gestion),
        Destino(Textos.obtener(R.string.texto_estado_de_validacion), ValidacionNegocio),
        Destino(Textos.obtener(R.string.texto_agenda), Agenda),
        Destino(Textos.obtener(R.string.texto_resenas), ResenasNegocio),
        Destino(Textos.obtener(R.string.texto_reembolsos), Reembolsos),
        Destino(Textos.obtener(R.string.texto_notificaciones), Notificaciones)
    )
    Rol.ADMINISTRADOR -> listOf(
        Destino(Textos.obtener(R.string.texto_notificaciones), Notificaciones)
    )
    null -> emptyList()
}

fun infoDe(ruta: NavKey): InfoPantalla = when (ruta) {
    Bienvenida -> InfoPantalla(Textos.obtener(R.string.app_name), listOf("HU-C06"))
    Login -> InfoPantalla(Textos.obtener(R.string.texto_iniciar_sesion), listOf("HU-C03"))
    Registro -> InfoPantalla(Textos.obtener(R.string.texto_crear_cuenta), listOf("HU-C01", "HU-C02"))
    RegistroDeportista -> InfoPantalla(
        Textos.obtener(R.string.texto_registro_de_deportista), listOf("HU-C01"),
        listOf(Destino(Textos.obtener(R.string.texto_ir_a_iniciar_sesion), Login))
    )
    RegistroEstablecimiento -> InfoPantalla(
        Textos.obtener(R.string.texto_registro_de_establecimiento), listOf("HU-C02"),
        listOf(Destino(Textos.obtener(R.string.texto_ir_a_iniciar_sesion), Login))
    )
    Recuperar -> InfoPantalla(Textos.obtener(R.string.texto_recuperar_acceso), listOf("HU-C04"))

    Explorar -> InfoPantalla(Textos.obtener(R.string.texto_explorar), listOf("HU-D01"))
    Buscar -> InfoPantalla(
        Textos.obtener(R.string.texto_buscar), listOf("HU-D01", "HU-D02"),
        listOf(Destino(Textos.obtener(R.string.texto_filtros), Filtros), Destino(Textos.obtener(R.string.texto_ver_en_el_mapa), Mapa), Destino(Textos.obtener(R.string.texto_ver_un_resultado), DetalleActividad("a1")))
    )
    Filtros -> InfoPantalla(
        Textos.obtener(R.string.texto_filtros), listOf("HU-D02"),
        listOf(Destino(Textos.obtener(R.string.texto_elegir_origen_manual), Origen))
    )
    Mapa -> InfoPantalla(
        Textos.obtener(R.string.texto_mapa), listOf("HU-D02", "HU-D05", "HU-D27"),
        listOf(
            Destino(Textos.obtener(R.string.texto_elegir_origen), Origen),
            Destino(Textos.obtener(R.string.texto_ver_establecimiento), DetalleEstablecimiento("b1")),
            Destino(Textos.obtener(R.string.texto_como_llegar), Ruta("b1")),
            Destino(Textos.obtener(R.string.texto_permiso_de_ubicacion), Permiso("ubicacion"))
        )
    )
    Origen -> InfoPantalla(Textos.obtener(R.string.texto_origen_de_busqueda), listOf("HU-D02", "HU-D05", "HU-D27"))
    is DetalleActividad -> InfoPantalla(
        Textos.obtener(R.string.texto_detalle_del_servicio), listOf("HU-D03"),
        listOf(Destino(Textos.obtener(R.string.texto_ver_establecimiento), DetalleEstablecimiento("b1")), Destino(Textos.obtener(R.string.texto_ver_horarios), Horarios(ruta.id)))
    )
    is DetalleEstablecimiento -> InfoPantalla(
        Textos.obtener(R.string.texto_establecimiento), listOf("HU-D03", "HU-D05", "HU-D14", "HU-D19"),
        listOf(
            Destino(Textos.obtener(R.string.texto_ver_servicio), DetalleActividad("a1")),
            Destino(Textos.obtener(R.string.texto_ver_horarios), Horarios("a1")),
            Destino(Textos.obtener(R.string.texto_enviar_mensaje), Chat("c2")),
            Destino(Textos.obtener(R.string.texto_como_llegar), Ruta(ruta.id)),
            Destino(Textos.obtener(R.string.texto_reportar), Reportar("negocio"))
        )
    )
    is Horarios -> InfoPantalla(
        Textos.obtener(R.string.texto_horarios_y_cupos), listOf("HU-D04", "HU-D06", "HU-D07", "HU-D14"),
        listOf(Destino(Textos.obtener(R.string.texto_solicitar_este_horario), ResumenSolicitud(ruta.actividadId, java.time.LocalDate.now(java.time.ZoneId.of("America/Bogota")).toString(), 10)))
    )

    is ResumenSolicitud -> InfoPantalla(
        Textos.obtener(R.string.texto_resumen_de_la_solicitud), listOf("HU-D06", "HU-D07"),
        listOf(Destino(Textos.obtener(R.string.texto_enviar_solicitud), DetalleSolicitud("s1")))
    )
    is DetalleSolicitud -> InfoPantalla(
        Textos.obtener(R.string.texto_solicitud), listOf("HU-D06", "HU-D08", "HU-D09", "HU-D32", "HU-E06", "HU-E13"),
        listOf(Destino(Textos.obtener(R.string.texto_pagar), Pago(ruta.id)), Destino(Textos.obtener(R.string.texto_rechazar_con_motivo), MotivoRechazo(ruta.id)))
    )
    is Pago -> InfoPantalla(
        Textos.obtener(R.string.texto_pago), listOf("HU-D09", "HU-D16"),
        listOf(Destino(Textos.obtener(R.string.texto_confirmar_pago), PagoExitoso(ruta.referencia)))
    )
    is PagoExitoso -> InfoPantalla(
        Textos.obtener(R.string.texto_pago_confirmado), listOf("HU-D09", "HU-D16"),
        listOf(Destino(Textos.obtener(R.string.texto_ver_reserva), DetalleReserva("r1")))
    )
    MisReservas -> InfoPantalla(
        Textos.obtener(R.string.texto_mis_reservas), listOf("HU-D08", "HU-D10", "HU-D32"),
        listOf(
            Destino(Textos.obtener(R.string.texto_solicitud_en_revision), DetalleSolicitud("s1")),
            Destino(Textos.obtener(R.string.texto_reserva_confirmada), DetalleReserva("r1")),
            Destino(Textos.obtener(R.string.texto_reserva_finalizada), DetalleReserva("r2")),
            Destino(Textos.obtener(R.string.texto_reembolsos), Reembolsos)
        )
    )
    is DetalleReserva -> InfoPantalla(
        Textos.obtener(R.string.texto_reserva), listOf("HU-D10", "HU-D11", "HU-D13", "HU-D29", "HU-D31", "HU-E07", "HU-E08"),
        listOf(
            Destino(Textos.obtener(R.string.texto_cancelar_reserva), CancelarReserva(ruta.id)),
            Destino(Textos.obtener(R.string.texto_publicar_transferencia), PublicarTransferencia(ruta.id)),
            Destino(Textos.obtener(R.string.texto_como_llegar), Ruta("b1")),
            Destino(Textos.obtener(R.string.texto_registrar_llegada), CheckIn(ruta.id)),
            Destino(Textos.obtener(R.string.texto_calificar), Resena(ruta.id))
        )
    )
    is CancelarReserva -> InfoPantalla(
        Textos.obtener(R.string.texto_cancelar_reserva), listOf("HU-D11"),
        listOf(Destino(Textos.obtener(R.string.texto_ver_reembolso), DetalleReembolso("ref1")))
    )
    Reembolsos -> InfoPantalla(
        Textos.obtener(R.string.texto_reembolsos), listOf("HU-D12", "HU-E09"),
        listOf(Destino(Textos.obtener(R.string.texto_ver_reembolso), DetalleReembolso("ref1")))
    )
    is DetalleReembolso -> InfoPantalla(Textos.obtener(R.string.texto_reembolso), listOf("HU-D11", "HU-D12", "HU-D17", "HU-E09"))

    is PublicarTransferencia -> InfoPantalla(
        Textos.obtener(R.string.texto_publicar_transferencia), listOf("HU-D13"),
        listOf(Destino(Textos.obtener(R.string.texto_ver_publicacion), DetalleTransferencia("t2")))
    )
    Transferencias -> InfoPantalla(
        Textos.obtener(R.string.texto_reservas_transferibles), listOf("HU-D14"),
        listOf(Destino(Textos.obtener(R.string.texto_ver_publicacion), DetalleTransferencia("t1")), Destino(Textos.obtener(R.string.texto_mis_transferencias), MisTransferencias))
    )
    is DetalleTransferencia -> InfoPantalla(
        Textos.obtener(R.string.texto_transferencia), listOf("HU-D13", "HU-D14", "HU-D15", "HU-D16", "HU-D17", "HU-D18", "HU-E10"),
        listOf(
            Destino(Textos.obtener(R.string.texto_ver_establecimiento), DetalleEstablecimiento("b1")),
            Destino(Textos.obtener(R.string.texto_hablar_con_quien_publico), Chat("c1")),
            Destino(Textos.obtener(R.string.texto_pagar_transferencia), Pago(ruta.id)),
            Destino(Textos.obtener(R.string.texto_rechazar_con_motivo), MotivoTransferencia(ruta.id)),
            Destino(Textos.obtener(R.string.texto_ver_reembolso), DetalleReembolso("ref1"))
        )
    )
    MisTransferencias -> InfoPantalla(
        Textos.obtener(R.string.texto_mis_transferencias), listOf("HU-D17"),
        listOf(Destino(Textos.obtener(R.string.texto_ver_transferencia), DetalleTransferencia("t1")))
    )

    Mensajes -> InfoPantalla(
        Textos.obtener(R.string.texto_mensajes), listOf("HU-D18", "HU-D20", "HU-D22", "HU-D25", "HU-E11"),
        listOf(
            Destino(Textos.obtener(R.string.texto_chat_con_camila), Chat("c1")),
            Destino(Textos.obtener(R.string.texto_chat_con_distrito_activo), Chat("c2")),
            Destino(Textos.obtener(R.string.texto_nuevo_chat), NuevoChat),
            Destino(Textos.obtener(R.string.texto_crear_comunidad), CrearComunidad),
            Destino(Textos.obtener(R.string.texto_comunidad_bogota_se_mueve), Comunidad("g1"))
        )
    )
    is Chat -> InfoPantalla(
        Textos.obtener(R.string.texto_conversacion), listOf("HU-D18", "HU-D19", "HU-D23", "HU-E11"),
        listOf(Destino(Textos.obtener(R.string.texto_reportar_mensaje), Reportar("mensaje")))
    )
    NuevoChat -> InfoPantalla(
        Textos.obtener(R.string.texto_nuevo_chat), listOf("HU-D18"),
        listOf(Destino(Textos.obtener(R.string.texto_abrir_conversacion), Chat("c1")))
    )
    CrearComunidad -> InfoPantalla(
        Textos.obtener(R.string.texto_crear_comunidad), listOf("HU-D20"),
        listOf(Destino(Textos.obtener(R.string.texto_ver_comunidad_creada), Comunidad("g1")))
    )
    is Comunidad -> InfoPantalla(
        Textos.obtener(R.string.texto_comunidad), listOf("HU-D20", "HU-D21", "HU-D22", "HU-D23", "HU-D24", "HU-D25"),
        listOf(
            Destino(Textos.obtener(R.string.texto_chat_del_grupo), Chat(ruta.id)),
            Destino(Textos.obtener(R.string.texto_editar_comunidad), EditarComunidad(ruta.id)),
            Destino(Textos.obtener(R.string.texto_invitar_integrantes), Invitar(ruta.id)),
            Destino(Textos.obtener(R.string.texto_asignar_sucesor), Sucesion(ruta.id))
        )
    )
    is EditarComunidad -> InfoPantalla(
        Textos.obtener(R.string.texto_editar_comunidad), listOf("HU-D24"),
        listOf(Destino(Textos.obtener(R.string.texto_invitar_integrantes), Invitar(ruta.id)), Destino(Textos.obtener(R.string.texto_asignar_sucesor), Sucesion(ruta.id)))
    )
    is Invitar -> InfoPantalla(
        Textos.obtener(R.string.texto_invitar_integrantes), listOf("HU-D21", "HU-D24", "HU-D26"),
        listOf(Destino(Textos.obtener(R.string.texto_elegir_desde_contactos), Contactos), Destino(Textos.obtener(R.string.texto_permiso_de_contactos), Permiso("contactos")))
    )
    is Sucesion -> InfoPantalla(Textos.obtener(R.string.texto_sucesion_de_la_comunidad), listOf("HU-D24", "HU-D25"))
    Contactos -> InfoPantalla(Textos.obtener(R.string.texto_contactos), listOf("HU-D26"))

    is Permiso -> InfoPantalla(when (ruta.tipo) {
        "camara" -> Textos.obtener(R.string.texto_permiso_de_camara)
        "contactos" -> Textos.obtener(R.string.texto_permiso_de_contactos)
        else -> Textos.obtener(R.string.texto_permiso_de_ubicacion)
    }, listOf("HU-D26", "HU-D27", "HU-D29", "HU-D30"))
    is Ruta -> InfoPantalla(
        Textos.obtener(R.string.texto_como_llegar), listOf("HU-D27", "HU-D28"),
        listOf(Destino(Textos.obtener(R.string.texto_elegir_origen), Origen), Destino(Textos.obtener(R.string.texto_abrir_brujula), Brujula(ruta.negocioId)))
    )
    is Brujula -> InfoPantalla(Textos.obtener(R.string.texto_brujula), listOf("HU-D28"))
    is CheckIn -> InfoPantalla(
        Textos.obtener(R.string.texto_registrar_llegada), listOf("HU-D29", "HU-D30"),
        listOf(Destino(Textos.obtener(R.string.texto_permiso_de_ubicacion), Permiso("ubicacion")), Destino(Textos.obtener(R.string.texto_agregar_fotografia), FotoLlegada(ruta.reservaId)))
    )
    is FotoLlegada -> InfoPantalla(
        Textos.obtener(R.string.texto_fotografia_de_llegada), listOf("HU-D30"),
        listOf(Destino(Textos.obtener(R.string.texto_permiso_de_camara), Permiso("camara")))
    )
    is Resena -> InfoPantalla(Textos.obtener(R.string.texto_calificar), listOf("HU-D31"))
    Notificaciones -> InfoPantalla(
        Textos.obtener(R.string.texto_notificaciones), listOf("HU-C07", "HU-C08"),
        listOf(
            Destino(Textos.obtener(R.string.texto_solicitud_en_revision), DetalleSolicitud("s1")),
            Destino(Textos.obtener(R.string.texto_reserva_confirmada), DetalleReserva("r1")),
            Destino(Textos.obtener(R.string.texto_transferencia), DetalleTransferencia("t1"))
        )
    )
    is Reportar -> InfoPantalla(Textos.obtener(R.string.texto_reportar_incidencia), listOf("HU-C08"))
    EditarPerfil -> InfoPantalla(
        Textos.obtener(R.string.texto_editar_perfil), listOf("HU-C05"),
        listOf(Destino(Textos.obtener(R.string.texto_permiso_de_camara), Permiso("camara")))
    )
    Perfil -> InfoPantalla(Textos.obtener(R.string.texto_perfil), listOf("HU-C05", "HU-C06"))

    Gestion -> InfoPantalla(
        Textos.obtener(R.string.texto_gestion), listOf("HU-E06", "HU-E07", "HU-E10", "HU-E13"),
        listOf(
            Destino(Textos.obtener(R.string.texto_solicitud_por_revisar), DetalleSolicitud("s1")),
            Destino(Textos.obtener(R.string.texto_agenda), Agenda),
            Destino(Textos.obtener(R.string.texto_transferencia_por_aprobar), DetalleTransferencia("t1"))
        )
    )
    Agenda -> InfoPantalla(
        Textos.obtener(R.string.texto_agenda), listOf("HU-E07"),
        listOf(Destino(Textos.obtener(R.string.texto_ver_reserva), DetalleReserva("r1")))
    )
    is MotivoRechazo -> InfoPantalla(Textos.obtener(R.string.texto_motivo_de_rechazo), listOf("HU-E06"))
    is MotivoTransferencia -> InfoPantalla(Textos.obtener(R.string.texto_motivo_de_rechazo), listOf("HU-E10"))
    Servicios -> InfoPantalla(
        Textos.obtener(R.string.texto_servicios), listOf("HU-E02", "HU-E03", "HU-E04", "HU-E05"),
        listOf(
            Destino(Textos.obtener(R.string.texto_nuevo_servicio), EditarServicio("nuevo")),
            Destino(Textos.obtener(R.string.texto_editar_servicio), EditarServicio("a1"))
        )
    )
    is EditarServicio -> InfoPantalla(Textos.obtener(R.string.texto_servicio), listOf("HU-E03", "HU-E04", "HU-E05"))
    Pagos -> InfoPantalla(
        Textos.obtener(R.string.texto_pagos), listOf("HU-E08"),
        listOf(Destino(Textos.obtener(R.string.texto_reserva_pagada), DetalleReserva("r1")), Destino(Textos.obtener(R.string.texto_reembolsos), Reembolsos))
    )
    ValidacionNegocio -> InfoPantalla(
        Textos.obtener(R.string.texto_estado_de_validacion), listOf("HU-C02", "HU-E01")
    )
    ResenasNegocio -> InfoPantalla(
        Textos.obtener(R.string.texto_resenas), listOf("HU-E12"),
        listOf(Destino(Textos.obtener(R.string.texto_responder_resena), ResponderResena("res1")))
    )
    is ResponderResena -> InfoPantalla(Textos.obtener(R.string.texto_responder_resena), listOf("HU-E12"))

    Validaciones -> InfoPantalla(
        Textos.obtener(R.string.texto_negocios_por_validar), listOf("HU-A01", "HU-A02"),
        listOf(Destino(Textos.obtener(R.string.texto_movimiento_chapinero), DetalleValidacion("b2")))
    )
    is DetalleValidacion -> InfoPantalla(Textos.obtener(R.string.texto_validacion_de_negocio), listOf("HU-A01", "HU-A02"))
    Reportes -> InfoPantalla(
        Textos.obtener(R.string.texto_reportes), listOf("HU-A03", "HU-A04"),
        listOf(Destino(Textos.obtener(R.string.texto_reporte_pendiente), DetalleReporte("rep1")))
    )
    is DetalleReporte -> InfoPantalla(Textos.obtener(R.string.texto_detalle_del_reporte), listOf("HU-A03", "HU-A04"))

    else -> InfoPantalla(Textos.obtener(R.string.app_name), emptyList())
}
