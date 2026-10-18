# Persona A: autenticación con Firebase

## Qué se implementó
- **Firebase Auth** (correo y contraseña): registro, inicio y cierre de sesión, recuperación de contraseña por correo y sesión persistente.
- **Cloud Firestore**: colección `usuarios/{uid}` con el perfil y el rol de cada usuario (`set` al registrar, `get` al iniciar sesión, `update` al editar el perfil y `snapshots()` en tiempo real para chats, contactos e invitaciones).
- `SesionViewModel.cuenta` expone el usuario autenticado (id, rol, nombre, `negocioId`, `uid`) al resto de la app.

## Documento `usuarios/{uid}`
| Campo | Tipo | Descripción |
|---|---|---|
| nombre | String | Nombre (o contacto del establecimiento) |
| email | String | Correo en minúsculas |
| telefono | String? | Opcional |
| rol | String | `DEPORTISTA`, `ESTABLECIMIENTO` o `ADMINISTRADOR` |
| negocioId | String? | Solo establecimientos |
| activa | Boolean | `false` bloquea el acceso |
| creadoEn | Timestamp | Hora del servidor |
| idApp | String? | Solo cuentas de demostración (`juan`, `camila`, `negocio`, `admin`) |

No se guarda ninguna contraseña en Firestore. La foto de perfil sigue siendo local y privada (Persona B).

## Configuración en la consola de Firebase
1. Crear el proyecto y agregar la app Android con el paquete `com.sportec.sporthub`.
2. Descargar `google-services.json` y colocarlo en `app/`.
3. Authentication → método de acceso → habilitar **Correo/contraseña**. En Plantillas → Restablecer contraseña, poner el idioma en español.
4. Firestore Database → Crear base de datos → modo producción → pegar `firestore.rules` en la pestaña Reglas y publicar.
5. Crear los usuarios de demostración (Authentication → Usuarios → Agregar usuario, contraseña `Demo1234`) y su documento en `usuarios` con el UID como ID:

| Correo | nombre | rol | idApp | negocioId | telefono |
|---|---|---|---|---|---|
| juan@demo.test | Juan Carlos | DEPORTISTA | juan | | 300 123 4567 |
| camila@demo.test | Camila Torres | DEPORTISTA | camila | | |
| negocio@demo.test | Laura Méndez | ESTABLECIMIENTO | negocio | b1 | 300 444 3322 |
| admin@demo.test | Ana Administración | ADMINISTRADOR | admin | | |

En todos: `email` igual al correo, `activa` = true (boolean).

## Pruebas manuales
Registro (deportista y establecimiento), correo duplicado, contraseña de menos de 6 caracteres, inicio de sesión correcto e incorrecto, cerrar y reabrir la app (la sesión se restaura), correo de recuperación real, cerrar sesión, sin red (mensaje de servicio no disponible).
