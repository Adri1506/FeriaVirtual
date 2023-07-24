package com.adrian.feriaapp

import java.io.Serializable

data class TiendaItem(
    val nombre: String,
    val precio: Double,
    val id: Int,
    var cantidad: Int
) : Serializable


