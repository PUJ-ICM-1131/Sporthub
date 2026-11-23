package com.sportec.sporthub.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.sportec.sporthub.domain.Reservas
import com.sportec.sporthub.domain.Transferencias
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private const val INTERVALO_MS = 30_000L

@Composable
fun RevisionDeTurnos(usuarioId: String?) {
    LaunchedEffect(usuarioId) {
        if (usuarioId == null) return@LaunchedEffect
        while (isActive) {
            Reservas.revisarVencimientos()
            Transferencias.revisarVencimientos()
            delay(INTERVALO_MS)
        }
    }
}
