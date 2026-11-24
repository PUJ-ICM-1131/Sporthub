# SportHub — Diagnóstico y guion de la demostración (entrega 3)

Revisado sobre `main` (`a2d5119`, PR #7). La app compila (`assembleDebug`) y pasan las 86 pruebas unitarias.
Este documento se basa en el código; **los flujos no se han ejecutado de punta a punta en dos emuladores**. Hay que hacer un ensayo completo antes de grabar (sección 7).

## 1. Diagnóstico: qué hay realmente

| Requisito del guion | Estado en el código | Qué mostrar / declarar |
|---|---|---|
| Multiusuario y misma base de datos | ✅ Firestore con listeners en vivo (`data/Sincronizacion.kt`), sesiones con Firebase Auth. | Cambios en un emulador aparecen en el otro sin reiniciar. |
| Registro de establecimiento | ⚠️ Pide nombre del negocio, NIT, dirección, persona de contacto, teléfono, correo y contraseña. **No pide foto, descripción ni ubicación**: todo negocio nuevo queda en el centro de Bogotá (5.6486, -74.0813). | No prometer foto ni ubicación en el registro. La descripción se edita después en la ficha. |
| Aprobación por el administrador | ✅ `Validaciones` → detalle → Aprobar / Rechazar (`Negocios.validar`). | Mostrar el cambio de estado en ambos emuladores. |
| Aviso de aprobación | ✅ (C2, rama `fix/validacion-negocio`) El establecimiento recibe un aviso al ser aprobado o rechazado. | Mostrar el aviso en E1. |
| Botón «Reenviar a revisión» del establecimiento | ✅ (C3) Deja el negocio PENDIENTE y avisa a los administradores; solo el administrador valida (también en `firestore.rules`). | Se puede usar en el video. |
| Crear servicio | ✅ Nombre, categoría, tipo, precio, duración, capacidad, horario, política, hasta 8 fotos (cámara o galería). Las fotos se suben a Firestore (`FotosRemotas`). | Foto con cámara aquí: es la evidencia de cámara. |
| Catálogo dinámico | ✅ El deportista lo lee de Firestore. | Crear el servicio y verlo aparecer en el otro emulador. |
| Ubicación y permisos | ✅ Permiso preciso/aproximado con Accompanist, Fused Location, seguimiento con la pantalla visible. | Mostrar el diálogo de permiso y la posición configurada en el emulador. |
| Mapa | ✅ **Mapbox** (no Google Maps). Sin token → mapa simplificado. | Decir «Mapbox» en el video. |
| Ruta, distancia y tiempo | ✅ Directions API de Mapbox (REST, `utils/DireccionesMapbox.kt`), a pie / bici / carro; distancia y tiempo restantes en vivo; recalcula al desviarse > 60 m. | Evidencia REST principal. |
| Rutas alternativas | ❌ No se piden (`alternatives` no se usa). | Declarar como no implementado. |
| Sin conexión / error de la API | ✅ Ruta estimada en línea recta, marcada como estimada. | Activar modo avión en la escena REST. |
| Reserva | ✅ Solicitud → el **establecimiento aprueba** → el deportista paga dentro del plazo. Cupos por hora (`ocupacion`), cruce de horarios validado. | El orden real incluye la aprobación del establecimiento antes del pago. |
| Pago | ⚠️ **Simulado** (tarjeta / billetera / transferencia «simulado»). No hay pasarela ni modo de pruebas de terceros. | Decir en voz alta «pago simulado». El registro sí queda en `pagos` en Firestore. |
| Transferencia | ✅ con otro orden (ver 5.5): A **publica** la reserva, B **se une** desde «Reservas transferibles» (no se elige a B), A acepta, el establecimiento aprueba, B paga, se crea el reembolso de A. | Seguir el orden real. |
| Chat | ✅ Al unirse, B envía un mensaje automático a A; chat bidireccional en vivo y persistente. | |
| Reembolso | ✅ Queda **PENDIENTE**; el establecimiento lo marca como realizado o fallido en «Reembolsos». | No presentarlo como realizado antes de confirmarlo. |
| Notificaciones | ⚠️ Avisos en la campana (Firestore) + notificaciones del sistema Android generadas **localmente** cuando llega el aviso (`AvisosSistema`). **No hay FCM**: con la app cerrada del todo no llegan. Al tocarlas abren la pantalla correspondiente. | Declarar: «notificaciones locales disparadas por datos compartidos, no push». |
| Cámara y galería | ✅ `SelectorFoto`: servicios, comunidades, perfil, foto de llegada. | |
| Sensores | ✅ Acelerómetro + magnetómetro en la **brújula** hacia el establecimiento. No se usa sensor de luz ni otros. | En el emulador: *Extended controls → Virtual sensors*, girar el dispositivo. |
| Ubicación compartida en tiempo real | ✅ «Encontrarnos» desde un chat o comunidad: interruptor, marcadores en vivo, punto de reunión, ruta al punto. | **Es la mejor evidencia de tiempo real con dos emuladores**; no está en el prompt, pero sí en el plan de la entrega 3. |

## 2. Plan de correcciones antes de grabar

| ID | Corrección | Esfuerzo | Recomendación |
|---|---|---|---|
| C1 | Ensayo completo en dos emuladores (sección 7) y anotar fallos reales. | 1 h | **Obligatorio.** |
| C2 | Aviso al establecimiento cuando el administrador aprueba o rechaza (`Negocios.validar`). | 10 min | ✅ Aplicada. |
| C3 | «Reenviar a revisión» deja el negocio en `PENDIENTE` en lugar de validarlo solo. | 10 min | ✅ Aplicada. **Requiere publicar `firestore.rules` de nuevo.** |
| C4 | Foto y ubicación del negocio en el registro. | 2–4 h | No recomendado con la sustentación mañana: declarar como limitación. |
| C5 | Rutas alternativas de Mapbox. | 1–2 h | No recomendado: declarar. |

## 3. Configuración de los emuladores

| | Emulador 1 | Emulador 2 |
|---|---|---|
| AVD | `Medium_Phone_API_37.0` (ya existe) | Duplicado del anterior (*Device Manager → ⋮ → Duplicate*), p. ej. `SportHub_2` |
| Imagen | Android 37, Google Play, arm64 | igual |
| Cuentas | Establecimiento nuevo ↔ Deportista A | Administrador ↔ Deportista B |
| Ubicación | 5.6280, -74.0640 | 5.6533, -74.0837 |

1. Iniciar los dos AVD desde *Device Manager* y ponerlos lado a lado. Instalar la app en ambos (*Run* eligiendo cada dispositivo, o `adb -s <id> install -r app/build/outputs/apk/debug/app-debug.apk`).
2. Ubicación de cada uno (ojo: `geo fix` recibe **longitud primero**):
   ```bash
   ~/Library/Android/sdk/platform-tools/adb -s emulator-5554 emu geo fix -74.0640 5.6280
   ```
   ```bash
   ~/Library/Android/sdk/platform-tools/adb -s emulator-5556 emu geo fix -74.0837 5.6533
   ```
   (`adb devices` muestra los ids reales). También sirve *Extended controls → Location*.
3. Movimiento simulado: *Extended controls → Location → Routes* (trazar ruta y *Play route*), o este bucle que avanza ~25 m cada 2 s hacia el norte:
   ```bash
   for i in $(seq 0 30); do ~/Library/Android/sdk/platform-tools/adb -s emulator-5554 emu geo fix -74.0640 $(echo "5.6280 + $i*0.00023" | bc -l); sleep 2; done
   ```
4. Para que se vean los diálogos de permisos: *Ajustes → Apps → SportHub → Borrar almacenamiento* (o desinstalar/instalar) antes de grabar.
5. `local.properties` con `MAPBOX_ACCESS_TOKEN` válido (si no, no hay mapa interactivo).
6. Reglas de `firestore.rules` publicadas en la consola de Firebase (versión de `main`).

## 4. Datos de prueba

- **Administrador:** `admin@demo.test`. **Deportista A:** `juan@demo.test`. **Deportista B:** `camila@demo.test`. Contraseña de demostración en `docs/Persona-A-Entrega3.md`.
- **Establecimiento nuevo (se registra en vivo):** p. ej. «Club Demo Chapinero», NIT válido, dirección «Calle 63 # 9-20, Bogotá». Correo nuevo (no reutilizar uno ya registrado).
- **Servicio nuevo:** «Fútbol 5 nocturno», precio 80.000, duración 60 min, capacidad 1 (así se ve el cupo agotarse), horario 6–21, política de reembolso que devuelva > 0 % para que la transferencia genere reembolso.
- **Fecha de la reserva:** al menos un día después de la grabación (la transferencia exige que la reserva no haya empezado).
- **Respaldo:** negocios sembrados como «Verde Yoga» (5.659, -74.071) por si el registro en vivo falla.
- Antes de grabar: Juan y Camila sin reservas que crucen con el horario elegido; un chat Juan–Camila ya creado facilita la escena de «Encontrarnos».

## 5. Guion cronológico (~22 min; el plan de la entrega fija **máximo 15 min** de demo: sobran ~7 min: 5.7 y 5.8 pueden ir al pitch, y 5.3 y 5.5 se pueden acortar)

Etiquetas en pantalla: «Emulador 1 — <cuenta>» / «Emulador 2 — <cuenta>», actualizadas en cada cambio de sesión.

### 5.1 Registro y aprobación (3 min) — E1: establecimiento nuevo · E2: administrador
1. E1: Bienvenida → Registro → Establecimiento; llenar datos; crear cuenta. Mostrar estado «Pendiente».
2. E2: iniciar sesión como administrador → *Negocios por validar*: aparece el negocio nuevo sin recargar.
3. E2: abrir detalle → **Aprobar**.
4. E1: el estado cambia a «Validado» en vivo y llega el aviso de aprobación (campana y notificación del sistema).
5. Decir: la foto y la ubicación del negocio no se piden en el registro (limitación).

### 5.2 Servicio publicado (2 min) — E1: establecimiento · E2: deportista B
1. E2: cerrar sesión de administrador → entrar como **Camila (B)**.
2. E1: *Servicios → Nuevo*: datos del servicio; **foto con la cámara** (mostrar diálogo de permiso de cámara) → Guardar.
3. E2: *Explorar/Buscar*: el servicio aparece sin reiniciar; abrir detalle y comparar datos y foto.

### 5.3 Ubicación, mapa y ruta REST (3 min) — E1: deportista A
1. E1: cerrar sesión → entrar como **Juan (A)**. Diálogo de permiso de ubicación → **Precisa**.
2. *Mapa*: posición en 5.6280, -74.0640 (coincide con la configurada) y establecimientos con distancias.
3. Abrir el servicio → *Cómo llegar*: ruta por calles de la **Directions API de Mapbox**, distancia y tiempo; cambiar a bici y carro.
4. Ejecutar el bucle de movimiento: el punto avanza, la distancia y el tiempo restantes bajan; desviarse para mostrar el recálculo.
5. Modo avión → pedir otra ruta: aparece «ruta estimada» (manejo del fallo REST). Quitar modo avión.
6. *Brújula*: mover *Virtual sensors* → la flecha gira (acelerómetro + magnetómetro).

### 5.4 Reserva y pago (3 min) — E1: deportista A · E2: establecimiento
1. E1: elegir fecha y hora → resumen → *Enviar solicitud* (estado «En revisión»).
2. E2: cerrar sesión → entrar como **establecimiento**: aparece la solicitud → *Aprobar*.
3. E1: estado «Pago pendiente» + aviso → *Pagar* con medio **simulado** (decirlo) → reserva confirmada.
4. E2: la reserva y el pago aparecen en *Agenda* y *Pagos*; el cupo de esa hora se ve agotado.
5. Notificaciones del sistema en ambos (bajar la cortina; tocar una abre la pantalla).

### 5.5 Transferencia + chat (5 min) — E1: A · E2: B, luego establecimiento
1. E1 (A): *Mis reservas* → reserva → *Publicar transferencia*.
2. E2: cerrar sesión → **Camila (B)** → *Reservas transferibles* → abrir → revisar servicio, precio, fecha y establecimiento → *Unirme*.
3. E1: aviso «interesado» + mensaje automático de B en *Mensajes*. Responder; E2 contesta; mostrar el ida y vuelta en vivo.
4. E1 (A): detalle de la transferencia → *Aceptar interesado*.
5. E2: cerrar sesión → **establecimiento** → transferencia → ver ambos deportistas → *Aprobar*.
6. E2: cerrar sesión → **Camila (B)** → aviso «aprobada» → *Pagar* (simulado).
7. E1 (A): la reserva ya no está activa; aparece el **reembolso PENDIENTE**.
8. E2: cerrar sesión → **establecimiento** → *Reembolsos* → *Marcar realizado*. E1: el reembolso pasa a «Realizado» + aviso.
9. E2 → Camila: la reserva es suya; una sola reserva (sin duplicados).

### 5.6 Encontrarnos: ubicación compartida en vivo (2 min) — E1: A · E2: B
1. Ambos en el chat Juan–Camila → icono de ubicación. Interruptor apagado al entrar.
2. Activar en ambos: cada uno ve el marcador con el nombre del otro.
3. Mover E1 con el bucle: el marcador de Juan se mueve en E2.
4. Proponer un punto de reunión (pulsación larga) → aparece en ambos; ruta al punto.
5. Apagar el interruptor en E1: su marcador desaparece en E2; el punto sigue.

### 5.7 Hardware y permisos (resumen, 1 min)
Cámara (5.2), galería (foto de comunidad o perfil), ubicación (5.3/5.6), notificaciones (permiso al entrar), sensores (brújula 5.3).

### 5.8 Persistencia (1 min)
Cerrar la app en ambos, reabrir: reservas, chat, transferencia y reembolso siguen. Mostrar las colecciones en la consola de Firestore **sin mostrar claves ni tokens**.

## 6. Lista de comprobación de la rúbrica

| Requisito | Evidencia |
|---|---|
| Multiusuario en tiempo real | 5.1, 5.2, 5.5, 5.6 |
| Persistencia | 5.8 + consola de Firestore |
| Roles | admin, establecimiento, deportista (5.1–5.5) |
| Ubicación en tiempo real | 5.3 (movimiento) y 5.6 (entre dispositivos) |
| Mapa y rutas REST | 5.3 (Mapbox Directions, fallo controlado) |
| Cámara / galería | 5.2, 5.7 |
| Sensores | 5.3 brújula |
| Notificaciones | 5.4, 5.5 (locales, no push) |
| Pagos y reembolsos | 5.4, 5.5 (simulados, con registros en Firestore) |

## 7. Ensayo previo (obligatorio)

Hacer todo el guion una vez sin grabar y anotar aquí lo que falle. Puntos de riesgo: la primera apertura de «Encontrarnos» con las reglas publicadas; que los avisos del sistema aparezcan con el permiso concedido; el plazo de pago de la solicitud; que el servicio con capacidad 1 permita la transferencia.

## 8. Limitaciones a declarar en el video

Pagos y reembolsos simulados · notificaciones locales sin FCM · sin foto ni ubicación del negocio en el registro · sin rutas alternativas · Mapbox en lugar de Google Maps · ubicación compartida solo con la pantalla abierta.
