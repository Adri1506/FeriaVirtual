package com.adrian.feriaapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductosAdapter(
    private val productosList: List<Producto>,
    private val onItemClick: Int,
    private val identificacion: String,
    private var onItemClickListener: OnItemClickListener? = null

) : RecyclerView.Adapter<ProductosAdapter.ProductoViewHolder>() {

    inner class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagenProducto: ImageView = itemView.findViewById(R.id.imagenImageView)
        val nombreLocatario: TextView = itemView.findViewById(R.id.nombreTextView)
        val precioProducto: TextView = itemView.findViewById(R.id.precioTextView)
        val irTiendaButton: Button = itemView.findViewById(R.id.irTiendaButton)

        init {
            irTiendaButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val producto = productosList[position]
                    onItemClickListener?.onItemClick(producto)
                }
            }

        }

    }

    interface OnItemClickListener {
        fun onItemClick(producto: Producto)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productosList[position]




        holder.nombreLocatario.text = producto.nombre
        holder.precioProducto.text = "Precio: ${producto.precio}"
    }

    override fun getItemCount(): Int {
        return productosList.size
    }
}
