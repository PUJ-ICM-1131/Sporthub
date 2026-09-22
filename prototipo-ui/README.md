# Prototipo de interfaz de SportHub

Prototipo web navegable que representa las pantallas y los recorridos de SportHub para deportistas, establecimientos y administradores. Complementa la aplicación Android del repositorio.

## Abrir el prototipo

Abrir `index.html` en un navegador moderno. No requiere instalación de paquetes ni compilación. También se puede servir desde esta carpeta con Python:

```sh
python -m http.server 8766
```

Después abrir `http://localhost:8766`. Los datos de la demo se conservan en el almacenamiento local del navegador; la opción **Reiniciar demo** restablece el escenario.

## Navegación

- **Opciones de demostración:** seleccionar cuentas por rol, ajustar el reloj, simular fallos y restablecer datos.
- **Historias en esta vista:** consultar la relación entre la pantalla y las historias de usuario.
- **Deportista:** buscar actividades, consultar mapa y disponibilidad, solicitar reservas, pagar de forma ficticia, transferir, conversar y participar en comunidades.
- **Establecimiento:** gestionar servicios, horarios, condiciones, solicitudes, turnos, pagos y devoluciones simuladas.
- **Administrador:** revisar establecimientos y atender reportes.

Los escenarios utilizan cuentas y datos ficticios. La simulación de permisos, mapas, notificaciones y pagos no acredita una integración nativa ni permite operar con dinero real.

## Archivos

| Archivo | Responsabilidad |
|---|---|
| `index.html` | Página de entrada y contenedores de la interfaz. |
| `styles.css` | Estilos, colores y distribución adaptable. |
| `app.js` | Vistas, formularios, navegación y eventos. |
| `model.js` | Datos de ejemplo, reglas y transiciones de estado. |
| `stories.js` | Identificadores y recorridos de historias. |
| `verify.cjs` | Comprobaciones de regresión de reglas del modelo. |
| `verify-complete.cjs` | Ejecución de escenarios y matriz de criterios. |
| `tests-*.cjs` | Escenarios agrupados por acceso, negocio, reservas, comunidad, transferencias, búsqueda y recursos móviles. |

Los scripts se mantienen en esta carpeta para conservar sus referencias relativas. Los ZIP y las copias HTML empaquetadas no forman parte del código fuente publicado.

## Pruebas

Desde esta carpeta, con Node.js que soporte `structuredClone`:

```sh
node verify.cjs
node verify-complete.cjs "ruta/a/Historias-de-usuario.md"
```

La segunda prueba requiere la especificación Markdown de la wiki. Si se omite la ruta, busca el clon hermano `Sporthub.wiki-publicacion`. El informe `verificacion-completa.json` se genera localmente y no se versiona. Estas pruebas verifican lógica y recorridos en un entorno JavaScript simulado, no una ejecución en Android ni una revisión visual completa del navegador.

## Documentación

[Diseño de interfaz](https://github.com/PUJ-ICM-1131/Sporthub/wiki/Diseno-de-UI) · [Historias de usuario](https://github.com/PUJ-ICM-1131/Sporthub/wiki/Historias-de-usuario)
