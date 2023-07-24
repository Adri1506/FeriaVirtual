package com.adrian.feriaapp

data class Transporte(
    val agricultor: String,
    val cosechaID: Int,
    val costo: Double,
    val direccionDestino: String,
    val direccionOrigen: String,
    val estado: String,
    val fechaLlegada: String,
    val fechaSalida: String,
    val id: Int,
    val locatario: String,
    val transportista: String
)

