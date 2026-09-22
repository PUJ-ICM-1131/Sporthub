# SportHub

**Descubre, reserva y conecta.**

SportHub es una aplicación Android del equipo **Sportec** que reúne actividades deportivas y de bienestar, reservas y comunidades privadas. Está dirigida a personas de 18 años en adelante, con Bogotá como punto de partida y una propuesta extensible a otras ciudades y pueblos.

[Wiki del proyecto](https://github.com/PUJ-ICM-1131/Sporthub/wiki) · [Historias de usuario](https://github.com/PUJ-ICM-1131/Sporthub/wiki/Historias-de-usuario) · [Prototipo de interfaz](prototipo-ui/README.md) · [Issues](https://github.com/PUJ-ICM-1131/Sporthub/issues)

## Qué permite hacer

- Descubrir actividades por categoría, ubicación, disponibilidad y radio de cercanía.
- Solicitar un espacio deportivo completo o un cupo en una clase.
- Gestionar reservas con aprobación del establecimiento, fila de solicitudes y plazo de pago.
- Cancelar y transferir reservas según las condiciones del establecimiento.
- Conversar con personas y negocios, y participar en comunidades privadas.
- Consultar notificaciones, calificar experiencias y reportar incidencias.

El catálogo contempla fútbol, pádel, tenis, baloncesto, natación, squash, gimnasios, CrossFit, yoga, pilates, boxeo y entrenamiento funcional. Marcadores deportivos y torneos se consideran ampliaciones futuras.

## Roles

| Rol | Responsabilidades |
|---|---|
| Deportista | Explorar, solicitar reservas, transferir, conversar, participar en comunidades y calificar. |
| Establecimiento | Administrar servicios, disponibilidad, condiciones, solicitudes y atención a usuarios. |
| Administrador | Validar establecimientos y gestionar reportes. |

Cada cuenta tiene un rol fijo. El registro público no permite crear cuentas de administrador.

## Aplicación y simulaciones

El repositorio contiene la aplicación Android y el código del prototipo web. La autenticación, los servicios de backend y las operaciones financieras se representan con mocks y datos ficticios. Los pagos y reembolsos no operan con dinero real.

Las [reglas del producto](https://github.com/PUJ-ICM-1131/Sporthub/wiki/Alcance-y-reglas) y los criterios de las historias definen el comportamiento esperado. La existencia de una pantalla no sustituye la comprobación de su recorrido.

## Explorar el prototipo

Abrir `prototipo-ui/index.html` en el navegador. El [README del prototipo](prototipo-ui/README.md) explica sus archivos, cuentas de demostración, escenarios y pruebas. Consultar archivos en GitHub no ejecuta automáticamente la interfaz HTML.

## Qué contiene la wiki

| Página | Contenido |
|---|---|
| [Presentación](https://github.com/PUJ-ICM-1131/Sporthub/wiki/Presentación) | Problema, objetivos, propuesta de valor, público y equipo. |
| [Alcance y reglas](https://github.com/PUJ-ICM-1131/Sporthub/wiki/Alcance-y-reglas) | Roles, funcionalidades y condiciones de negocio. |
| [Lean Canvas](https://github.com/PUJ-ICM-1131/Sporthub/wiki/Lean-Canvas) | Síntesis del producto y su propuesta de valor. |
| [PRD](https://github.com/PUJ-ICM-1131/Sporthub/wiki/PRD) | Requisitos del producto. |
| [Historias de usuario](https://github.com/PUJ-ICM-1131/Sporthub/wiki/Historias-de-usuario) | 57 historias y 227 criterios de aceptación. |
| [Arquitectura](https://github.com/PUJ-ICM-1131/Sporthub/wiki/Arquitectura) | Responsabilidades de interfaz, aplicación, dominio y adaptadores. |
| [Modelo de datos](https://github.com/PUJ-ICM-1131/Sporthub/wiki/Modelo-de-datos) | Información e invariantes que debe preservar el producto. |
| [Diagrama de clases](https://github.com/PUJ-ICM-1131/Sporthub/wiki/Diagrama-de-clases) | Imagen del modelo y descripción técnica. |
| [Diseño de interfaz](https://github.com/PUJ-ICM-1131/Sporthub/wiki/Diseno-de-UI) | Organización de vistas, roles y enlace al prototipo. |
| [Componentes del proyecto](https://github.com/PUJ-ICM-1131/Sporthub/wiki/Entregables) | Documentación, prototipo y aplicación Android. |

## Colaboración

Consultar las historias antes de modificar un flujo, trabajar en una rama y describir el cambio y su verificación en el pull request. Cuando exista un issue asociado, enlazarlo. Actualizar la wiki si cambia una regla de negocio y comprobar los recorridos afectados. La wiki se versiona en su propio repositorio Git.

## Equipo Sportec

| Integrante | Rol |
|---|---|
| Diego Fernando Zabala | Desarrollador |
| Juan Carlos Santamaria | Desarrollador |
| Diego Alejandro Negro | Desarrollador |
