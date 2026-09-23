package com.sportec.sporthub.navigation

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos

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
        Pestana(Explorar, Textos.obtener(R.string.texto_explorar), Icons.Filled.Search),
        Pestana(MisReservas, Textos.obtener(R.string.texto_mis_reservas), Icons.Filled.DateRange),
        Pestana(Mensajes, Textos.obtener(R.string.texto_mensajes), Icons.Filled.Email),
        Pestana(Perfil, Textos.obtener(R.string.texto_perfil), Icons.Filled.Person)
    )
    Rol.ESTABLECIMIENTO -> listOf(
        Pestana(Gestion, Textos.obtener(R.string.texto_gestion), Icons.Filled.DateRange),
        Pestana(Servicios, Textos.obtener(R.string.texto_servicios), Icons.Filled.Build),
        Pestana(Pagos, Textos.obtener(R.string.texto_pagos), Icons.Filled.ShoppingCart),
        Pestana(Mensajes, Textos.obtener(R.string.texto_mensajes), Icons.Filled.Email),
        Pestana(Perfil, Textos.obtener(R.string.texto_cuenta), Icons.Filled.Person)
    )
    Rol.ADMINISTRADOR -> listOf(
        Pestana(Validaciones, Textos.obtener(R.string.texto_negocios), Icons.Filled.CheckCircle),
        Pestana(Reportes, Textos.obtener(R.string.texto_reportes), Icons.Filled.Warning),
        Pestana(Perfil, Textos.obtener(R.string.texto_cuenta), Icons.Filled.Person)
    )
}

fun inicioDe(rol: Rol): NavKey = pestanasDe(rol).first().ruta
