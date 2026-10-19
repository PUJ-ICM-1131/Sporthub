# Configurar Mapbox

Cada integrante debe agregar el token público compartido por el equipo a `local.properties`, en la raíz del proyecto:

```properties
MAPBOX_ACCESS_TOKEN=pk.TOKEN_PUBLICO_DEL_EQUIPO
```

Conservar la línea `sdk.dir` y las demás propiedades existentes. `local.properties.example` sirve como plantilla de la propiedad; no reemplazar todo el archivo local si ya existe.

Sincronizar Gradle y volver a compilar y ejecutar la aplicación. También se admite la variable de entorno `MAPBOX_ACCESS_TOKEN` cuando la propiedad local no está definida.

El token se comparte por el canal privado del equipo. No incluir su valor en esta documentación ni en commits. `local.properties` está excluido de Git; el APK sí contiene el token público necesario para Mapbox. No usar tokens secretos `sk.`. El consumo de los integrantes que utilicen el mismo token corresponde a la misma cuenta de Mapbox.
