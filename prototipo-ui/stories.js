const STORIES = [
  {
    "id": "HU-C01",
    "title": "Registrarse como deportista",
    "goal": "Como visitante mayor de edad, quiero crear una cuenta de deportista para reservar actividades y participar en comunidades.",
    "flow": [
      "Abre el registro y elige Deportista.",
      "Ingresa nombre, correo, contraseña y datos para declarar su edad.",
      "Revisa y envía el formulario.",
      "El sistema valida los datos y crea la cuenta con rol Deportista.",
      "Se muestra confirmación y acceso al inicio de sesión."
    ],
    "alternatives": "Correo ya registrado: se informa y no se duplica la cuenta.; Edad inferior a 18 años o campos inválidos: se impide el registro y se identifican los campos.; Fallo simulado: se conserva lo necesario para corregir y reintentar, sin mostrar la contraseña.",
    "routes": [
      "register",
      "registerAthlete",
      "login"
    ]
  },
  {
    "id": "HU-C02",
    "title": "Registrar un establecimiento",
    "goal": "Como representante de un establecimiento, quiero crear su cuenta e identificar el negocio para solicitar su validación antes de ofrecer servicios.",
    "flow": [
      "Elige registro de Establecimiento.",
      "Ingresa datos de cuenta y del negocio, incluido NIT y ubicación.",
      "Selecciona revisión administrativa o consulta simulada disponible.",
      "Envía la solicitud.",
      "El sistema registra el estado de validación y muestra los pasos siguientes."
    ],
    "alternatives": "Datos incompletos: se señalan antes de enviar.; Resultado simulado no concluyente: se informa que la validación está pendiente.; Datos duplicados: se solicita revisión y no se publica otra ficha automáticamente.",
    "routes": [
      "register",
      "registerBusiness",
      "login",
      "businessValidation"
    ]
  },
  {
    "id": "HU-C03",
    "title": "Iniciar sesión según el rol",
    "goal": "Como usuario registrado, quiero autenticarme para acceder a las funciones de mi cuenta.",
    "flow": [
      "Abre inicio de sesión.",
      "Introduce sus credenciales ficticias.",
      "El sistema valida la cuenta y obtiene su rol.",
      "Abre la interfaz de ese rol y muestra las restricciones de validación del negocio si corresponden."
    ],
    "alternatives": "Credenciales incorrectas: se muestra error sin abrir sesión.; Cuenta de demostración inactiva: se informa que no tiene acceso.; Servicio simulado no disponible: se ofrece reintento.",
    "routes": [
      "login",
      "explore",
      "manage",
      "validations"
    ]
  },
  {
    "id": "HU-C04",
    "title": "Recuperar el acceso",
    "goal": "Como usuario registrado, quiero solicitar recuperación de contraseña para volver a utilizar mi cuenta.",
    "flow": [
      "Elige recuperar acceso.",
      "Introduce el correo.",
      "El sistema muestra confirmación de solicitud.",
      "En el recorrido de demostración abre una recuperación válida.",
      "Define una nueva contraseña y regresa al inicio de sesión."
    ],
    "alternatives": "Correo mal formado: se solicita corregirlo.; Enlace simulado vencido: permite solicitar otro.; El mensaje de solicitud no revela si existe una cuenta de otra persona.",
    "routes": [
      "recover",
      "recoverLink",
      "newPassword",
      "login"
    ]
  },
  {
    "id": "HU-C05",
    "title": "Actualizar el perfil",
    "goal": "Como usuario autenticado, quiero consultar y editar mis datos y fotografía para mantener mi información vigente.",
    "flow": [
      "Consulta los datos de su perfil.",
      "Edita los campos permitidos.",
      "Opcionalmente selecciona una foto de galería o toma una fotografía.",
      "Previsualiza y guarda.",
      "El sistema confirma y muestra el perfil actualizado."
    ],
    "alternatives": "Cancela la selección: mantiene la foto anterior.; Permiso necesario denegado: permite continuar sin cambiar la fotografía.; Datos inválidos o fallo al guardar: no informa éxito y conserva la edición recuperable.",
    "routes": [
      "profile",
      "profileEdit"
    ]
  },
  {
    "id": "HU-C06",
    "title": "Cerrar sesión",
    "goal": "Como usuario autenticado, quiero finalizar mi sesión para evitar que otra persona use mi cuenta.",
    "flow": [
      "Abre las opciones de cuenta.",
      "Selecciona cerrar sesión.",
      "El sistema termina la sesión.",
      "Regresa al inicio de sesión y restringe las pantallas privadas."
    ],
    "alternatives": "Fallo remoto simulado: el cierre local no debe mantener acceso privado.; Intento de regresar con Atrás: se solicita iniciar sesión.",
    "routes": [
      "profile",
      "welcome"
    ]
  },
  {
    "id": "HU-D01",
    "title": "Explorar categorías",
    "goal": "Como deportista, quiero explorar deportes y bienestar para encontrar una actividad de mi interés.",
    "flow": [
      "Abre Explorar.",
      "Consulta las categorías deportivas y de bienestar.",
      "Selecciona una categoría.",
      "Visualiza los servicios publicados de esa categoría.",
      "Abre una opción para conocer su detalle."
    ],
    "alternatives": "Categoría sin oferta: se muestra estado vacío y permite cambiar categoría.; Error de carga: permite reintentar.",
    "routes": [
      "explore",
      "search"
    ]
  },
  {
    "id": "HU-D02",
    "title": "Buscar y filtrar actividades",
    "goal": "Como deportista, quiero buscar actividades por ubicación, horario, precio y calificación para comparar opciones adecuadas a mis preferencias.",
    "flow": [
      "Escribe una búsqueda o abre filtros.",
      "Define el centro de búsqueda: ubicación autorizada o punto de referencia manual; la demo permite elegir un punto de referencia o coordenadas manuales.",
      "Selecciona radio de 3, 5, 10 o 20 km, o Sin límite de distancia, y los criterios de fecha, horario, actividad, precio y calificación.",
      "Aplica los filtros combinados y consulta actividades con disponibilidad; buscar no aparta cupos.",
      "Revisa resultados, distancia geográfica al centro y criterios activos.",
      "Abre el mapa para consultar el círculo y los establecimientos coincidentes.",
      "Modifica o limpia filtros y abre un resultado."
    ],
    "alternatives": "Intervalo de precio u horario inválido: solicita corregirlo.; Sin coincidencias: permite quitar filtros.; Ubicación del teléfono no disponible: permite elegir ubicación manual.",
    "routes": [
      "search",
      "filters",
      "map",
      "origin"
    ]
  },
  {
    "id": "HU-D03",
    "title": "Consultar establecimiento y servicio",
    "goal": "Como deportista, quiero ver información y condiciones de una actividad para decidir si satisface mis necesidades antes de reservar.",
    "flow": [
      "Abre un resultado.",
      "Consulta descripción, fotos, ubicación y contacto.",
      "Consulta servicios, modalidad, precio y reseñas.",
      "Lee las condiciones de reserva, cancelación y transferencia.",
      "Accede a disponibilidad o a ayuda."
    ],
    "alternatives": "Fotografía ausente: se muestra un recurso de sustitución.; Servicio retirado: se informa y no permite solicitarlo.; Sin reseñas: se indica que no hay opiniones registradas.",
    "routes": [
      "activity",
      "business"
    ]
  },
  {
    "id": "HU-D04",
    "title": "Consultar horarios y cupos",
    "goal": "Como deportista, quiero consultar disponibilidad por fecha para elegir un horario que pueda solicitar.",
    "flow": [
      "Abre su disponibilidad.",
      "Selecciona una fecha.",
      "Consulta inicio, fin y disponibilidad de cada horario.",
      "Distingue espacio completo de cupos individuales.",
      "Selecciona un horario o consulta la posibilidad de fila."
    ],
    "alternatives": "Fecha sin oferta: permite elegir otra.; Horario ocupado: no se anuncia confirmación inmediata; puede ofrecer espera.; La disponibilidad cambia antes de solicitar: se vuelve a comprobar.",
    "routes": [
      "slots"
    ]
  },
  {
    "id": "HU-D05",
    "title": "Explorar establecimientos en el mapa",
    "goal": "Como deportista, quiero ver establecimientos geográficamente para comparar su ubicación.",
    "flow": [
      "Abre el mapa.",
      "Selecciona una zona o autoriza ubicación cuando corresponda.",
      "Consulta marcadores de establecimientos.",
      "Selecciona uno y ve su resumen.",
      "Accede al detalle o la ruta."
    ],
    "alternatives": "Ubicación denegada: centra el mapa en una zona seleccionable.; Mapa no disponible: ofrece volver al listado.; Coordenadas ausentes: no coloca un marcador ficticio del negocio.",
    "routes": [
      "map",
      "business",
      "origin"
    ]
  },
  {
    "id": "HU-D06",
    "title": "Solicitar un espacio completo",
    "goal": "Como deportista, quiero solicitar una cancha o espacio completo para realizar mi actividad en el horario elegido.",
    "flow": [
      "Selecciona fecha y horario.",
      "Revisa precio, duración y condiciones.",
      "Confirma que desea solicitar.",
      "El sistema comprueba conflictos del usuario y estado del recurso.",
      "Registra la solicitud en orden de llegada y muestra si espera aceptación o está en fila."
    ],
    "alternatives": "Cruce con una reserva vigente del usuario: se rechaza la nueva solicitud.; Recurso sin disponibilidad inmediata: informa que la solicitud solo entra en espera si el usuario lo acepta.; Envío repetido: no genera solicitudes duplicadas.",
    "routes": [
      "slots",
      "requestSummary",
      "request"
    ]
  },
  {
    "id": "HU-D07",
    "title": "Solicitar un cupo en una clase",
    "goal": "Como deportista, quiero reservar un cupo en una clase para participar en una sesión grupal.",
    "flow": [
      "Selecciona una sesión.",
      "Revisa inicio, fin, cupos, precio y condiciones.",
      "Solicita un cupo.",
      "El sistema revisa cruces y capacidad.",
      "Registra la solicitud para aprobación o informa que queda en espera."
    ],
    "alternatives": "Clase completa: solo ofrece fila, sin prometer un cupo.; Cruce de horario: impide continuar.; Solicitud duplicada para la misma sesión: se informa y muestra la existente.",
    "routes": [
      "slots",
      "requestSummary",
      "request"
    ]
  },
  {
    "id": "HU-D08",
    "title": "Consultar estado de solicitudes",
    "goal": "Como deportista, quiero seguir mis solicitudes para saber cuándo debo pagar o esperar.",
    "flow": [
      "Abre Mis solicitudes.",
      "Consulta estado, establecimiento y horario.",
      "Abre una solicitud.",
      "Si fue aceptada, ve fecha límite y acceso al pago.",
      "Si cambió su estado, consulta el motivo y la acción siguiente.",
      "Si aún no pagó, puede retirar su solicitud tras confirmar; el sistema libera su turno y propone al siguiente. Si ya pagó, se utiliza Cancelar reserva (HU-D11)."
    ],
    "alternatives": "Solicitud rechazada: muestra motivo y no habilita pago.; Solicitud vencida: informa pérdida del turno.; Notificación antigua: al abrirla presenta el estado actual.",
    "routes": [
      "reservations",
      "request"
    ]
  },
  {
    "id": "HU-D09",
    "title": "Pagar una reserva aceptada",
    "goal": "Como deportista, quiero realizar el pago simulado dentro del plazo para confirmar mi reserva.",
    "flow": [
      "Abre el pago desde la solicitud.",
      "Revisa servicio, importe, condiciones y vencimiento.",
      "Selecciona un medio de demostración e ingresa datos ficticios.",
      "Confirma la operación.",
      "El sistema comprueba vigencia, registra el resultado y confirma la reserva si fue exitoso.",
      "Notifica al deportista y al establecimiento."
    ],
    "alternatives": "Pago rechazado: no confirma reserva y permite reintentar solo dentro del plazo.; Plazo vencido: rechaza el pago y activa el avance de fila.; Doble pulsación: no registra dos pagos ni dos reservas.",
    "routes": [
      "request",
      "payment",
      "paymentSuccess"
    ]
  },
  {
    "id": "HU-D10",
    "title": "Consultar reservas e historial",
    "goal": "Como deportista, quiero ver mis reservas próximas y anteriores para organizarme y consultar el resultado de mis actividades.",
    "flow": [
      "Abre Mis reservas.",
      "Distingue próximas, canceladas y anteriores.",
      "Selecciona una reserva.",
      "Consulta horario, pago y estado de cierre.",
      "Accede a acciones válidas como ruta, cancelación, transferencia o reseña."
    ],
    "alternatives": "Sin reservas: ofrece volver al catálogo.; El horario terminó pero falta cierre: muestra esa diferencia.; Reserva de otro titular: no muestra datos privados.",
    "routes": [
      "reservations",
      "reservation"
    ]
  },
  {
    "id": "HU-D11",
    "title": "Cancelar una reserva",
    "goal": "Como deportista, quiero cancelar conforme a las condiciones aceptadas para informar que no asistiré y conocer la devolución aplicable.",
    "flow": [
      "Abre Cancelar.",
      "Consulta plazo, condiciones y devolución calculada.",
      "Confirma o desiste.",
      "El sistema revisa nuevamente la elegibilidad.",
      "Registra cancelación y, si corresponde, reembolso pendiente.",
      "Notifica al establecimiento y muestra el resultado."
    ],
    "alternatives": "Fuera de plazo o condición no cumplida: explica que no procede.; Desiste: conserva la reserva.; Cancelación ya registrada: no duplica la devolución.",
    "routes": [
      "reservation",
      "cancel",
      "refund"
    ]
  },
  {
    "id": "HU-D12",
    "title": "Consultar un reembolso",
    "goal": "Como deportista, quiero consultar la devolución simulada para saber cuánto y a qué medio se devolvió el pago.",
    "flow": [
      "Abre el detalle de devolución desde la reserva o notificación.",
      "Consulta motivo, importe y medio de origen enmascarado.",
      "Distingue pendiente, realizada o fallida.",
      "Cuando el establecimiento la realiza, consulta fecha y comprobante ficticio."
    ],
    "alternatives": "No corresponde devolución: muestra la condición aplicable.; Operación pendiente o fallida: no afirma que el dinero fue devuelto.; Datos de otra cuenta: se deniega acceso.",
    "routes": [
      "refunds",
      "refund"
    ]
  },
  {
    "id": "HU-D32",
    "title": "Consultar mi turno de reserva",
    "goal": "Como deportista, quiero conocer mi posición y estado en la fila para saber si debo esperar o pagar.",
    "flow": [
      "Abre el seguimiento de su solicitud.",
      "Consulta posición y estado.",
      "Si avanza, ve que espera aceptación del establecimiento.",
      "Después de la aceptación, consulta vencimiento y acceso a pago.",
      "Recibe cambios por notificaciones."
    ],
    "alternatives": "No hay disponibilidad: permanece en espera sin cronómetro de pago.; Turno vencido: muestra pérdida de prioridad.; Se actualiza la fila: refresca posición sin revelar identidades de otros solicitantes.",
    "routes": [
      "reservations",
      "request"
    ]
  },
  {
    "id": "HU-D13",
    "title": "Publicar una reserva transferible",
    "goal": "Como propietario de una reserva, quiero publicarla para que otro deportista la tome para cederla bajo las condiciones del establecimiento.",
    "flow": [
      "Abre la reserva y elige Transferir.",
      "Consulta condiciones y datos que serán públicos.",
      "Confirma publicación.",
      "El sistema verifica elegibilidad y crea una publicación.",
      "Muestra estado publicado y acceso al seguimiento."
    ],
    "alternatives": "No transferible o fuera de condiciones: explica la restricción.; Publicación existente: abre la existente sin duplicarla.; Cancelación o actividad finalizada: no admite nueva publicación.",
    "routes": [
      "reservation",
      "publishTransfer",
      "transfer"
    ]
  },
  {
    "id": "HU-D14",
    "title": "Explorar reservas transferibles",
    "goal": "Como deportista, quiero consultar reservas publicadas para transferencia para encontrar una actividad que pueda tomar.",
    "flow": [
      "Abre Transferencias.",
      "Consulta publicaciones activas.",
      "Filtra por actividad, ubicación o fecha.",
      "Abre detalle con horario, importe y condiciones.",
      "Accede a solicitar la transferencia.",
      "También puede abrir la publicación desde el establecimiento o su horario y contactar a quien la publicó."
    ],
    "alternatives": "Oferta vacía: muestra estado sin publicaciones.; Publicación retirada o transferida: no permite solicitarla.; Información financiera del propietario: no se revela.",
    "routes": [
      "transfers",
      "transfer",
      "business",
      "slots"
    ]
  },
  {
    "id": "HU-D15",
    "title": "Aceptar tomar una reserva publicada",
    "goal": "Como deportista interesado, quiero solicitar tomar una reserva transferible para iniciar el proceso de adquisición.",
    "flow": [
      "Revisa la publicación.",
      "Acepta condiciones y expresa que quiere tomarla.",
      "El sistema verifica cruces y vigencia.",
      "Registra interés para revisión del establecimiento.",
      "Muestra pendiente de aprobación, sin habilitar todavía el pago."
    ],
    "alternatives": "Cruce de horario: se rechaza la solicitud.; Publicación ya asignada: informa indisponibilidad.; Solicitud duplicada: muestra el estado existente.",
    "routes": [
      "transfer"
    ]
  },
  {
    "id": "HU-D16",
    "title": "Pagar una transferencia aprobada",
    "goal": "Como destinatario de una transferencia, quiero pagar de forma simulada después de la aprobación para completar el cambio de titular de la reserva.",
    "flow": [
      "Abre la transferencia aprobada.",
      "Consulta importe y condiciones.",
      "Selecciona medio ficticio y confirma pago.",
      "El sistema revalida vigencia y ausencia de cruces.",
      "Con pago exitoso completa una sola vez el cambio de titular.",
      "Registra devolución aplicable al propietario y notifica a las partes."
    ],
    "alternatives": "Pago fallido: conserva titular original.; Aprobación vencida: conserva al propietario original, avanza al siguiente interesado y solicita nueva aprobación del establecimiento.; Operación repetida: no duplica pago, titularidad ni devolución.",
    "routes": [
      "transfer",
      "payment",
      "paymentSuccess"
    ]
  },
  {
    "id": "HU-D17",
    "title": "Seguir una transferencia",
    "goal": "Como participante de una transferencia, quiero consultar su avance para conocer las acciones y aprobaciones pendientes.",
    "flow": [
      "Abre Mis transferencias o una notificación.",
      "Consulta reserva, rol propio y estado.",
      "Identifica si falta aceptación, aprobación, pago o devolución.",
      "Accede a la acción habilitada.",
      "Actualiza el detalle y consulta el resultado."
    ],
    "alternatives": "Transferencia rechazada: muestra motivo sin cambiar titular.; Notificación antigua: abre estado actual.; Persona ajena: se impide acceso a detalles privados.",
    "routes": [
      "myTransfers",
      "transfer",
      "refund"
    ]
  },
  {
    "id": "HU-D18",
    "title": "Conversar con otros deportistas",
    "goal": "Como deportista, quiero iniciar y mantener un chat individual para coordinar actividades con otras personas.",
    "flow": [
      "Selecciona al destinatario desde su perfil o búsqueda permitida.",
      "Abre o recupera la conversación existente.",
      "Escribe y envía un mensaje.",
      "Consulta el estado de envío y las respuestas."
    ],
    "alternatives": "Mensaje vacío: no se envía.; Error de envío: queda señalado y permite reintentar sin duplicarlo.; Destinatario no disponible o restringido: informa que no puede iniciar el contacto.",
    "routes": [
      "messages",
      "newChat",
      "chat",
      "transfer"
    ]
  },
  {
    "id": "HU-D19",
    "title": "Contactar al establecimiento",
    "goal": "Como deportista, quiero enviar consultas a un establecimiento para obtener información o ayuda sobre actividades y reservas.",
    "flow": [
      "Abre Contactar desde la ficha o reserva.",
      "Consulta el nombre del establecimiento destinatario.",
      "Escribe su consulta.",
      "El sistema registra el mensaje en el canal correspondiente.",
      "Consulta la respuesta del establecimiento.",
      "Si prefiere llamar, revisa el teléfono y abre el marcador; decide allí si inicia la llamada."
    ],
    "alternatives": "Negocio no disponible: se informa el estado.; Fallo de envío: permite reintentar.; Mensaje vacío: no lo registra.",
    "routes": [
      "business",
      "chat"
    ]
  },
  {
    "id": "HU-D20",
    "title": "Crear una comunidad privada",
    "goal": "Como deportista, quiero crear un grupo privado para organizar actividades con personas invitadas.",
    "flow": [
      "Abre Comunidades y elige Crear.",
      "Ingresa nombre y descripción.",
      "Confirma su carácter privado.",
      "Guarda el grupo.",
      "El sistema lo registra como creador y administrador de integrantes."
    ],
    "alternatives": "Nombre vacío: solicita completarlo.; Fallo de creación: no muestra un grupo exitoso.; Doble envío: no crea grupos duplicados.",
    "routes": [
      "messages",
      "groupCreate",
      "group"
    ]
  },
  {
    "id": "HU-D21",
    "title": "Invitar integrantes",
    "goal": "Como creador de una comunidad, quiero invitar deportistas para incorporar personas a mi grupo privado.",
    "flow": [
      "Abre administración de integrantes.",
      "Busca o selecciona deportistas.",
      "Envía invitaciones.",
      "El sistema registra invitaciones pendientes y notifica a los destinatarios.",
      "Consulta el estado de cada invitación."
    ],
    "alternatives": "Destinatario ya integrante: no genera otra invitación.; Invitación pendiente: muestra la existente.; Persona sin rol de deportista: no la incorpora por este flujo.",
    "routes": [
      "group",
      "invite"
    ]
  },
  {
    "id": "HU-D22",
    "title": "Responder una invitación",
    "goal": "Como deportista invitado, quiero aceptar o rechazar una comunidad para decidir a qué grupos pertenezco.",
    "flow": [
      "Abre la invitación.",
      "Consulta nombre del grupo y quién invita.",
      "Acepta o rechaza.",
      "El sistema actualiza la invitación.",
      "Si acepta, habilita la comunidad; si rechaza, no lo incorpora."
    ],
    "alternatives": "Invitación retirada o grupo no disponible: no permite incorporarse.; Respuesta repetida: conserva un único resultado.; Invitación ajena: no permite responder.",
    "routes": [
      "messages",
      "group"
    ]
  },
  {
    "id": "HU-D23",
    "title": "Conversar en una comunidad",
    "goal": "Como integrante de una comunidad, quiero participar en el chat privado del grupo para coordinar actividades con sus integrantes.",
    "flow": [
      "Abre su comunidad.",
      "Consulta los mensajes permitidos.",
      "Redacta y envía un mensaje.",
      "El sistema muestra autor y estado de envío.",
      "Consulta respuestas del grupo."
    ],
    "alternatives": "Ha sido retirado o salió: no puede enviar ni acceder al grupo privado.; Mensaje vacío o envío fallido: se informa sin duplicar.; Intento de acceso por enlace sin membresía: se deniega.",
    "routes": [
      "group",
      "chat"
    ]
  },
  {
    "id": "HU-D24",
    "title": "Administrar la comunidad",
    "goal": "Como creador de una comunidad, quiero editar sus datos y gestionar integrantes para mantener organizado mi grupo.",
    "flow": [
      "Abre administración.",
      "Edita nombre o descripción, o consulta integrantes.",
      "Selecciona una modificación o retiro de integrante.",
      "Confirma la acción.",
      "El sistema actualiza el grupo y sus accesos.",
      "Si desea ceder la administración, selecciona un integrante y envía la propuesta de sucesión.",
      "El integrante acepta o rechaza desde el grupo; solo la aceptación transfiere la administración y permite la salida del creador anterior."
    ],
    "alternatives": "Actor sin permisos: no modifica el grupo.; Datos inválidos: solicita corrección.; Si el sucesor rechaza o todavía no acepta, el creador conserva su responsabilidad y no puede salir.",
    "routes": [
      "group",
      "groupEdit",
      "invite",
      "succession"
    ]
  },
  {
    "id": "HU-D25",
    "title": "Salir de una comunidad",
    "goal": "Como integrante de una comunidad, quiero abandonar el grupo para dejar de participar en sus conversaciones.",
    "flow": [
      "Abre opciones de comunidad.",
      "Si administra el grupo, asigna un sucesor y espera su aceptación; después selecciona Salir.",
      "Consulta la consecuencia y confirma.",
      "El sistema retira la membresía.",
      "Regresa al listado sin acceso al grupo."
    ],
    "alternatives": "Cancela confirmación: permanece.; Ya no es integrante: muestra estado actual.; Es creador: informa que necesita resolver la administración antes de salir.",
    "routes": [
      "group",
      "succession",
      "messages"
    ]
  },
  {
    "id": "HU-D26",
    "title": "Invitar desde contactos",
    "goal": "Como deportista, quiero seleccionar contactos del teléfono para invitar personas a una actividad o comunidad.",
    "flow": [
      "Elige invitar desde contactos.",
      "La aplicación explica el uso y solicita acceso cuando corresponda.",
      "Selecciona explícitamente personas.",
      "Revisa el contenido y confirma el envío simulado.",
      "Consulta el resultado."
    ],
    "alternatives": "Permiso denegado: puede volver o usar búsqueda manual.; Agenda vacía: muestra estado vacío.; Contacto sin cuenta: representa una invitación externa ficticia, sin incorporarlo automáticamente al grupo.",
    "routes": [
      "invite",
      "permission",
      "contacts"
    ]
  },
  {
    "id": "HU-D27",
    "title": "Consultar una ruta",
    "goal": "Como deportista, quiero ver una ruta hacia el establecimiento para orientarme para llegar a mi actividad.",
    "flow": [
      "Abre Cómo llegar.",
      "Autoriza ubicación o elige origen manual.",
      "Confirma destino.",
      "Consulta la ruta y referencia del establecimiento.",
      "Puede volver a la reserva o abrir la brújula."
    ],
    "alternatives": "Permiso denegado: admite origen manual.; Ruta no disponible: conserva dirección y destino.; Ubicación imprecisa: muestra la limitación sin afirmar llegada.",
    "routes": [
      "map",
      "permission",
      "route",
      "origin"
    ]
  },
  {
    "id": "HU-D28",
    "title": "Orientarse con brújula",
    "goal": "Como deportista, quiero consultar una dirección orientativa hacia el establecimiento para complementar la ruta del mapa.",
    "flow": [
      "Abre Brújula desde la ruta.",
      "La aplicación verifica recursos disponibles.",
      "Muestra dirección orientativa hacia el destino.",
      "El usuario consulta cambios de orientación.",
      "Puede regresar al mapa."
    ],
    "alternatives": "Sensor no disponible: informa y permite usar el mapa.; Sin ubicación: solicita definir origen o vuelve a ruta.; Lectura imprecisa: muestra indicación de precisión limitada.",
    "routes": [
      "route",
      "compass"
    ]
  },
  {
    "id": "HU-D29",
    "title": "Registrar llegada por ubicación",
    "goal": "Como deportista, quiero hacer check-in en mi actividad para registrar mi llegada al establecimiento.",
    "flow": [
      "Abre su reserva y elige check-in.",
      "Autoriza ubicación cuando sea necesario.",
      "El sistema compara su posición con el destino y la ventana aplicable.",
      "Si cumple las condiciones, registra llegada.",
      "Muestra resultado y opción de fotografía."
    ],
    "alternatives": "Fuera de rango: informa que no puede registrar llegada.; Permiso denegado o ubicación no disponible: no registra un check-in ficticiamente exitoso.; Check-in repetido: conserva el registro previo.",
    "routes": [
      "reservation",
      "permission",
      "checkin"
    ]
  },
  {
    "id": "HU-D30",
    "title": "Adjuntar fotografía de llegada",
    "goal": "Como deportista, quiero tomar una fotografía de check-in para acompañar el registro de mi llegada.",
    "flow": [
      "Elige agregar fotografía.",
      "Autoriza cámara si corresponde.",
      "Toma una foto y la previsualiza.",
      "Confirma o repite.",
      "El sistema la vincula al check-in y muestra resultado."
    ],
    "alternatives": "Permiso denegado: permite volver.; Cancela captura: no adjunta imagen.; Fallo al guardar: informa y no muestra una imagen como almacenada.",
    "routes": [
      "checkin",
      "permission",
      "photo"
    ]
  },
  {
    "id": "HU-D31",
    "title": "Calificar después de asistir",
    "goal": "Como deportista, quiero escribir una reseña y calificación para compartir mi experiencia en un establecimiento.",
    "flow": [
      "Abre la reserva terminada.",
      "Selecciona Calificar.",
      "Ingresa puntuación y comentario.",
      "Revisa y envía.",
      "El sistema valida elegibilidad y publica la reseña vinculada a la reserva."
    ],
    "alternatives": "Actividad sin cierre: explica que todavía no puede calificar.; Puntuación fuera del rango: solicita corregir.; Reseña ya existente: no duplica; edición queda sujeta a decisión posterior.",
    "routes": [
      "reservation",
      "review"
    ]
  },
  {
    "id": "HU-C07",
    "title": "Consultar notificaciones",
    "goal": "Como usuario autenticado, quiero recibir y consultar avisos de mis operaciones para saber cuándo debo actuar y qué cambió.",
    "flow": [
      "Abre la bandeja o un aviso.",
      "Consulta evento, fecha y operación relacionada.",
      "Abre el detalle actual.",
      "Marca el aviso como leído.",
      "Regresa a la bandeja actualizada."
    ],
    "alternatives": "Notificación antigua: el detalle muestra el estado vigente.; Permiso de notificación del sistema denegado: conserva bandeja interna.; Evento repetido: evita avisos duplicados de la misma transición.",
    "routes": [
      "notifications",
      "request",
      "reservation",
      "transfer"
    ]
  },
  {
    "id": "HU-C08",
    "title": "Reportar una incidencia",
    "goal": "Como usuario autenticado, quiero reportar contenido o comportamiento inapropiado para solicitar revisión de la plataforma.",
    "flow": [
      "Abre Reportar desde el elemento.",
      "Selecciona motivo y describe la situación.",
      "Revisa y envía.",
      "El sistema registra el reporte y su contexto.",
      "Muestra confirmación de recepción."
    ],
    "alternatives": "Descripción o motivo requerido ausente: solicita completar.; Elemento retirado: permite informar o muestra estado, según contexto.; Envío repetido: no crea duplicados de la misma acción.",
    "routes": [
      "report",
      "notifications"
    ]
  },
  {
    "id": "HU-E01",
    "title": "Consultar y corregir la validación",
    "goal": "Como establecimiento registrado, quiero conocer el resultado de mi validación y corregir datos para habilitar mi oferta cuando cumpla los requisitos.",
    "flow": [
      "Abre estado de validación.",
      "Consulta resultado y observaciones.",
      "Corrige los datos requeridos.",
      "Reenvía a revisión o ejecuta la consulta simulada habilitada.",
      "Consulta el nuevo estado."
    ],
    "alternatives": "Sin observaciones pendientes: muestra el estado actual.; Datos aún incompletos: no finaliza reenvío.; Consulta ficticia fallida: no declara aprobado el negocio.",
    "routes": [
      "profile",
      "businessValidation",
      "businessEdit"
    ]
  },
  {
    "id": "HU-E02",
    "title": "Editar la ficha del negocio",
    "goal": "Como establecimiento, quiero actualizar información, ubicación y fotografías para presentar mi oferta con datos vigentes.",
    "flow": [
      "Abre Mi establecimiento.",
      "Edita descripción, contacto y ubicación.",
      "Selecciona o toma fotografías.",
      "Previsualiza y guarda.",
      "El sistema actualiza los datos permitidos y su visibilidad según validación."
    ],
    "alternatives": "Ubicación inválida: solicita corrección.; Permiso de cámara denegado: permite conservar imágenes.; Intento de cambiar el NIT desde la ficha: no se permite; se informa que exige revisión de identidad.",
    "routes": [
      "services",
      "businessEdit"
    ]
  },
  {
    "id": "HU-E03",
    "title": "Gestionar servicios",
    "goal": "Como establecimiento validado, quiero crear y editar actividades o espacios para publicar lo que pueden reservar los deportistas.",
    "flow": [
      "Abre Servicios y elige crear o editar.",
      "Define nombre, categoría, modalidad, descripción, precio y duración.",
      "Configura espacio completo o clase con cupos.",
      "Revisa y guarda.",
      "Accede a horarios del servicio."
    ],
    "alternatives": "Precio o duración inválidos: no guarda.; Negocio no validado: no publica.; Cambio sobre servicio con reservas existentes: no altera compromisos sin tratamiento explícito.",
    "routes": [
      "services",
      "serviceEdit"
    ]
  },
  {
    "id": "HU-E04",
    "title": "Gestionar horarios y capacidad",
    "goal": "Como establecimiento, quiero configurar disponibilidad y cupos para controlar qué puede reservarse sin sobreasignación.",
    "flow": [
      "Abre calendario del servicio.",
      "Define fecha, inicio y fin.",
      "Asigna capacidad si es una clase o disponibilidad del espacio.",
      "Guarda y consulta el calendario.",
      "Bloquea horarios no comprometidos cuando corresponda."
    ],
    "alternatives": "Fin anterior o igual al inicio: no guarda.; Bloqueo que afecta reserva confirmada o turno retenido: advierte y no elimina el compromiso automáticamente.; Capacidad inferior a compromisos actuales: no aplica la reducción.",
    "routes": [
      "services",
      "schedule"
    ]
  },
  {
    "id": "HU-E05",
    "title": "Configurar condiciones",
    "goal": "Como establecimiento, quiero definir condiciones y plazo de pago para establecer reglas claras para mis reservas.",
    "flow": [
      "Abre Condiciones.",
      "Configura plazo máximo de pago, cancelación, porcentaje y plazo de devolución y transferibilidad.",
      "Añade otras condiciones visibles.",
      "Revisa los datos y guarda.",
      "Consulta cómo se presentan antes de reservar."
    ],
    "alternatives": "Plazo no positivo o devolución fuera de 0 a 100: no guarda.; Reglas contradictorias: solicita corregir.; Condiciones nuevas: se aplican a futuras solicitudes; las existentes conservan las condiciones que conoció el deportista al solicitar.",
    "routes": [
      "services",
      "policy"
    ]
  },
  {
    "id": "HU-E06",
    "title": "Aceptar o rechazar solicitudes",
    "goal": "Como establecimiento, quiero revisar la solicitud con prioridad para asignar mi disponibilidad de forma ordenada.",
    "flow": [
      "Abre solicitudes y selecciona la que tiene prioridad.",
      "Consulta deportista, horario y disponibilidad.",
      "Acepta o rechaza indicando motivo cuando corresponda.",
      "Si acepta, el sistema aparta el recurso e inicia el plazo configurado.",
      "Notifica al deportista con la acción de pago."
    ],
    "alternatives": "Otro usuario está con turno vigente de pago: no se permite sobreasignar el recurso.; Solicitud ya vencida o resuelta: muestra estado actual.; Rechazo: no habilita pago.",
    "routes": [
      "manage",
      "request",
      "reason"
    ]
  },
  {
    "id": "HU-E07",
    "title": "Consultar agenda y cerrar actividades",
    "goal": "Como establecimiento, quiero seguir mis reservas y confirmar la finalización para organizar la atención y liberar el espacio al terminar.",
    "flow": [
      "Abre agenda y consulta fecha y reserva.",
      "Revisa pago, llegada y horario.",
      "Al alcanzar la hora final, el sistema indica horario finalizado y avisa.",
      "Si el deportista continúa, el establecimiento envía aviso de salida.",
      "El establecimiento confirma el cierre de la actividad.",
      "El sistema registra el cierre y actualiza historial."
    ],
    "alternatives": "Hora final no alcanzada: no presenta fin de horario automático.; Horario finalizado sin cierre: mantiene ambos estados diferenciados.; Cierre repetido: no genera cierres ni avisos duplicados.",
    "routes": [
      "manage",
      "agenda",
      "reservation"
    ]
  },
  {
    "id": "HU-E08",
    "title": "Consultar pagos de reservas",
    "goal": "Como establecimiento, quiero ver los pagos simulados de mis actividades para distinguir reservas confirmadas de turnos pendientes.",
    "flow": [
      "Abre la reserva o panel de pagos.",
      "Consulta importe, estado y referencia ficticia.",
      "Distingue pendiente, exitoso, rechazado y vencido.",
      "Accede al detalle de la reserva.",
      "Consulta los avisos relacionados."
    ],
    "alternatives": "Pago fallido: no lo muestra como ingreso exitoso.; No existe pago: muestra pendiente sin comprobante inventado.; Negocio ajeno: restringe consulta.",
    "routes": [
      "payments",
      "reservation"
    ]
  },
  {
    "id": "HU-E09",
    "title": "Gestionar reembolsos",
    "goal": "Como establecimiento, quiero registrar las devoluciones simuladas aplicables para cumplir las condiciones de cancelación o transferencia.",
    "flow": [
      "Abre devoluciones pendientes.",
      "Consulta motivo, importe y condiciones aplicables.",
      "Confirma el reembolso simulado al medio de origen.",
      "El sistema registra éxito o fallo.",
      "Si es exitoso, notifica al deportista e informa el resultado."
    ],
    "alternatives": "No corresponde devolución: muestra la condición.; Reembolso fallido: sigue sin constar como realizado.; Ya realizado: evita un segundo registro de devolución.",
    "routes": [
      "refunds",
      "refund"
    ]
  },
  {
    "id": "HU-E10",
    "title": "Aprobar transferencias",
    "goal": "Como establecimiento, quiero revisar el cambio de titular solicitado para autorizarlo según mis condiciones.",
    "flow": [
      "Abre la solicitud de transferencia.",
      "Consulta propietario, interesado, horario y condiciones.",
      "Revisa elegibilidad.",
      "Aprueba o rechaza con motivo.",
      "El sistema notifica el resultado, aparta el turno de transferencia e inicia el plazo configurado para que el destinatario aprobado pague."
    ],
    "alternatives": "Condiciones incumplidas: rechaza con explicación.; Transferencia ya completada: no permite otra aprobación.; Aprobación concurrente para otro interesado: mantiene una sola asignación activa.",
    "routes": [
      "manage",
      "transfer",
      "transferReason"
    ]
  },
  {
    "id": "HU-E11",
    "title": "Responder consultas",
    "goal": "Como establecimiento, quiero contestar mensajes de deportistas para resolver dudas sobre mis servicios.",
    "flow": [
      "Abre bandeja de atención.",
      "Selecciona la conversación.",
      "Consulta la pregunta y contexto de reserva si está disponible.",
      "Redacta y envía respuesta.",
      "Consulta estado de envío."
    ],
    "alternatives": "Mensaje vacío: no envía.; Fallo de envío: permite reintentar sin duplicar.; Conversación de otro negocio: deniega acceso.",
    "routes": [
      "messages",
      "chat"
    ]
  },
  {
    "id": "HU-E12",
    "title": "Consultar y responder reseñas",
    "goal": "Como establecimiento, quiero atender opiniones sobre mis servicios para dar respuesta a las experiencias de los deportistas.",
    "flow": [
      "Abre Reseñas.",
      "Consulta puntuación, comentario y servicio asociado.",
      "Selecciona una reseña y redacta respuesta.",
      "Envía.",
      "La ficha muestra respuesta identificada como del establecimiento."
    ],
    "alternatives": "Sin reseñas: muestra estado vacío.; Respuesta vacía: no guarda.; No puede modificar la puntuación o texto del deportista.",
    "routes": [
      "businessReviews",
      "respondReview"
    ]
  },
  {
    "id": "HU-E13",
    "title": "Gestionar el siguiente turno",
    "goal": "Como establecimiento, quiero recibir automáticamente al siguiente solicitante para continuar la asignación cuando vence el pago anterior.",
    "flow": [
      "El sistema detecta vencimiento, marca el turno vencido y libera su apartado.",
      "Selecciona automáticamente al siguiente solicitante disponible.",
      "Notifica al establecimiento quién perdió el turno y quién sigue.",
      "El establecimiento revisa y acepta al nuevo solicitante.",
      "Se aparta el recurso e inicia un nuevo plazo; se notifica al nuevo deportista."
    ],
    "alternatives": "Fila vacía: se notifica el vencimiento y queda disponibilidad sin asignar.; Pago anterior ya confirmado: no avanza la fila.; Nuevo solicitante con cruce: se marca incompatible, se le notifica y se propone al siguiente sin habilitar pago al incompatible.",
    "routes": [
      "manage",
      "request"
    ]
  },
  {
    "id": "HU-A01",
    "title": "Revisar establecimientos pendientes",
    "goal": "Como administrador, quiero consultar registros y datos de negocios para evaluar su validación.",
    "flow": [
      "Abre validaciones.",
      "Filtra por estado.",
      "Selecciona un establecimiento.",
      "Consulta datos enviados, NIT y resultado de consulta simulada si existe.",
      "Continúa a decisión."
    ],
    "alternatives": "No hay pendientes: muestra estado vacío.; Datos faltantes: los identifica sin asumir validez.; Cuenta no administradora: deniega acceso.",
    "routes": [
      "validations",
      "validationDetail"
    ]
  },
  {
    "id": "HU-A02",
    "title": "Decidir validación de negocio",
    "goal": "Como administrador, quiero aprobar o rechazar el registro con un motivo para controlar qué establecimientos pueden ofrecer servicios.",
    "flow": [
      "Revisa el expediente.",
      "Elige aprobar o rechazar.",
      "Ingresa motivo y confirma.",
      "El sistema registra decisión y notifica al negocio.",
      "Habilita publicación únicamente cuando queda validado."
    ],
    "alternatives": "Datos insuficientes: no presenta verificación exitosa automática.; Registro ya resuelto: muestra resultado actual y evita duplicación.; Rechazo sin motivo: solicita completar.",
    "routes": [
      "validationDetail",
      "validations"
    ]
  },
  {
    "id": "HU-A03",
    "title": "Consultar reportes",
    "goal": "Como administrador, quiero revisar incidencias y su contexto para evaluar si requieren una acción de moderación.",
    "flow": [
      "Abre reportes.",
      "Filtra por estado o motivo.",
      "Selecciona un reporte.",
      "Consulta descripción y elemento relacionado.",
      "Continúa a resolver."
    ],
    "alternatives": "Contenido ya retirado: muestra lo disponible sin inventar evidencia.; Sin reportes: estado vacío.; Acceso no autorizado: restringe la información.",
    "routes": [
      "reports",
      "reportDetail"
    ]
  },
  {
    "id": "HU-A04",
    "title": "Resolver reportes",
    "goal": "Como administrador, quiero registrar una decisión de moderación para atender incidencias con trazabilidad.",
    "flow": [
      "Revisa contexto.",
      "Elige retirar el contenido reportado o descartar el reporte.",
      "Registra justificación.",
      "Confirma.",
      "El sistema registra responsable, fecha y resultado; informa al reportante y, si retira contenido, a su autor."
    ],
    "alternatives": "Justificación ausente: no finaliza.; Reporte ya resuelto: muestra decisión y evita repetir la acción.; Acción fuera de permisos: no se ejecuta.",
    "routes": [
      "reportDetail",
      "reports"
    ]
  }
];
