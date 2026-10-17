package com.sportec.sporthub.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.sportec.sporthub.R

@Composable
fun CarruselFotos(fotos: List<String>, onQuitar: ((String) -> Unit)? = null) {
    if (fotos.isEmpty()) return
    val pager = rememberPagerState(pageCount = { fotos.size })
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalPager(state = pager, modifier = Modifier.fillMaxWidth()) { pagina ->
            AsyncImage(model = fotos[pagina], contentDescription = stringResource(R.string.fotos_numero, pagina + 1, fotos.size),
                contentScale = ContentScale.Fit, modifier = Modifier.fillMaxWidth().height(220.dp).background(MaterialTheme.colorScheme.surfaceContainerHigh))
        }
        Text(stringResource(R.string.fotos_numero, pager.currentPage.coerceAtMost(fotos.lastIndex) + 1, fotos.size))
        if (onQuitar != null) TextButton(onClick = { fotos.getOrNull(pager.currentPage)?.let(onQuitar) }) {
            Text(stringResource(R.string.fotos_quitar))
        }
    }
}
