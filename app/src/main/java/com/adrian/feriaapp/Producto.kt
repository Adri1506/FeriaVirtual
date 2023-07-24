package com.adrian.feriaapp

data class Producto(

    var id: Int,
    var codigo: String,
    var nombre: String,
    var tipo: String,
    var unidadMedida: String,
    var precio: Double,
    var estado: String,
    var registroInstante: String,
    var imagen: String
)

