# Persona C (entrega 3): localización, mapas y encuentros

Rama: `entrega3/localizacion-encuentros`, creada desde `develop` e incorporando la base de Persona A (`multusuario`).

> Estado: compila (`./gradlew assembleDebug`) y pasan las pruebas unitarias (56, de ellas 11 nuevas en
> `LogicaEncuentroTest`). Falta la prueba con dos dispositivos (sección 4), que requiere publicar `firestore.rules`.

## 1. Qué se implementó

| Pieza | Archivo | Qué hace |
|---|---|---|
| Pantalla «Encontrarnos» | `ui/screens/comun/EncontrarnosScreen.kt` | Mapa del encuentro, interruptor de compartir, marcadores con nombre, punto de reunión, estados de espera, conexión y desactualización, salir y finalizar. |
| Reglas de la pantalla | `utils/LogicaEncuentro.kt` | Cuándo publicar la posición, estado de cada posición (en vivo / desactualizada / vencida), filtro de participantes y quién puede finalizar. Probado en `LogicaEncuentroTest`. |
| Marcadores con etiqueta | `ui/components/MapaMapbox.kt` | `MarcadorMapa` acepta `etiqueta` y `color` (opcionales; las pantallas anteriores no cambian). |
| Acceso | `ChatScreen.kt`, `ComunidadScreen.kt` | Icono de ubicación en la barra del chat (individual o de comunidad) y botón «Encontrarnos» en la comunidad. |
| Navegación | `Rutas.kt`, `SportHubApp.kt`, `Destinos.kt` | Ruta `Encontrarnos(origenId)`: id del chat individual o de la comunidad. |
| Aviso del punto | `DestinoNotificacion.kt` | Al proponer un punto se avisa a los demás participantes (`ruta = "meetup"`); abrir el aviso valida que la cuenta siga participando. |
| Regla de lectura | `firestore.rules` | Se permite leer `encuentros/{id}` cuando aún no existe (`resource == null`); sin esto la primera apertura fallaba con PERMISSION_DENIED. |

El seguimiento propio y las rutas de Mapbox (permisos, precisión, ciclo de vida, recálculo al desviarse, distancia y tiempo restantes, ruta estimada sin conexión) ya estaban en `develop` desde las entregas anteriores.

## 2. Comportamiento acordado con Persona A

Se usa el contrato de `data/Encuentros.kt` sin cambios:

- **Un encuentro por chat o comunidad** (`e_<origenId>`). El chat de una comunidad abre el encuentro de la comunidad, así el acceso sigue a sus miembros actuales.
- **Consentimiento:** el interruptor empieza apagado cada vez que se abre la pantalla. Conceder el permiso de Android no activa el interruptor; si se activa el interruptor sin permiso, se pide el permiso.
- **Solo con la pantalla visible:** al salir, al pasar a segundo plano (`ON_STOP`), al perder el permiso o el acceso, o cuando el encuentro finaliza, se borra la posición propia. No se reanuda sola.
- **Frecuencia de envío:** al moverse ≥ 10 m (mínimo 3 s entre envíos) y una renovación cada 15 s aunque no haya movimiento, para seguir «en vivo» (60 s). No se envía una lectura de más de 2 min ni con el GPS apagado.
- **Punto de reunión:** mantener presionado el mapa (o «Usar mi ubicación actual») y confirmar con «Nos encontramos aquí». Cualquier participante lo reemplaza; no depende de compartir la posición y no se borra al apagar el interruptor.
- **Finalizar:** solo quien abrió el encuentro o el administrador de la comunidad (igual que en las reglas). Cualquiera puede iniciar uno nuevo después.
- **Desactualizada:** la pantalla recalcula cada 5 s; pasados 60 s el marcador se atenúa y dice «desactualizada», a los 300 s desaparece.
- **Segunda barrera:** además de las reglas, la pantalla solo dibuja posiciones de participantes actuales.

## 3. Para publicar

1. Publicar `firestore.rules` de esta rama en la consola de Firebase (incluye el cambio de la regla de lectura).
2. Tener `MAPBOX_ACCESS_TOKEN` en `local.properties`; sin token la pantalla funciona, pero sin mapa interactivo.

## 4. Pruebas pendientes con dos dispositivos

Cuentas `juan@demo.test` y `camila@demo.test`, con un chat entre ambas; una tercera cuenta para los accesos no autorizados.

| ID | Cómo | Esperado |
|---|---|---|
| ENC-01 | Abrir el icono de ubicación en el chat. | Interruptor apagado; solo los dos participantes entran. |
| ENC-02 | Simulador de reglas con un UID ajeno sobre `encuentros/e_<chatId>`. | Denegado. |
| ENC-03 | Activar en ambos (en el emulador, mover la ubicación desde *Extended controls*). | Cada uno ve el marcador con el nombre del otro moverse. |
| ENC-04 | Compartir en un chat y abrir otro. | En el segundo el interruptor está apagado y no hay posición propia. |
| ENC-05 | Proponer un punto con el interruptor apagado. | El otro ve el punto (y recibe un aviso); no aparece la posición de quien lo propuso. |
| ENC-06 | Apagar el interruptor. | El marcador desaparece en el otro; el punto sigue. |
| ENC-07 | Salir y volver. | Interruptor apagado; posición retirada. |
| ENC-08 | Modo avión en uno mientras comparte. | En el otro pasa a «desactualizada» a los 60 s y desaparece a los 300 s. |
| ENC-09 | Salir de la comunidad durante un encuentro de comunidad. | Pierde el acceso y deja de compartir. |
| ENC-10 | Finalizar desde quien lo abrió. | Ambos ven «Este encuentro finalizó». |
| ENC-11 | Quitar el permiso de ubicación desde Ajustes mientras comparte. | Deja de compartir sin cierre inesperado. |

## 5. Limitaciones

- No hay seguimiento en segundo plano: compartir exige la pantalla abierta.
- La vigencia compara la hora del servidor con el reloj del teléfono; un reloj muy desfasado altera «en vivo».
- Al girar la pantalla se recrea la actividad y el interruptor vuelve a apagarse.
- No se incluye Street View.
