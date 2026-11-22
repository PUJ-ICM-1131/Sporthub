package com.sportec.sporthub.ui.screens.comun

import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.sportec.sporthub.R
import com.sportec.sporthub.data.Encuentro
import com.sportec.sporthub.data.Encuentros
import com.sportec.sporthub.data.EstadoEncuentro
import com.sportec.sporthub.data.Nube
import com.sportec.sporthub.data.PosicionCompartida
import com.sportec.sporthub.domain.Comunidades
import com.sportec.sporthub.domain.Cuentas
import com.sportec.sporthub.domain.Mensajes
import com.sportec.sporthub.domain.Notificaciones
import com.sportec.sporthub.domain.Ubicacion
import com.sportec.sporthub.ui.components.Aviso
import com.sportec.sporthub.ui.components.EstadoCargando
import com.sportec.sporthub.ui.components.EstadoError
import com.sportec.sporthub.ui.components.EstadoVacio
import com.sportec.sporthub.ui.components.MapaMapbox
import com.sportec.sporthub.ui.components.MarcadorMapa
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.PermisoSistema
import com.sportec.sporthub.ui.components.SeguimientoUbicacion
import com.sportec.sporthub.ui.components.TipoAviso
import com.sportec.sporthub.ui.components.mapboxDisponible
import com.sportec.sporthub.ui.components.rememberPermisosUbicacion
import com.sportec.sporthub.utils.Coordenada
import com.sportec.sporthub.utils.EstadoPosicion
import com.sportec.sporthub.utils.Geo
import com.sportec.sporthub.utils.LogicaEncuentro
import com.sportec.sporthub.utils.Publicacion
import com.sportec.sporthub.utils.Textos
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private sealed interface CargaEncuentro {
    data object Cargando : CargaEncuentro
    data object SinAcceso : CargaEncuentro
    data class Listo(val encuentro: Encuentro) : CargaEncuentro
}

private val ColorPunto = Color(0xFFDF854B)
private val ColorCandidato = Color(0xFF5B6F86)
private val ColoresParticipantes = listOf(
    Color(0xFF2F6FDB), Color(0xFF8E44AD), Color(0xFFC0392B),
    Color(0xFF16A085), Color(0xFFB7950B), Color(0xFF34495E)
)
private val Respaldo = Coordenada(4.6486, -74.0813)

private fun colorDe(uid: String) = ColoresParticipantes[abs(uid.hashCode()) % ColoresParticipantes.size]

private fun nombreDe(uid: String): String =
    Cuentas.porId(uid)?.let { Cuentas.nombreParaMostrar(it) } ?: Textos.obtener(R.string.encuentro_participante)

private fun hace(segundos: Long): String =
    if (segundos < 60) Textos.obtener(R.string.encuentro_hace_s, segundos.toInt())
    else Textos.obtener(R.string.encuentro_hace_min, (segundos / 60).toInt())

private fun distanciaTexto(metros: Double): String =
    if (metros < 1000) Textos.obtener(R.string.encuentro_distancia_m, metros.roundToInt())
    else Textos.obtener(R.string.encuentro_distancia_km, String.format("%.1f", metros / 1000))

/** Conectividad del dispositivo; sirve para avisar que las posiciones pueden no estar al día. */
@Composable
private fun rememberConectado(): Boolean {
    val context = LocalContext.current
    val gestor = remember { context.getSystemService(ConnectivityManager::class.java) }
    fun hayRed(): Boolean = gestor.activeNetwork?.let {
        gestor.getNetworkCapabilities(it)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } == true
    var conectado by remember { mutableStateOf(hayRed()) }
    DisposableEffect(gestor) {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { conectado = true }
            override fun onLost(network: Network) { conectado = hayRed() }
        }
        gestor.registerDefaultNetworkCallback(callback)
        onDispose { gestor.unregisterNetworkCallback(callback) }
    }
    return conectado
}

