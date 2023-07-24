package com.adrian.feriaapp

data class Pedido(
    val id: Int,
    val fecha: String,
    val productos: List<Producto>,
    val estado: String,
    val total: Double
)