package com.sportec.sporthub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.sportec.sporthub.R

@Composable
fun AvatarChat(fotoUri: String?, nombre: String, esGrupo: Boolean = false, tamano: Dp = 48.dp) {
    var fallo by remember(fotoUri) { mutableStateOf(false) }
    Box(Modifier.size(tamano).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHigh), contentAlignment = Alignment.Center) {
        if (fotoUri == null || fallo) {
            Icon(
                imageVector = if (esGrupo) Icons.Filled.Groups else Icons.Filled.Person,
                contentDescription = stringResource(if (esGrupo) R.string.avatar_grupo_sin_foto else R.string.avatar_persona_sin_foto, nombre),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(tamano * 0.6f)
            )
        } else {
            AsyncImage(model = fotoUri, contentDescription = stringResource(R.string.avatar_foto, nombre),
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize(), onError = { fallo = true })
        }
    }
}
