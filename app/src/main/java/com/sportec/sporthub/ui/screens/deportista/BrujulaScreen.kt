package com.sportec.sporthub.ui.screens.deportista

import com.sportec.sporthub.R
import com.sportec.sporthub.utils.Textos
import androidx.compose.ui.res.stringResource

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportec.sporthub.domain.Negocios
import com.sportec.sporthub.domain.Ubicacion
import com.sportec.sporthub.ui.components.Aviso
import com.sportec.sporthub.ui.components.EstadoVacio
import com.sportec.sporthub.ui.components.PantallaBase
import com.sportec.sporthub.ui.components.TipoAviso

@Composable
fun BrujulaScreen(negocioId: String, onBack: () -> Unit) {
    val negocio = Negocios.negocioDe(negocioId)
    val origen by Ubicacion.origen.collectAsState()

    val transicion = rememberInfiniteTransition(label = "brujula")
    val giro by transicion.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "giro"
    )

    PantallaBase(titulo = Textos.obtener(R.string.texto_como_llegar), onBack = onBack) { padding ->
        if (negocio == null) {
            EstadoVacio(
                mensaje = Textos.obtener(R.string.texto_el_establecimiento_ya_no_existe),
                modifier = Modifier.padding(padding)
            )
        } else {
            val distancia = Ubicacion.distanciaKm(origen, negocio.latitud, negocio.longitud)
            val rumbo = Ubicacion.rumboGrados(origen, negocio.latitud, negocio.longitud)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(negocio.nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = stringResource(R.string.formato_distancia_origen).format(distancia, origen.nombre),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = Icons.Filled.Navigation,
                    contentDescription = Textos.obtener(R.string.texto_direccion_hacia_el_establecimiento),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(160.dp)
                        .graphicsLayer { rotationZ = rumbo.toFloat() + giro }
                )
                Text(
                    text = stringResource(R.string.brujula_rumbo, rumbo.toInt()),
                    style = MaterialTheme.typography.titleMedium
                )
                Aviso(
                    texto = stringResource(R.string.brujula_simulada),
                    tipo = TipoAviso.ADVERTENCIA,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
