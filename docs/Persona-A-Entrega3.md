# Persona A (entrega 3): Firebase y multiusuario

Este documento describe qué se implementó, cómo publicarlo en Firebase paso a paso, cómo probarlo y qué limitaciones declarar.
No requiere el plan de pago de Firebase (no usa Storage ni Cloud Functions).

> Estado: integrado en `entrega3/integracion-firebase-localizacion-hardware`. Compila
> (`./gradlew :app:compileDebugKotlin`), pasan las 75 pruebas unitarias (`./gradlew :app:testDebugUnitTest`)
> y las pruebas de `firestore.rules` contra el emulador (`cd tools/reglas && npm test`).
> Falta la prueba con dos dispositivos de la sección 4, que requiere publicar las reglas.

## 1. Qué se implementó

| Pieza | Archivo | Qué hace |
|---|---|---|
| Escritura atómica | `data/Nube.kt` | `Nube.agrupar { … }` reúne todas las escrituras de una operación y las confirma en **un lote atómico**, o en una **transacción** si lleva condiciones (`Condicion.Igual`, `Condicion.Menor`). Si falla, avisa por `Nube.errores` y resincroniza. `Nube.confirmarOperacion { … }` hace lo mismo pero **espera** la respuesta del servidor y lanza el error a quien llama: lo usan pagar una solicitud y pagar una transferencia, que no pueden mostrarse como exitosas antes de que Firestore las acepte. `Nube.ejecutar(mensaje) { … }` lanza una operación de segundo plano (subir fotografías) con su propio aviso. |
| Conversión | `data/Mapeos.kt` | Modelo de dominio ⇄ documento de Firestore (explícito, sin reflexión). |
| Sincronización | `data/Sincronizacion.kt` | Un listener por colección, limitado a lo que cada rol puede leer. Se inicia en `SesionViewModel.iniciar` y se detiene al cerrar sesión. |
| Almacenes | `domain/*.kt` | Mismas funciones públicas y `StateFlow` que antes (la interfaz no cambió); ahora leen de Firestore y escriben en él. Ya no usan `DatosMock` como fuente. |
| Fotos compartidas | `data/FotosRemotas.kt` | Comprime y sube imágenes a `fotos/{hash}` (referencia `fr:{hash}`) o, cuando pertenecen a una reserva, a `fotosPrivadas/{hash}` (referencia `fp:{hash}`). Los documentos guardan la referencia, nunca rutas locales. |
| Encuentros | `data/Encuentros.kt` | Datos y permisos de «Encontrarnos» para Persona C. Cada apertura estrena una `sesion`, así que al reabrir un encuentro no reaparece ninguna posición anterior. |
| Acceso a encuentros | `domain/AccesoEncuentros.kt` | Única definición de quién entra y comparte: deportista activo que participa en ese chat (dos deportistas) o comunidad. La usan el chat, la comunidad, la pantalla, el aviso y `Encuentros.abrir`; las reglas repiten la comprobación en el servidor. |
| Semilla | `data/Semilla.kt` | Carga única de negocios y servicios de demostración (la ejecuta el administrador). |
| Reglas | `firestore.rules` | Acceso por rol, dueño, miembro o participante; estados válidos; cupos; encuentros; autorización de pago para cambiar el titular de una reserva. |
| Pruebas de reglas | `tools/reglas/` | 42 pruebas sobre `firestore.rules` con el emulador de Firestore (`npm test`). No tocan el proyecto real. |

### Colecciones

