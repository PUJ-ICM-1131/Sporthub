package com.sportec.sporthub.utils

import android.content.Context
import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.annotation.PluralsRes

object Textos {
    private lateinit var recursos: Resources

    fun iniciar(context: Context) {
        recursos = context.applicationContext.resources
    }

    fun cantidad(@PluralsRes id: Int, cantidad: Int, vararg argumentos: Any?): String =
        recursos.getQuantityString(id, cantidad, *argumentos)

    fun obtener(@StringRes id: Int, vararg argumentos: Any?): String =
        if (argumentos.isEmpty()) recursos.getString(id)
        else recursos.getString(id, *argumentos)
}
