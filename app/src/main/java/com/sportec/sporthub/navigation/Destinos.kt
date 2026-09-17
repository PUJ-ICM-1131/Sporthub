package com.sportec.sporthub.navigation

import androidx.navigation3.runtime.NavKey
import com.sportec.sporthub.data.model.Rol

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
        Destino("Editar perfil", EditarPerfil),
        Destino("Notificaciones", Notificaciones),
        Destino("Reservas transferibles", Transferencias),
        Destino("Mis transferencias", MisTransferencias),
        Destino("Reembolsos", Reembolsos),
        Destino("Reportar una incidencia", Reportar("general"))
    )
    Rol.ESTABLECIMIENTO -> listOf(
        Destino("Estado de validación", ValidacionNegocio),
        Destino("Editar ficha del negocio", EditarNegocio),
        Destino("Agenda", Agenda),
        Destino("Reseñas", ResenasNegocio),
        Destino("Reembolsos", Reembolsos),
        Destino("Editar perfil", EditarPerfil),
        Destino("Notificaciones", Notificaciones)
    )
    Rol.ADMINISTRADOR -> listOf(
        Destino("Editar perfil", EditarPerfil),
        Destino("Notificaciones", Notificaciones)
    )
    null -> emptyList()
}

fun infoDe(ruta: NavKey): InfoPantalla = when (ruta) {
    Bienvenida -> InfoPantalla("SportHub", listOf("HU-C06"))
    Login -> InfoPantalla("Iniciar sesión", listOf("HU-C03"))
    Registro -> InfoPantalla("Crear cuenta", listOf("HU-C01", "HU-C02"))
    RegistroDeportista -> InfoPantalla(
        "Registro de deportista", listOf("HU-C01"),
        listOf(Destino("Ir a iniciar sesión", Login))
    )
    RegistroEstablecimiento -> InfoPantalla(
        "Registro de establecimiento", listOf("HU-C02"),
        listOf(Destino("Ir a iniciar sesión", Login))
    )
    Recuperar -> InfoPantalla(
        "Recuperar acceso", listOf("HU-C04"),
        listOf(Destino("Abrir enlace de recuperación", EnlaceRecuperacion))
    )
    EnlaceRecuperacion -> InfoPantalla(
        "Enlace de recuperación", listOf("HU-C04"),
        listOf(Destino("Definir nueva contraseña", NuevaContrasena), Destino("Solicitar otro enlace", Recuperar))
    )
    NuevaContrasena -> InfoPantalla(
        "Nueva contraseña", listOf("HU-C04"),
        listOf(Destino("Volver a iniciar sesión", Login))
    )

    Explorar -> InfoPantalla("Explorar", listOf("HU-D01"))
    Buscar -> InfoPantalla(
        "Buscar", listOf("HU-D01", "HU-D02"),
        listOf(Destino("Filtros", Filtros), Destino("Ver en el mapa", Mapa), Destino("Ver un resultado", DetalleActividad("a1")))
    )
    Filtros -> InfoPantalla(
        "Filtros", listOf("HU-D02"),
        listOf(Destino("Elegir origen manual", Origen))
    )
    Mapa -> InfoPantalla(
        "Mapa", listOf("HU-D02", "HU-D05", "HU-D27"),
        listOf(
            Destino("Elegir origen", Origen),
            Destino("Ver establecimiento", DetalleEstablecimiento("b1")),
            Destino("Cómo llegar", Ruta("b1")),
            Destino("Permiso de ubicación", Permiso("ubicacion"))
        )
    )
    Origen -> InfoPantalla("Origen de búsqueda", listOf("HU-D02", "HU-D05", "HU-D27"))
    is DetalleActividad -> InfoPantalla(
        "Detalle del servicio", listOf("HU-D03"),
        listOf(Destino("Ver establecimiento", DetalleEstablecimiento("b1")), Destino("Ver horarios", Horarios(ruta.id)))
    )
    is DetalleEstablecimiento -> InfoPantalla(
        "Establecimiento", listOf("HU-D03", "HU-D05", "HU-D14", "HU-D19"),
        listOf(
            Destino("Ver servicio", DetalleActividad("a1")),
            Destino("Ver horarios", Horarios("a1")),
            Destino("Enviar mensaje", Chat("c2")),
            Destino("Cómo llegar", Ruta(ruta.id)),
            Destino("Reportar", Reportar("negocio"))
        )
    )
    is Horarios -> InfoPantalla(
        "Horarios y cupos", listOf("HU-D04", "HU-D06", "HU-D07", "HU-D14"),
        listOf(Destino("Solicitar este horario", ResumenSolicitud(ruta.actividadId, "2026-09-22", 10)))
    )

    is ResumenSolicitud -> InfoPantalla(
        "Resumen de la solicitud", listOf("HU-D06", "HU-D07"),
        listOf(Destino("Enviar solicitud", DetalleSolicitud("s1")))
    )
    is DetalleSolicitud -> InfoPantalla(
        "Solicitud", listOf("HU-D06", "HU-D08", "HU-D09", "HU-D32", "HU-E06", "HU-E13"),
        listOf(Destino("Pagar", Pago(ruta.id)), Destino("Rechazar con motivo", MotivoRechazo(ruta.id)))
    )
    is Pago -> InfoPantalla(
        "Pago", listOf("HU-D09", "HU-D16"),
        listOf(Destino("Confirmar pago", PagoExitoso(ruta.referencia)))
    )
    is PagoExitoso -> InfoPantalla(
        "Pago confirmado", listOf("HU-D09", "HU-D16"),
        listOf(Destino("Ver reserva", DetalleReserva("r1")))
    )
    MisReservas -> InfoPantalla(
        "Mis reservas", listOf("HU-D08", "HU-D10", "HU-D32"),
        listOf(
            Destino("Solicitud en revisión", DetalleSolicitud("s1")),
            Destino("Reserva confirmada", DetalleReserva("r1")),
            Destino("Reserva finalizada", DetalleReserva("r2")),
            Destino("Reembolsos", Reembolsos)
        )
    )
    is DetalleReserva -> InfoPantalla(
        "Reserva", listOf("HU-D10", "HU-D11", "HU-D13", "HU-D29", "HU-D31", "HU-E07", "HU-E08"),
        listOf(
            Destino("Cancelar reserva", CancelarReserva(ruta.id)),
            Destino("Publicar transferencia", PublicarTransferencia(ruta.id)),
            Destino("Cómo llegar", Ruta("b1")),
            Destino("Registrar llegada", CheckIn(ruta.id)),
            Destino("Calificar", Resena(ruta.id))
        )
    )
    is CancelarReserva -> InfoPantalla(
        "Cancelar reserva", listOf("HU-D11"),
        listOf(Destino("Ver reembolso", DetalleReembolso("ref1")))
    )
    Reembolsos -> InfoPantalla(
        "Reembolsos", listOf("HU-D12", "HU-E09"),
        listOf(Destino("Ver reembolso", DetalleReembolso("ref1")))
    )
    is DetalleReembolso -> InfoPantalla("Reembolso", listOf("HU-D11", "HU-D12", "HU-D17", "HU-E09"))

    is PublicarTransferencia -> InfoPantalla(
        "Publicar transferencia", listOf("HU-D13"),
        listOf(Destino("Ver publicación", DetalleTransferencia("t2")))
    )
    Transferencias -> InfoPantalla(
        "Reservas transferibles", listOf("HU-D14"),
        listOf(Destino("Ver publicación", DetalleTransferencia("t1")), Destino("Mis transferencias", MisTransferencias))
    )
    is DetalleTransferencia -> InfoPantalla(
        "Transferencia", listOf("HU-D13", "HU-D14", "HU-D15", "HU-D16", "HU-D17", "HU-D18", "HU-E10"),
        listOf(
            Destino("Ver establecimiento", DetalleEstablecimiento("b1")),
            Destino("Hablar con quien publicó", Chat("c1")),
            Destino("Pagar transferencia", Pago(ruta.id)),
            Destino("Rechazar con motivo", MotivoTransferencia(ruta.id)),
            Destino("Ver reembolso", DetalleReembolso("ref1"))
        )
    )
    MisTransferencias -> InfoPantalla(
        "Mis transferencias", listOf("HU-D17"),
        listOf(Destino("Ver transferencia", DetalleTransferencia("t1")))
    )

    Mensajes -> InfoPantalla(
        "Mensajes", listOf("HU-D18", "HU-D20", "HU-D22", "HU-D25", "HU-E11"),
        listOf(
            Destino("Chat con Camila", Chat("c1")),
            Destino("Chat con Distrito Activo", Chat("c2")),
            Destino("Nuevo chat", NuevoChat),
            Destino("Crear comunidad", CrearComunidad),
            Destino("Comunidad Bogotá se mueve", Comunidad("g1"))
        )
    )
    is Chat -> InfoPantalla(
        "Conversación", listOf("HU-D18", "HU-D19", "HU-D23", "HU-E11"),
        listOf(Destino("Reportar mensaje", Reportar("mensaje")))
    )
    NuevoChat -> InfoPantalla(
        "Nuevo chat", listOf("HU-D18"),
        listOf(Destino("Abrir conversación", Chat("c1")))
    )
    CrearComunidad -> InfoPantalla(
        "Crear comunidad", listOf("HU-D20"),
        listOf(Destino("Ver comunidad creada", Comunidad("g1")))
    )
    is Comunidad -> InfoPantalla(
        "Comunidad", listOf("HU-D20", "HU-D21", "HU-D22", "HU-D23", "HU-D24", "HU-D25"),
        listOf(
            Destino("Chat del grupo", Chat(ruta.id)),
            Destino("Editar comunidad", EditarComunidad(ruta.id)),
            Destino("Invitar integrantes", Invitar(ruta.id)),
            Destino("Asignar sucesor", Sucesion(ruta.id))
        )
    )
    is EditarComunidad -> InfoPantalla(
        "Editar comunidad", listOf("HU-D24"),
        listOf(Destino("Invitar integrantes", Invitar(ruta.id)), Destino("Asignar sucesor", Sucesion(ruta.id)))
    )
    is Invitar -> InfoPantalla(
        "Invitar integrantes", listOf("HU-D21", "HU-D24", "HU-D26"),
        listOf(Destino("Elegir desde contactos", Contactos), Destino("Permiso de contactos", Permiso("contactos")))
    )
    is Sucesion -> InfoPantalla("Sucesión de la comunidad", listOf("HU-D24", "HU-D25"))
    Contactos -> InfoPantalla("Contactos", listOf("HU-D26"))

    is Permiso -> InfoPantalla("Permiso de ${ruta.tipo}", listOf("HU-D26", "HU-D27", "HU-D29", "HU-D30"))
    is Ruta -> InfoPantalla(
        "Cómo llegar", listOf("HU-D27", "HU-D28"),
        listOf(Destino("Elegir origen", Origen), Destino("Abrir brújula", Brujula(ruta.negocioId)))
    )
    is Brujula -> InfoPantalla("Brújula", listOf("HU-D28"))
    is CheckIn -> InfoPantalla(
        "Registrar llegada", listOf("HU-D29", "HU-D30"),
        listOf(Destino("Permiso de ubicación", Permiso("ubicacion")), Destino("Agregar fotografía", FotoLlegada(ruta.reservaId)))
    )
    is FotoLlegada -> InfoPantalla(
        "Fotografía de llegada", listOf("HU-D30"),
        listOf(Destino("Permiso de cámara", Permiso("camara")))
    )
    is Resena -> InfoPantalla("Calificar", listOf("HU-D31"))
    Notificaciones -> InfoPantalla(
        "Notificaciones", listOf("HU-C07", "HU-C08"),
        listOf(
            Destino("Solicitud en revisión", DetalleSolicitud("s1")),
            Destino("Reserva confirmada", DetalleReserva("r1")),
            Destino("Transferencia", DetalleTransferencia("t1"))
        )
    )
    is Reportar -> InfoPantalla("Reportar incidencia", listOf("HU-C08"))
    EditarPerfil -> InfoPantalla(
        "Editar perfil", listOf("HU-C05"),
        listOf(Destino("Permiso de cámara", Permiso("camara")))
    )
    Perfil -> InfoPantalla("Perfil", listOf("HU-C05", "HU-C06"))

    Gestion -> InfoPantalla(
        "Gestión", listOf("HU-E06", "HU-E07", "HU-E10", "HU-E13"),
        listOf(
            Destino("Solicitud por revisar", DetalleSolicitud("s1")),
            Destino("Agenda", Agenda),
            Destino("Transferencia por aprobar", DetalleTransferencia("t1"))
        )
    )
    Agenda -> InfoPantalla(
        "Agenda", listOf("HU-E07"),
        listOf(Destino("Ver reserva", DetalleReserva("r1")))
    )
    is MotivoRechazo -> InfoPantalla("Motivo de rechazo", listOf("HU-E06"))
    is MotivoTransferencia -> InfoPantalla("Motivo de rechazo", listOf("HU-E10"))
    Servicios -> InfoPantalla(
        "Servicios", listOf("HU-E02", "HU-E03", "HU-E04", "HU-E05"),
        listOf(
            Destino("Editar ficha del negocio", EditarNegocio),
            Destino("Nuevo servicio", EditarServicio("nuevo")),
            Destino("Editar servicio", EditarServicio("a1")),
            Destino("Horarios y capacidad", HorariosServicio("a1")),
            Destino("Condiciones", Condiciones("a1"))
        )
    )
    is EditarServicio -> InfoPantalla(
        "Servicio", listOf("HU-E03"),
        listOf(Destino("Horarios y capacidad", HorariosServicio(ruta.id)), Destino("Condiciones", Condiciones(ruta.id)))
    )
    is HorariosServicio -> InfoPantalla("Horarios y capacidad", listOf("HU-E04"))
    is Condiciones -> InfoPantalla("Condiciones", listOf("HU-E05"))
    Pagos -> InfoPantalla(
        "Pagos", listOf("HU-E08"),
        listOf(Destino("Reserva pagada", DetalleReserva("r1")), Destino("Reembolsos", Reembolsos))
    )
    ValidacionNegocio -> InfoPantalla(
        "Estado de validación", listOf("HU-C02", "HU-E01"),
        listOf(Destino("Corregir datos", EditarNegocio))
    )
    EditarNegocio -> InfoPantalla(
        "Ficha del negocio", listOf("HU-E01", "HU-E02"),
        listOf(Destino("Permiso de cámara", Permiso("camara")))
    )
    ResenasNegocio -> InfoPantalla(
        "Reseñas", listOf("HU-E12"),
        listOf(Destino("Responder reseña", ResponderResena("res1")))
    )
    is ResponderResena -> InfoPantalla("Responder reseña", listOf("HU-E12"))

    Validaciones -> InfoPantalla(
        "Negocios por validar", listOf("HU-A01", "HU-A02"),
        listOf(Destino("Movimiento Chapinero", DetalleValidacion("b2")))
    )
    is DetalleValidacion -> InfoPantalla("Validación de negocio", listOf("HU-A01", "HU-A02"))
    Reportes -> InfoPantalla(
        "Reportes", listOf("HU-A03", "HU-A04"),
        listOf(Destino("Reporte pendiente", DetalleReporte("rep1")))
    )
    is DetalleReporte -> InfoPantalla("Detalle del reporte", listOf("HU-A03", "HU-A04"))

    else -> InfoPantalla("SportHub", emptyList())
}