| Colección | Id del documento | Notas |
|---|---|---|
| `usuarios` | uid | `Cuenta.id` es **siempre el uid**. Del propio perfil solo se pueden editar `nombre`, `telefono` y `foto`: el rol, el estado de la cuenta y el `negocioId` quedan fijos. |
| `negocios`, `actividades` | `b1…`, `a…` | `actividades.fotos` = lista de referencias `fr:…`. |
| `solicitudes`, `reservas`, `pagos`, `reembolsos` | ids deterministas donde importa | Llevan `negocioId` para que el establecimiento consulte y para las reglas. |
| `ocupacion` | `{actividadId}_{fecha}_{hora}` | Contador `n` de cupos por hora; las reglas impiden que supere la capacidad. |
| `transferencias` | uuid | Campo `version`; `postulantesUids` para consultar «en las que participo». |
| `autorizacionesTransferencia` | id de la transferencia | Permiso que emite el establecimiento al aprobar: comprador, reserva, monto y plazo. Sin él las reglas no permiten cambiar el titular de la reserva ni completar la transferencia. |
| `chats/{id}` y `chats/{id}/mensajes/{id}` | chat individual: `uidMenor_uidMayor`; comunidad: id de la comunidad | El id determinista evita chats duplicados. |
| `comunidades`, `invitaciones` | invitación: `{comunidadId}_{invitadoId}` | Aceptar es una transacción. |
| `resenas` | id de la reserva | Una sola reseña por reserva (lo garantiza el id). |
| `notificaciones` | uuid | `usuarioId` = destinatario, `autorId` = quien origina el evento. |
| `reportes` | uuid | Lo lee el administrador. |
| `fotos` | hash SHA-256 del contenido | No se sobrescribe; misma foto = mismo documento. Legible con sesión: fotos de servicios, comunidades y perfil. |
| `fotosPrivadas` | hash SHA-256 del contenido y de la reserva | Foto de llegada: la sube el titular de la reserva y la leen solo ese titular y el establecimiento. |
| `encuentros/{e_<origenId>}` y `/sesiones/{sesion}/posiciones/{uid}` | ver sección 5 | Contrato con Persona C. |

### Cómo se evitan duplicados y sobreventa

- **Pagar una solicitud:** transacción que exige que la solicitud siga en `PAGO`; la reserva usa el id `r{solicitudId}`, el pago `p{solicitudId}`. Pulsar dos veces, o hacerlo desde dos dispositivos, no crea una segunda reserva.
- **Aprobar una solicitud:** exige estado `REVISION` y que cada hora tenga cupo (`ocupacion.n < capacidad`); retiene el cupo (+1). Rechazar o retirar una solicitud en `PAGO` lo libera (−1); cancelar una reserva también.
- **Transferencias:** toda operación exige que la `version` del servidor sea la que vio quien actúa. Dos interesados simultáneos: el segundo recibe «otra persona modificó esto» y la pantalla se resincroniza. Completar una transferencia usa ids deterministas (`p_t_…`, `ref_t_…`).
- **Invitaciones:** responder exige `PENDIENTE`; el id determinista impide invitaciones repetidas.
- **Avisos:** se escriben en el mismo lote/transacción que el hecho que los origina; si el hecho no se guarda, el aviso tampoco.

Si una transacción falla, la operación ya se mostró en el teléfono (actualización optimista); al fallar se emite un mensaje por `Nube.errores` y `Sincronizacion.reiniciar()` vuelve a leer el estado real.

## 2. Paso a paso en la consola de Firebase

1. **Proyecto.** Usa el mismo proyecto de la entrega 2 (el de `app/google-services.json`). Plan **Spark** (gratuito) es suficiente.
2. **Authentication.** Debe seguir habilitado *Correo/contraseña*. Confirma que existen los usuarios de demostración (contraseña `Demo1234`): `juan@demo.test`, `camila@demo.test`, `negocio@demo.test`, `admin@demo.test`.
3. **Perfiles.** En Firestore → `usuarios` cada documento tiene como ID el UID del usuario. Comprueba:
   - `admin@demo.test`: `rol = ADMINISTRADOR`, `activa = true`.
   - `negocio@demo.test`: `rol = ESTABLECIMIENTO`, `negocioId = b1`, `activa = true`.
   - `juan` y `camila`: `rol = DEPORTISTA`, `activa = true`.
   - El campo `idApp` ya no es necesario (puede quedarse).
4. **Reglas.** Firestore Database → pestaña **Reglas** → borra el contenido, pega el archivo `firestore.rules` del repositorio → **Publicar**. Si aparece algún error de sintaxis, la consola indica la línea.
5. **Índices.** No hay que crear índices: todas las consultas son de igualdad sobre un solo campo.
6. **Catálogo inicial.** Inicia sesión **una vez** en la aplicación con `admin@demo.test`. Si `negocios` está vacía, la aplicación carga los 7 negocios y sus servicios. Verifica en la consola que aparecen las colecciones `negocios` (7 documentos) y `actividades`.
   Si no aparecen: revisa Logcat (filtro `Semilla`) y que las reglas se publicaron.
