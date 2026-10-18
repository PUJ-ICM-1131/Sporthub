package com.sportec.sporthub.utils

import com.sportec.sporthub.domain.ModoViaje
import com.sportec.sporthub.domain.Trayecto
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/** Cliente mínimo de la Directions API de Mapbox. Devuelve null si no hay token, red o ruta. */
object DireccionesMapbox {
    fun tokenValido(token: String): Boolean = token.startsWith("pk.")

    suspend fun consultar(token: String, negocioId: String, modo: ModoViaje, origen: Coordenada, destino: Coordenada): Trayecto? {
        if (!tokenValido(token)) return null
        return withContext(Dispatchers.IO) {
            runCatching {
                val coordenadas = "${origen.longitud},${origen.latitud};${destino.longitud},${destino.latitud}"
                val url = URL(
                    "https://api.mapbox.com/directions/v5/mapbox/${modo.perfilMapbox}/$coordenadas" +
                        "?geometries=geojson&overview=full&language=es&access_token=${URLEncoder.encode(token, "UTF-8")}"
                )
                val conexion = url.openConnection() as HttpURLConnection
                try {
                    conexion.connectTimeout = 8000
                    conexion.readTimeout = 8000
                    if (conexion.responseCode != HttpURLConnection.HTTP_OK) return@runCatching null
                    interpretar(conexion.inputStream.bufferedReader().use { it.readText() }, negocioId, modo, origen)
                } finally {
                    conexion.disconnect()
                }
            }.getOrNull()
        }
    }

    private fun interpretar(json: String, negocioId: String, modo: ModoViaje, origen: Coordenada): Trayecto? {
        val rutas = JSONObject(json).optJSONArray("routes") ?: return null
        if (rutas.length() == 0) return null
        val ruta = rutas.getJSONObject(0)
        val coordenadas = ruta.getJSONObject("geometry").getJSONArray("coordinates")
        val puntos = (0 until coordenadas.length()).map {
            val par = coordenadas.getJSONArray(it)
            Coordenada(latitud = par.getDouble(1), longitud = par.getDouble(0))
        }
        if (puntos.size < 2) return null
        return Trayecto(negocioId, modo, origen, puntos, ruta.getDouble("distance"), ruta.getDouble("duration"), estimado = false)
    }
}
