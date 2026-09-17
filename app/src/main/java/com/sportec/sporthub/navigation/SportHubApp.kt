package com.sportec.sporthub.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.sportec.sporthub.ui.SesionViewModel
import com.sportec.sporthub.ui.screens.acceso.BienvenidaScreen
import com.sportec.sporthub.ui.screens.acceso.LoginScreen
import com.sportec.sporthub.ui.screens.acceso.RegistroScreen
import com.sportec.sporthub.ui.screens.comun.NotificacionesScreen
import com.sportec.sporthub.ui.screens.comun.PantallaPendiente
import com.sportec.sporthub.ui.screens.comun.PerfilScreen
import com.sportec.sporthub.ui.screens.deportista.CancelarReservaScreen
import com.sportec.sporthub.ui.screens.deportista.DetalleActividadScreen
import com.sportec.sporthub.ui.screens.deportista.DetalleReservaScreen
import com.sportec.sporthub.ui.screens.deportista.DetalleSolicitudScreen
import com.sportec.sporthub.ui.screens.deportista.ExplorarScreen
import com.sportec.sporthub.ui.screens.deportista.HorariosScreen
import com.sportec.sporthub.ui.screens.deportista.MisReservasScreen
import com.sportec.sporthub.ui.screens.deportista.PagoExitosoScreen
import com.sportec.sporthub.ui.screens.deportista.PagoScreen
import com.sportec.sporthub.ui.screens.deportista.ResumenSolicitudScreen

@Composable
fun SportHubApp(
    sesionViewModel: SesionViewModel = viewModel()
) {
    val cuenta by sesionViewModel.cuenta.collectAsState()
    val backStack = rememberNavBackStack(Bienvenida)
    val actividad = LocalActivity.current

    val pestanas = cuenta?.let { pestanasDe(it.rol) }.orEmpty()
    val rutaActual = backStack.lastOrNull()
    val mostrarBarra = pestanas.any { it.ruta == rutaActual }

    fun irA(ruta: NavKey) {
        val indice = backStack.indexOf(ruta)
        if (indice >= 0) {
            while (backStack.lastIndex > indice) {
                backStack.removeAt(backStack.lastIndex)
            }
        } else {
            backStack.add(ruta)
        }
    }

    fun reiniciarEn(ruta: NavKey) {
        if (backStack.size == 1 && backStack.first() == ruta) return
        backStack.remove(ruta)
        backStack.add(ruta)
        while (backStack.size > 1) {
            backStack.removeAt(0)
        }
    }

    fun volver() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        } else {
            actividad?.finish()
        }
    }

    LaunchedEffect(cuenta) {
        if (cuenta == null && backStack.lastOrNull() !is RutaAcceso) {
            reiniciarEn(Bienvenida)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (mostrarBarra) {
                NavigationBar {
                    pestanas.forEach { pestana ->
                        NavigationBarItem(
                            selected = pestana.ruta == rutaActual,
                            onClick = {
                                if (pestana.ruta != rutaActual) reiniciarEn(pestana.ruta)
                            },
                            icon = { Icon(pestana.icono, contentDescription = null) },
                            label = { Text(pestana.etiqueta) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding),
            onBack = { volver() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
                rememberViewModelStoreNavEntryDecorator<NavKey>()
            ),
            entryProvider = entryProvider(
                fallback = { ruta ->
                    NavEntry(ruta) {
                        PantallaPendiente(
                            info = infoDe(ruta),
                            onNavegar = { irA(it) },
                            onBack = if (backStack.size > 1) {
                                { volver() }
                            } else {
                                null
                            }
                        )
                    }
                }
            ) {
                entry<Bienvenida> {
                    BienvenidaScreen(
                        onIniciarSesion = { irA(Login) },
                        onCrearCuenta = { irA(Registro) }
                    )
                }
                entry<Login> {
                    LoginScreen(
                        onBack = { volver() },
                        onRecuperar = { irA(Recuperar) },
                        onAutenticado = { cuentaIniciada ->
                            sesionViewModel.iniciar(cuentaIniciada)
                            reiniciarEn(inicioDe(cuentaIniciada.rol))
                        }
                    )
                }
                entry<Registro> {
                    RegistroScreen(
                        onBack = { volver() },
                        onDeportista = { irA(RegistroDeportista) },
                        onEstablecimiento = { irA(RegistroEstablecimiento) },
                        onIniciarSesion = { irA(Login) }
                    )
                }
                entry<Explorar> {
                    ExplorarScreen(onNavegar = { irA(it) })
                }
                entry<Perfil> {
                    PerfilScreen(
                        cuenta = cuenta,
                        onNavegar = { irA(it) },
                        onCerrarSesion = {
                            reiniciarEn(Bienvenida)
                            sesionViewModel.cerrar()
                        }
                    )
                }
                entry<Notificaciones> {
                    NotificacionesScreen(
                        usuarioId = cuenta?.id.orEmpty(),
                        onBack = { volver() },
                        onNavegar = { irA(it) }
                    )
                }
                entry<DetalleActividad> { ruta ->
                    DetalleActividadScreen(
                        actividadId = ruta.id,
                        onBack = { volver() },
                        onVerHorarios = { irA(Horarios(ruta.id)) }
                    )
                }
                entry<Horarios> { ruta ->
                    HorariosScreen(
                        actividadId = ruta.actividadId,
                        onBack = { volver() },
                        onSolicitar = { fecha, hora -> irA(ResumenSolicitud(ruta.actividadId, fecha, hora)) }
                    )
                }
                entry<ResumenSolicitud> { ruta ->
                    ResumenSolicitudScreen(
                        actividadId = ruta.actividadId,
                        fecha = ruta.fecha,
                        hora = ruta.hora,
                        usuarioId = cuenta?.id.orEmpty(),
                        onBack = { volver() },
                        onSolicitudEnviada = { id -> irA(DetalleSolicitud(id)) }
                    )
                }
                entry<DetalleSolicitud> { ruta ->
                    DetalleSolicitudScreen(
                        solicitudId = ruta.id,
                        cuenta = cuenta,
                        onBack = { volver() },
                        onPagar = { id -> irA(Pago(id)) }
                    )
                }
                entry<Pago> { ruta ->
                    PagoScreen(
                        solicitudId = ruta.referencia,
                        usuarioId = cuenta?.id.orEmpty(),
                        onBack = { volver() },
                        onPagoExitoso = { reservaId -> irA(PagoExitoso(reservaId)) }
                    )
                }
                entry<PagoExitoso> { ruta ->
                    PagoExitosoScreen(
                        reservaId = ruta.referencia,
                        onVerReserva = { id -> reiniciarEn(DetalleReserva(id)) }
                    )
                }
                entry<MisReservas> {
                    MisReservasScreen(
                        usuarioId = cuenta?.id.orEmpty(),
                        onNavegar = { irA(it) }
                    )
                }
                entry<DetalleReserva> { ruta ->
                    DetalleReservaScreen(
                        reservaId = ruta.id,
                        usuarioId = cuenta?.id.orEmpty(),
                        onBack = { volver() },
                        onCancelar = { id -> irA(CancelarReserva(id)) }
                    )
                }
                entry<CancelarReserva> { ruta ->
                    CancelarReservaScreen(
                        reservaId = ruta.id,
                        usuarioId = cuenta?.id.orEmpty(),
                        onBack = { volver() },
                        onCancelada = { volver() }
                    )
                }
            }
        )
    }
}
