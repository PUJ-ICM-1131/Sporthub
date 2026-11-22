# Pruebas de `firestore.rules`

Comprueban en el **emulador de Firestore** que las reglas del repositorio acepten lo que la
aplicación necesita y rechacen lo demás. No tocan el proyecto real de Firebase: el emulador
trabaja con el proyecto `demo-sporthub` y el prefijo `demo-` impide usar credenciales reales.

## Requisitos

- Node.js 20 o superior (`node --version`).
- Java 11 o superior en el PATH (lo exige el emulador de Firestore).

## Cómo ejecutarlas

```bash
cd tools/reglas
npm install
npm test
```

`npm test` levanta el emulador, ejecuta las pruebas y lo apaga. Las reglas se leen del archivo
`firestore.rules` de la raíz del repositorio, así que se prueba exactamente lo que se publica.

## Qué cubren

| Grupo | Comprobaciones |
|---|---|
| Encuentros: quién entra | Solo deportistas activos del chat o de la comunidad; se rechaza al establecimiento del chat, al administrador de la aplicación, a cuentas ajenas y a quien no inició sesión. |
| Posiciones compartidas | Cada persona publica únicamente la suya y con la hora del servidor; no se publica en una sesión distinta a la vigente; una posición de más de 300 s deja de ser legible; cada quien puede retirar la propia y nadie borra la de otra persona. |
| Punto de reunión y cierre | Cualquier participante propone el punto a su nombre; finaliza quien abrió el encuentro o el administrador de la comunidad. |
| Perfil | Se edita el nombre propio; no se cambian el rol, el estado de la cuenta ni el `negocioId`; no se edita el perfil de otra persona. |
| Transferencias | Inscripción válida; la autorización de pago solo la emite el establecimiento y con el monto de la reserva; no se cambia el titular de la reserva sin pago ni autorización; completar la transferencia funciona en un solo lote y se rechaza sin autorización o desde una cuenta ajena. |
| Fotografías | Se suben a nombre propio y no se sobrescriben; la foto de llegada solo la sube el titular de la reserva y solo la ven el titular y su establecimiento. |
| Reembolsos | Los consulta el beneficiario o el establecimiento; solo el establecimiento los resuelve. |

## Si falla el arranque del emulador

La primera ejecución descarga el emulador de Firestore, así que necesita conexión. Si el puerto
8080 está ocupado, cámbialo en `firebase.json` (raíz del repositorio) y vuelve a ejecutar.
