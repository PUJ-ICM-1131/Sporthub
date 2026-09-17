package com.sportec.sporthub.data.mock

import com.sportec.sporthub.data.model.ActividadConNegocio
import com.sportec.sporthub.data.model.Categoria
import com.sportec.sporthub.data.model.Cuenta
import com.sportec.sporthub.data.model.EstadoNegocio
import kotlinx.coroutines.delay

class ErrorMock(mensaje: String) : Exception(mensaje)

object RepositorioAutenticacionMock {

    var simularError: Boolean = false

    suspend fun iniciarSesion(usuario: String, contrasena: String): Result<Cuenta> {
        delay(700)
        if (simularError) {
            return Result.failure(ErrorMock("El servicio no está disponible. Intenta de nuevo."))
        }
        val clave = usuario.trim().lowercase()
        val cuenta = DatosMock.cuentas.firstOrNull { it.id == clave || it.email == clave }
        if (cuenta == null || cuenta.contrasena != contrasena) {
            return Result.failure(ErrorMock("Correo o contraseña incorrectos."))
        }
        if (!cuenta.activa) {
            return Result.failure(ErrorMock("Esta cuenta no tiene acceso."))
        }
        return Result.success(cuenta)
    }
}

data class ResultadoExplorar(
    val categorias: List<Categoria>,
    val actividades: List<ActividadConNegocio>
)

object RepositorioCatalogoMock {

    var simularError: Boolean = false

    suspend fun explorar(categoria: String?): Result<ResultadoExplorar> {
        delay(500)
        if (simularError) {
            return Result.failure(ErrorMock("No se pudo cargar el catálogo."))
        }
        val validados = DatosMock.negocios
            .filter { it.estado == EstadoNegocio.VALIDADO }
            .associateBy { it.id }
        val actividades = DatosMock.actividades
            .filter { categoria == null || it.categoria == categoria }
            .mapNotNull { actividad ->
                validados[actividad.negocioId]?.let { ActividadConNegocio(actividad, it) }
            }
        return Result.success(ResultadoExplorar(DatosMock.categorias, actividades))
    }

    suspend fun obtener(actividadId: String): Result<ActividadConNegocio> {
        delay(400)
        if (simularError) {
            return Result.failure(ErrorMock("No se pudo cargar el servicio."))
        }
        val actividad = DatosMock.actividades.firstOrNull { it.id == actividadId }
            ?: return Result.failure(ErrorMock("El servicio ya no está disponible."))
        val negocio = DatosMock.negocios.firstOrNull { it.id == actividad.negocioId }
            ?: return Result.failure(ErrorMock("El establecimiento ya no está disponible."))
        return Result.success(ActividadConNegocio(actividad, negocio))
    }
}