7. **Cuentas de prueba.** Para la demostración con dos dispositivos usa `juan@demo.test` en uno y `camila@demo.test` en otro (o registra cuentas nuevas desde la aplicación). Ya no se siembran reservas ni chats de ejemplo: se crean usando la aplicación.
8. **Comprobar reglas.** Lo más rápido es `cd tools/reglas && npm test` (42 pruebas en el emulador, sin tocar este proyecto). En la consola, Firestore → Reglas → **Simulador de reglas**: simula una lectura (`get`) de `/databases/(default)/documents/encuentros/e_<idDeUnChat>` autenticado con un UID que **no** pertenezca al chat; debe salir **Denegado** (ENC-02). Repite con un UID participante que sea deportista: **Permitido**. Una posición se consulta en `/encuentros/e_<idDeUnChat>/sesiones/<sesion>/posiciones/<uid>`.
9. **Respaldo de seguridad.** Antes de la sustentación exporta o anota el estado de las colecciones por si hay que restaurar datos de demostración (consola → Firestore → Datos).

## 3. Qué cambió fuera de mis archivos

Solo lo imprescindible de Persona B:
- `MainActivity.kt`: una línea (`FotosRemotas.iniciar(this)`), junto a `FotosLocales.iniciar(this)`.
- `res/values/strings.xml`: cinco textos de error (`nube_*`).
- `navigation/SportHubApp.kt`: un `SnackbarHost` que muestra `Nube.errores` (antes nadie consumía ese flujo).
- `ui/screens/deportista/PagoScreen.kt` y `PagoViewModel.kt`: el pago espera la confirmación de Firestore y el botón se deshabilita mientras tanto.
- `ui/screens/comun/ChatScreen.kt`, `ComunidadScreen.kt` y `navigation/DestinoNotificacion.kt`: el acceso a «Encontrarnos» usa `AccesoEncuentros`.

La foto de perfil **ya no es solo local**: al guardar el perfil se sube como referencia compartida (`fotos/{hash}`) y se guarda en el campo `foto` del usuario, para que las demás personas la vean en chats y comunidades. Es legible por cualquier cuenta con sesión, igual que el nombre.

## 4. Pruebas con dos dispositivos (A)

| ID | Prueba | Resultado esperado |
|---|---|---|
| A-01 | Dispositivo 1 envía un mensaje en un chat. | Aparece en el dispositivo 2 sin reiniciar; llega el aviso al destinatario correcto. |
| A-02 | Cerrar y reabrir la aplicación en ambos. | Chats, comunidades, reservas y avisos persisten. |
| A-03 | Invitar a una comunidad y aceptar desde el otro dispositivo. | La comunidad aparece al aceptar; quien invitó recibe el aviso; el chat de la comunidad funciona para ambos. |
| A-04 | Solicitud → aprobar (establecimiento) → pagar. | Los tres estados se ven en los tres dispositivos. |
| A-05 | Pulsar «Pagar» dos veces rápidamente, o desde dos dispositivos con la misma cuenta. | Una sola reserva y un solo pago. |
| A-06 | Dos solicitudes para un espacio de capacidad 1; el establecimiento aprueba ambas. | La segunda aprobación se rechaza con «Ya no quedan cupos». |
| A-07 | Publicar una transferencia; dos cuentas se unen a la vez. | Ambas quedan en la fila o la segunda recibe el aviso de conflicto y se resincroniza; nunca se pierde un interesado ni se completa dos veces. |
| A-08 | Dejar una reseña y responderla. | Se ve en ambos lados; una segunda reseña de la misma reserva se rechaza. |
| A-09 | Subir foto a un servicio / foto de llegada. | Se ve en el otro dispositivo. |
| A-10 | Con una cuenta ajena, leer `reservas`, `chats` o `encuentros` de otras personas (por ejemplo desde el simulador de reglas). | Denegado. |
| A-11 | Modo avión, enviar un mensaje, volver a conectar. | El mensaje se envía al reconectar. Pagar o aprobar sin conexión muestra el error de conexión. |

## 5. Contratos para B y C

### Notificaciones (B)
`notificaciones/{id}`: `usuarioId` (destinatario), `titulo`, `texto`, `ruta`, `argumento`, `leida`, `autorId`, `creadaEn`.
`Notificaciones.notificaciones` ya expone solo las de la cuenta actual; `AvisosSistema` y la campana siguen funcionando sin cambios.
Los avisos llegan mientras la aplicación está abierta o su proceso sigue vivo. Con la aplicación cerrada del todo falta integrar FCM y un emisor confiable de notificaciones: declarar como limitación.

### Fotos (B)
`FotosRemotas.subir(uri): String` devuelve la referencia `fr:…` y `FotosRemotas.resolver(ref): String?` una URI local lista para mostrar.
Los almacenes ya lo usan para fotos de servicios, comunidades y foto de llegada; B no necesita cambiar `SelectorFoto`.

