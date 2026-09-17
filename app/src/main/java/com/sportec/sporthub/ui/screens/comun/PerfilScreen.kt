package com.sportec.sporthub.ui.screens.comun

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import coil3.compose.AsyncImage
import com.sportec.sporthub.domain.Cuenta
import com.sportec.sporthub.domain.Rol
import com.sportec.sporthub.navigation.EditarPerfil
import com.sportec.sporthub.navigation.Notificaciones
import com.sportec.sporthub.navigation.opcionesPerfil
import com.sportec.sporthub.ui.components.Etiqueta
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.TarjetaHechos
import com.sportec.sporthub.utils.iniciales

private val PortadaInicio = Color(0xFF064D36)
private val PortadaFin = Color(0xFF187848)
private val AvatarFondo = Color(0xFFE1F3DB)
private val AvatarTexto = Color(0xFF205D3A)

@Composable
fun PerfilScreen(
    cuenta: Cuenta?,
    onNavegar: (NavKey) -> Unit,
    onCerrarSesion: () -> Unit
) {
    var confirmarSalida by rememberSaveable { mutableStateOf(false) }
    val titulo = if (cuenta?.rol == Rol.DEPORTISTA) "Perfil" else "Cuenta"

    PantallaBase(
        titulo = titulo,
        acciones = {
            IconButton(onClick = { onNavegar(Notificaciones) }) {
                Icon(Icons.Filled.Notifications, contentDescription = "Notificaciones")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .height(90.dp)
                            .background(
                                Brush.linearGradient(listOf(PortadaInicio, PortadaFin)),
                                RoundedCornerShape(23.dp)
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(104.dp)
                                .border(5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                .background(AvatarFondo, CircleShape)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (cuenta?.fotoUri != null) {
                                AsyncImage(
                                    model = cuenta.fotoUri,
                                    contentDescription = "Foto de perfil",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = iniciales(cuenta?.nombre.orEmpty()),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AvatarTexto
                                )
                            }
                        }
                        Text(
                            text = cuenta?.nombre.orEmpty(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Etiqueta(texto = cuenta?.rol?.etiqueta.orEmpty())
                    }
                }
            }
            item {
                TarjetaHechos(
                    pares = buildList {
                        add("Correo electrónico" to cuenta?.email.orEmpty())
                        add("Teléfono" to (cuenta?.telefono ?: "Sin agregar"))
                    }
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { onNavegar(EditarPerfil) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Editar mis datos")
                    }
                    opcionesPerfil(cuenta?.rol).forEach { destino ->
                        OutlinedButton(onClick = { onNavegar(destino.ruta) }, modifier = Modifier.fillMaxWidth()) {
                            Text(destino.etiqueta)
                        }
                    }
                    OutlinedButton(
                        onClick = { confirmarSalida = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cerrar sesión")
                    }
                }
            }
        }
    }

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Tendrás que ingresar de nuevo para ver tus reservas y mensajes.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarSalida = false
                    onCerrarSesion()
                }) {
                    Text("Cerrar sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarSalida = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