/**
 * «Encontrarnos»: mapa de un encuentro ligado a un chat individual o a una comunidad.
 *
 * - El permiso de Android solo permite leer la ubicación; compartirla exige activar el interruptor,
 *   que empieza apagado cada vez que se abre la pantalla.
 * - Solo se comparte mientras la pantalla está visible: al salir, pasar a segundo plano, perder el
 *   permiso o el acceso, o cuando el encuentro finaliza, se retira la posición propia.
 * - El punto de reunión es independiente de las posiciones: se puede proponer sin compartir.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun EncontrarnosScreen(
    origenId: String,
    usuarioId: String,
    onBack: () -> Unit
) {
    SeguimientoUbicacion()
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val alcance = rememberCoroutineScope()
    val gestorUbicacion = remember { context.getSystemService(Context.LOCATION_SERVICE) as LocationManager }

    val chats by Mensajes.chats.collectAsState()
    val comunidades by Comunidades.comunidades.collectAsState()
    val chat = chats.firstOrNull { it.id == origenId }
    val comunidadId = chat?.comunidadId ?: comunidades.firstOrNull { it.id == origenId }?.id
    val comunidad = comunidades.firstOrNull { it.id == comunidadId }
    val esComunidad = comunidadId != null
    val idOrigen = comunidadId ?: origenId
    val participantes: List<String> = if (esComunidad) comunidad?.miembros.orEmpty() else chat?.miembros.orEmpty()
    val tieneAcceso = if (esComunidad) usuarioId in participantes else chat != null && Mensajes.esMiembro(chat, usuarioId)
    val nombreOrigen = if (esComunidad) comunidad?.nombre ?: Textos.obtener(R.string.texto_comunidad)
        else participantes.firstOrNull { it != usuarioId }?.let(::nombreDe) ?: Textos.obtener(R.string.texto_conversacion)

    // ---------- Encuentro en Firestore ----------
    var intento by remember { mutableIntStateOf(0) }
    var encuentroId by remember { mutableStateOf<String?>(null) }
    var errorApertura by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(idOrigen, tieneAcceso, intento) {
        if (!tieneAcceso) return@LaunchedEffect
        errorApertura = null
        try {
            encuentroId = Encuentros.abrir(LogicaEncuentro.tipoOrigen(esComunidad), idOrigen)
        } catch (e: Exception) {
            errorApertura = Textos.obtener(R.string.encuentro_error_abrir)
        }
    }
    val carga by produceState<CargaEncuentro>(CargaEncuentro.Cargando, encuentroId) {
        val id = encuentroId ?: return@produceState
        Encuentros.observar(id).collect { value = it?.let(CargaEncuentro::Listo) ?: CargaEncuentro.SinAcceso }
    }
    val encuentro = (carga as? CargaEncuentro.Listo)?.encuentro
    val abierto = encuentro?.estado == EstadoEncuentro.ABIERTO
    val posiciones by produceState(emptyList<PosicionCompartida>(), encuentroId, abierto) {
        val id = encuentroId
        if (id == null || !abierto) { value = emptyList(); return@produceState }
        Encuentros.observarPosiciones(id).collect { value = it }
    }
    // Reloj de la pantalla: las posiciones envejecen aunque Firestore no envíe cambios.
    val ahora by produceState(System.currentTimeMillis()) {
        while (isActive) { delay(5_000); value = System.currentTimeMillis() }
    }

    // ---------- Ubicación propia ----------
    val posicion by Ubicacion.posicionActual.collectAsState()
    val conectado = rememberConectado()
    var ubicacionActiva by remember { mutableStateOf(LocationManagerCompat.isLocationEnabled(gestorUbicacion)) }
    LaunchedEffect(ahora) { ubicacionActiva = LocationManagerCompat.isLocationEnabled(gestorUbicacion) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var pendienteActivar by remember { mutableStateOf(false) }
    val permisos = rememberPermisosUbicacion { resultado ->
        if (resultado.values.none { it } && pendienteActivar) {
            pendienteActivar = false
            mensaje = Textos.obtener(R.string.encuentro_permiso_denegado)
        }
    }
    val permitido = permisos.permissions.any { it.status.isGranted }
    val soloAproximada = permitido && permisos.permissions.none {
        it.permission == android.Manifest.permission.ACCESS_FINE_LOCATION && it.status.isGranted
    }
    val propia = posicion?.takeIf { LogicaEncuentro.lecturaUtil(it, ahora, ubicacionActiva) }
        ?.let { Coordenada(it.latitud, it.longitud) }

    // ---------- Compartir ----------
    // No se guarda con rememberSaveable: al volver a la pantalla el interruptor empieza apagado (ENC-07).
    var compartiendo by remember { mutableStateOf(false) }
    var ultima by remember { mutableStateOf<Publicacion?>(null) }
    val idActual by rememberUpdatedState(encuentroId)

    fun detener() {
        val id = idActual
        val habiaPublicado = compartiendo || ultima != null
        compartiendo = false
        pendienteActivar = false
        ultima = null
        // Se lanza en el alcance de Nube para que la retirada sobreviva al cierre de la pantalla.
        if (habiaPublicado && id != null) Nube.alcance.launch { runCatching { Encuentros.dejarDeCompartir(id) } }
    }

    fun activar() {
        mensaje = null
        if (!permitido) {
            pendienteActivar = true
            permisos.launchMultiplePermissionRequest()
        } else {
            compartiendo = true
        }
    }

    LaunchedEffect(permitido) {
        if (permitido && pendienteActivar) { pendienteActivar = false; compartiendo = true }
        if (!permitido && compartiendo) { detener(); mensaje = Textos.obtener(R.string.encuentro_permiso_revocado) }
    }
    LaunchedEffect(tieneAcceso) { if (!tieneAcceso) detener() }
    LaunchedEffect(encuentro?.estado) { if (encuentro != null && !abierto) detener() }

    val detenerActual by rememberUpdatedState(::detener)
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_STOP) detenerActual()
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            detenerActual()
        }
    }

    LaunchedEffect(compartiendo, encuentroId, abierto) {
        val id = encuentroId
        if (!compartiendo || id == null || !abierto) return@LaunchedEffect
        while (isActive) {
            val instante = System.currentTimeMillis()
            val lectura = Ubicacion.posicionActual.value
            val activa = LocationManagerCompat.isLocationEnabled(gestorUbicacion)
            if (lectura != null && LogicaEncuentro.debePublicar(ultima, lectura, instante, activa)) {
                try {
                    Encuentros.publicarPosicion(id, lectura.latitud, lectura.longitud, lectura.precisionMetros)
                    ultima = Publicacion(Coordenada(lectura.latitud, lectura.longitud), instante)
                } catch (e: Exception) {
                    mensaje = Textos.obtener(R.string.encuentro_error_compartir)
                    delay(LogicaEncuentro.RENOVACION_MS)
                }
            }
            delay(1_000)
        }
    }

    // ---------- Punto de reunión ----------
    var candidato by remember { mutableStateOf<Coordenada?>(null) }
    var confirmarFin by remember { mutableStateOf(false) }

    fun proponer(punto: Coordenada) {
        val id = encuentroId ?: return
        alcance.launch {
            try {
                Encuentros.proponerPunto(id, punto.latitud, punto.longitud)
                candidato = null
                participantes.filter { it != usuarioId }.forEach {
                    Notificaciones.evento(it, R.string.aviso_punto_encuentro, "meetup", idOrigen)
                }
            } catch (e: Exception) {
                mensaje = Textos.obtener(R.string.encuentro_error_punto)
            }
        }
    }

    val conMapbox = mapboxDisponible()
    PantallaBase(
        titulo = stringResource(R.string.encuentro_titulo),
        onBack = { detener(); onBack() },
        acciones = { PermisoSistema() }
    ) { padding ->
        when {
            !tieneAcceso || carga is CargaEncuentro.SinAcceso -> {
                EstadoVacio(mensaje = stringResource(R.string.encuentro_sin_acceso), modifier = Modifier.padding(padding))
                return@PantallaBase
            }
            errorApertura != null -> {
                EstadoError(mensaje = errorApertura.orEmpty(), onReintentar = { intento++ }, modifier = Modifier.padding(padding))
                return@PantallaBase
            }
            encuentro == null -> {
                EstadoCargando(modifier = Modifier.padding(padding))
                return@PantallaBase
            }
        }
        val actual = encuentro!!
        val visibles = LogicaEncuentro.visibles(posiciones, participantes, usuarioId, ahora)
        val punto = actual.punto?.let { Coordenada(it.latitud, it.longitud) }
        val puedeFinalizar = LogicaEncuentro.puedeFinalizar(actual, usuarioId, comunidad?.adminId)

        val marcadores = buildList {
            visibles.forEach { p ->
                val estado = LogicaEncuentro.estado(p, ahora)
                add(MarcadorMapa(
                    id = p.uid,
                    posicion = Coordenada(p.latitud, p.longitud),
                    resaltado = false,
                    atenuado = estado == EstadoPosicion.DESACTUALIZADA,
                    etiqueta = if (estado == EstadoPosicion.DESACTUALIZADA)
                        Textos.obtener(R.string.encuentro_etiqueta_desactualizada, nombreDe(p.uid)) else nombreDe(p.uid),
                    color = colorDe(p.uid)
                ))
            }
            punto?.let { add(MarcadorMapa("punto", it, resaltado = true, atenuado = false, Textos.obtener(R.string.encuentro_punto), ColorPunto)) }
            candidato?.let { add(MarcadorMapa("candidato", it, resaltado = false, atenuado = false, Textos.obtener(R.string.encuentro_candidato), ColorCandidato)) }
        }
        // El encuadre cambia cuando cambia quién aparece o el punto, no con cada movimiento.
        val claveEncuadre = visibles.map { it.uid }.sorted() to Triple(punto, propia != null, abierto)
        val encuadre = remember(claveEncuadre) {
            buildList {
                propia?.let(::add)
                punto?.let(::add)
                visibles.forEach { add(Coordenada(it.latitud, it.longitud)) }
            }
        }
        val centro = propia ?: punto ?: encuadre.firstOrNull() ?: Respaldo

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.encuentro_en, nombreOrigen),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (!abierto) {
                item { Aviso(texto = stringResource(R.string.encuentro_finalizado), tipo = TipoAviso.ADVERTENCIA) }
                item {
                    Button(onClick = { encuentroId = null; intento++ }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.encuentro_reabrir))
                    }
                }
                item {
                    OutlinedButton(onClick = { detener(); onBack() }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.encuentro_salir))
                    }
                }
                return@LazyColumn
            }
            item {
                if (conMapbox) {
                    MapaMapbox(
                        modifier = Modifier.fillMaxWidth().height(320.dp).clip(RoundedCornerShape(20.dp)),
                        origen = centro,
                        origenEsPosicionActual = propia != null,
                        marcadores = marcadores,
                        encuadrarEn = encuadre,
                        mostrarOrigen = false,
                        onMarcarOrigen = { candidato = it }
                    )
                } else {
                    Aviso(texto = stringResource(R.string.encuentro_mapa_no_disponible), tipo = TipoAviso.ADVERTENCIA)
                }
            }
            if (!conectado) item { Aviso(texto = stringResource(R.string.encuentro_sin_conexion), tipo = TipoAviso.ADVERTENCIA) }
            mensaje?.let { item { Aviso(texto = it, tipo = TipoAviso.ERROR) } }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.encuentro_compartir), style = MaterialTheme.typography.titleSmall)
                                Text(
                                    stringResource(if (compartiendo) R.string.encuentro_compartiendo else R.string.encuentro_no_compartiendo),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = compartiendo || pendienteActivar,
                                onCheckedChange = { if (it) activar() else detener() }
                            )
                        }
                        if (compartiendo && !ubicacionActiva) {
                            Aviso(texto = stringResource(R.string.encuentro_gps_apagado), tipo = TipoAviso.ADVERTENCIA)
                        } else if (compartiendo && propia == null) {
                            Aviso(texto = stringResource(R.string.encuentro_esperando_lectura), tipo = TipoAviso.ADVERTENCIA)
                        }
                        if (compartiendo && soloAproximada) {
                            Aviso(texto = stringResource(R.string.encuentro_aproximada), tipo = TipoAviso.NORMAL)
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Flag, contentDescription = null, tint = ColorPunto)
                            Text(stringResource(R.string.encuentro_punto), style = MaterialTheme.typography.titleSmall)
                        }
                        if (actual.punto != null && punto != null) {
                            Text(
                                stringResource(R.string.encuentro_punto_autor, nombreDe(actual.punto.autorId).takeIf { actual.punto.autorId != usuarioId }
                                    ?: stringResource(R.string.encuentro_tu)),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            propia?.let {
                                Text(
                                    stringResource(R.string.encuentro_a_distancia, distanciaTexto(Geo.distanciaMetros(it, punto))),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Text(stringResource(R.string.encuentro_sin_punto), style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(
                            stringResource(if (conMapbox) R.string.encuentro_como_elegir else R.string.encuentro_como_elegir_sin_mapa),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (propia != null) {
                            OutlinedButton(onClick = { candidato = propia }, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text(stringResource(R.string.encuentro_usar_mi_ubicacion), modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                        Button(
                            onClick = { candidato?.let(::proponer) },
                            enabled = candidato != null,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(stringResource(R.string.encuentro_nos_encontramos_aqui)) }
                        if (candidato != null) {
                            TextButton(onClick = { candidato = null }, modifier = Modifier.fillMaxWidth()) {
                                Text(stringResource(R.string.encuentro_descartar_candidato))
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    stringResource(R.string.encuentro_quienes_comparten),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (visibles.isEmpty()) {
                item { Aviso(texto = stringResource(R.string.encuentro_esperando), tipo = TipoAviso.NORMAL) }
            } else {
                items(visibles, key = { it.uid }) { p ->
                    val estado = LogicaEncuentro.estado(p, ahora)
                    val distancia = propia?.let { Geo.distanciaMetros(it, Coordenada(p.latitud, p.longitud)) }
                    ListItem(
                        leadingContent = { Surface(color = colorDe(p.uid), shape = CircleShape, modifier = Modifier.size(14.dp)) {} },
                        headlineContent = { Text(nombreDe(p.uid)) },
                        supportingContent = {
                            val textoEstado = if (estado == EstadoPosicion.EN_VIVO) stringResource(R.string.encuentro_en_vivo)
                                else stringResource(R.string.encuentro_desactualizada, hace(LogicaEncuentro.antiguedadSegundos(p, ahora)))
                            Text(distancia?.let { "$textoEstado · ${distanciaTexto(it)}" } ?: textoEstado)
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }

            item {
                OutlinedButton(onClick = { detener(); onBack() }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.encuentro_salir))
                }
            }
            if (puedeFinalizar) item {
                TextButton(onClick = { confirmarFin = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.encuentro_finalizar), color = MaterialTheme.colorScheme.error)
                }
            }
            item {
                Text(
                    stringResource(R.string.encuentro_privacidad),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (confirmarFin) {
        AlertDialog(
            onDismissRequest = { confirmarFin = false },
            title = { Text(stringResource(R.string.encuentro_finalizar)) },
            text = { Text(stringResource(R.string.encuentro_finalizar_confirmacion)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmarFin = false
                    val id = encuentroId ?: return@TextButton
                    detener()
                    alcance.launch {
                        try { Encuentros.finalizar(id) }
                        catch (e: Exception) { mensaje = Textos.obtener(R.string.encuentro_error_finalizar) }
                    }
                }) { Text(stringResource(R.string.encuentro_finalizar)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmarFin = false }) { Text(stringResource(R.string.encuentro_cancelar)) }
            }
        )
    }
}
