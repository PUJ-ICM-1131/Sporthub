package com.sportec.sporthub.utils

import com.sportec.sporthub.domain.ModoViaje
import com.sportec.sporthub.domain.Trayecto
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import org.json.JSONObject

object DireccionesMapbox {
    private val cliente by lazy {
        HttpClient(Android) {
            install(HttpTimeout) {
                connectTimeoutMillis = 8000
                requestTimeoutMillis = 8000
            }
        }
    }

    fun tokenValido(token: String): Boolean = token.startsWith("pk.")

    suspend fun consultar(token: String, negocioId: String, modo: ModoViaje, origen: Coordenada, destino: Coordenada): Trayecto? {
        if (!tokenValido(token)) return null
        return try {
            val coordenadas = "${origen.longitud},${origen.latitud};${destino.longitud},${destino.latitud}"
            val respuesta = cliente.get("https://api.mapbox.com/directions/v5/mapbox/${modo.perfilMapbox}/$coordenadas") {
                parameter("geometries", "geojson")
                parameter("overview", "full")
                parameter("language", "es")
                parameter("access_token", token)
            }
            if (respuesta.status != HttpStatusCode.OK) null
            else interpretar(respuesta.bodyAsText(), negocioId, modo, origen)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
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
