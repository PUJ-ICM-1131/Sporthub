package com.sportec.sporthub.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import com.sportec.sporthub.domain.Rol

data class Pestana(
    val ruta: NavKey,
    val etiqueta: String,
    val icono: ImageVector
)

fun pestanasDe(rol: Rol): List<Pestana> = when (rol) {
    Rol.DEPORTISTA -> listOf(
        Pestana(Explorar, "Explorar", Icons.Filled.Search),
        Pestana(MisReservas, "Mis reservas", Icons.Filled.DateRange),
        Pestana(Mensajes, "Mensajes", Icons.Filled.Email),
        Pestana(Perfil, "Perfil", Icons.Filled.Person)
    )
    Rol.ESTABLECIMIENTO -> listOf(
        Pestana(Gestion, "Gestión", Icons.Filled.DateRange),
        Pestana(Servicios, "Servicios", Icons.Filled.Build),
        Pestana(Pagos, "Pagos", Icons.Filled.ShoppingCart),
        Pestana(Mensajes, "Mensajes", Icons.Filled.Email),
        Pestana(Perfil, "Cuenta", Icons.Filled.Person)
    )
    Rol.ADMINISTRADOR -> listOf(
        Pestana(Validaciones, "Negocios", Icons.Filled.CheckCircle),
        Pestana(Reportes, "Reportes", Icons.Filled.Warning),
        Pestana(Perfil, "Cuenta", Icons.Filled.Person)
    )
}

fun inicioDe(rol: Rol): NavKey = pestanasDe(rol).first().ruta
