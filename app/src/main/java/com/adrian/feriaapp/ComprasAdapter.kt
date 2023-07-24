package com.adrian.feriaapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ComprasAdapter(private val comprasList: List<Compra>) : RecyclerView.Adapter<ComprasAdapter.CompraViewHolder>() {
    inner class CompraViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val compraIdText: TextView = itemView.findViewById(R.id.compraIdText)
        val nombreLocatarioText: TextView = itemView.findViewById(R.id.nombreTienda)
        val fechaText: TextView = itemView.findViewById(R.id.fechaCompra)
        val montoText: TextView = itemView.findViewById(R.id.precioTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompraViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_compra, parent, false)
        return CompraViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CompraViewHolder, position: Int) {
        val compra = comprasList[position]
        holder.compraIdText.text = "ID de Compra: ${compra.id}"
        holder.nombreLocatarioText.text = "Locatario: ${compra.nombreLocatario}"
        holder.fechaText.text = "Fecha: ${compra.registroInstante}"
        holder.montoText.text = "Monto: ${compra.monto}"
    }

    override fun getItemCount(): Int {
        return comprasList.size
    }
}

