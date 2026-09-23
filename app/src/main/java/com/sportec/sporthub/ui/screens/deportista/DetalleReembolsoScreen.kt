package com.sportec.sporthub.ui.screens.deportista

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos
import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sportec.sporthub.domain.Reservas
import com.sportec.sporthub.ui.components.Aviso
import com.sportec.sporthub.ui.components.EstadoVacio
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.TarjetaHechos
import com.sportec.sporthub.ui.components.TipoAviso
import com.sportec.sporthub.utils.formatoPesos

@Composable
fun DetalleReembolsoScreen(reembolsoId: String, onBack: () -> Unit) {
    val reembolsos by Reservas.reembolsos.collectAsState()
    val reembolso = reembolsos.firstOrNull { it.id == reembolsoId }

    PantallaBase(titulo = Textos.obtener(R.string.reembolso_titulo), onBack = onBack) { padding ->
        if (reembolso == null) {
            EstadoVacio(
                mensaje = stringResource(R.string.reembolso_no_encontrado),
                modifier = Modifier.padding(padding)
            )
            return@PantallaBase
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TarjetaHechos(
                pares = listOf(
                    stringResource(R.string.reembolso_monto) to formatoPesos(reembolso.monto),
                    stringResource(R.string.reembolso_causa) to reembolso.causa,
                    stringResource(R.string.reembolso_estado) to reembolso.estado.name
                )
            )
            Aviso(texto = stringResource(R.string.reembolso_aviso), tipo = TipoAviso.NORMAL)
        }
    }
}
