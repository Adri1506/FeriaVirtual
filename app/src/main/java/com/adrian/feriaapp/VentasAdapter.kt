package com.adrian.feriaapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VentasAdapter(private val ventasList: List<Venta>) : RecyclerView.Adapter<VentasAdapter.VentaViewHolder>() {
    inner class VentaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ventaIdText: TextView = itemView.findViewById(R.id.ventaIdText)
        val nombreClienteText: TextView = itemView.findViewById(R.id.nombreCliente)
        val fechaText: TextView = itemView.findViewById(R.id.fechaVenta)
        val montoText: TextView = itemView.findViewById(R.id.precioTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VentaViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_venta, parent, false)
        return VentaViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: VentaViewHolder, position: Int) {
        val venta = ventasList[position]
        holder.ventaIdText.text = "ID de Venta: ${venta.id}"
        holder.nombreClienteText.text = "Cliente: ${venta.nombreCliente}"
        holder.fechaText.text = "Fecha: ${venta.registroInstante}"
        holder.montoText.text = "Monto: ${venta.monto}"
    }

    override fun getItemCount(): Int {
        return ventasList.size
    }
}