### Encuentros (C)
```kotlin
val id = Encuentros.abrir(OrigenEncuentro.CHAT /* o COMUNIDAD */, origenId)   // suspend; id = "e_<origenId>"
Encuentros.observar(id)                 // Flow<Encuentro?>: estado, punto común, creador (null = sin acceso)
Encuentros.observarPosiciones(id, sesion, participantes)   // Flow<List<PosicionCompartida>>: un listener por participante
Encuentros.publicarPosicion(id, sesion, lat, lng, precision)   // activar/actualizar (solo con el interruptor encendido)
Encuentros.dejarDeCompartir(id, sesion)   // apagar interruptor o salir: borra la posición propia
Encuentros.proponerPunto(id, lat, lng)  // «Nos encontramos aquí»
Encuentros.finalizar(id)                // creador o administrador de la comunidad
```
- `origenId` es el id del chat individual (`chat.id`) o de la comunidad (`comunidad.id`).
- `sesion` es `Encuentro.sesion`: identifica la apertura vigente. `abrir` estrena una sesión cada vez que crea o reabre el encuentro, así que ninguna posición anterior vuelve a ser legible sin activar otra vez el interruptor. Las reglas no permiten **listar** la subcolección: por eso se pasa la lista de participantes y se escucha un documento por persona.
- Solo entran y comparten deportistas activos que participen en ese chat o comunidad (`AccesoEncuentros`); el establecimiento que escribe en un chat no entra.
- **Acuerdos tomados (confirmar con C):** un punto común activo por encuentro; cualquier participante puede reemplazarlo; finaliza quien creó el encuentro o el administrador de la comunidad; sin historial de desplazamiento (un documento de posición por persona).
- **Vigencia:** `PosicionCompartida.enVivo()` = actualizada en los últimos 60 s; después se muestra como desactualizada; a los 300 s deja de mostrarse (`Encuentros.SEGUNDOS_EN_VIVO` y `SEGUNDOS_VIGENCIA`). La hora es la del servidor.
- **Consentimiento:** el documento `sesiones/{sesion}/posiciones/{uid}` existe solo mientras esa persona comparte; solo ella lo escribe y puede borrarlo siempre. Al volver a la pantalla no hay documento hasta que active el interruptor otra vez.
- **Acceso:** las reglas calculan los participantes a partir de los miembros actuales del chat o de la comunidad. Una cuenta ajena no lee nada aunque conozca el id; quien deja una comunidad pierde el acceso sin que nadie borre datos. Al finalizar, las posiciones dejan de ser legibles para todos.
- **Cierre abrupto:** al no haber borrado, la última posición queda como desactualizada y deja de mostrarse a los 300 s.
- Mientras se comparte, publicar con el ciclo de vida de la pantalla (cada ~10–15 s, no en segundo plano).

## 6. Limitaciones que conviene declarar

1. **Reglas, no servidor propio.** Los pagos son simulados y las reglas validan estados y propietarios, pero no pueden comprobar toda la lógica de negocio (por ejemplo, que un monto sea correcto). Con Cloud Functions (plan Blaze) esa lógica pasaría al servidor.
2. **Avisos sin FCM.** No hay notificaciones con la aplicación completamente cerrada.
3. **Los avisos los escribe el cliente.** Una cuenta modificada podría enviar un aviso falso a otra; con Cloud Functions se generarían en el servidor.
4. **Fotos en Firestore.** Cada imagen se comprime a menos de 700 KB; es suficiente para el MVP. Con Firebase Storage (plan Blaze) bastaría reemplazar `FotosRemotas.subir/descargar`.
5. **Autovalidación del negocio.** Se conserva el comportamiento de la maqueta: al solicitar validación con nombre y NIT completos, el negocio pasa a validado. Para endurecerlo, cambiar la regla de `negocios` para que solo el administrador pueda establecer `VALIDADO`.
6. **Reloj.** Las fechas límite de pago usan el reloj del dispositivo; la vigencia de posiciones y la hora de los avisos usan la del servidor.
7. **Sin vencimiento automático de solicitudes.** El plan de pago no se vence solo (tampoco ocurría antes); un cupo retenido se libera al retirar o rechazar.
8. **Operaciones críticas sin conexión.** Aprobar, pagar, cancelar y transferir requieren conexión (son transacciones); el resto se encola y se envía al reconectar.
