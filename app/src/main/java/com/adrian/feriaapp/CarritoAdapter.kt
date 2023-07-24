package com.adrian.feriaapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class CarritoAdapter(val productosSeleccionados: MutableList<TiendaItem>) : RecyclerView.Adapter<CarritoAdapter.CarritoViewHolder>() {
    private var onTrashClickListener: OnTrashClickListener? = null

    inner class CarritoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombreProductoTextView: TextView = itemView.findViewById(R.id.nombreProducto)
        private val cantidadTextView: TextView = itemView.findViewById(R.id.cantidadProducto)
        private val imageViewProducto: ImageView = itemView.findViewById(R.id.fruit_image)
        private val trashBtn: ImageButton = itemView.findViewById(R.id.trashBtn)

        fun bind(producto: TiendaItem) {
            nombreProductoTextView.text = producto.nombre
            cantidadTextView.text = "${producto.cantidad}".trim()

            val imageUrl = asignarURLImagen(nombreProductoTextView)
            Picasso.get().load(imageUrl).into(imageViewProducto)

            trashBtn.setOnClickListener {
                val position = adapterPosition
                val item = productosSeleccionados[position]
                onTrashClickListener?.onTrashClick(position, item.id, item.cantidad)
            }
        }
    }

    interface OnTrashClickListener {
        fun onTrashClick(position: Int, productoID: Int, cantidad: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarritoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_cart, parent, false)
        return CarritoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarritoViewHolder, position: Int) {
        val producto = productosSeleccionados[position]
        holder.bind(producto)
    }

    override fun getItemCount(): Int {
        return productosSeleccionados.size
    }

    fun setOnTrashClickListener(listener: OnTrashClickListener) {
        onTrashClickListener = listener
    }

    fun removeItem(position: Int) {
        productosSeleccionados.removeAt(position)
    }

    fun getTotalPrice(): Double {
        var totalPrice = 0.0
        for (producto in productosSeleccionados) {
            totalPrice += producto.precio * producto.cantidad
        }
        return totalPrice
    }




    private fun asignarURLImagen(productName:TextView): String {

        return when (productName.text) {
            "manzana" -> "https://cdn.pixabay.com/photo/2018/12/07/01/53/fruit-3860991_1280.jpg"
            "pera" -> "https://cdn.pixabay.com/photo/2016/07/22/09/59/fruits-1534494_1280.jpg"
            "limon" -> "https://cdn.pixabay.com/photo/2017/02/05/12/31/lemons-2039830_1280.jpg"
            "papa" -> "https://cdn.pixabay.com/photo/2016/05/29/08/45/potato-1422580_1280.jpg"
            "naranja" -> "https://cdn.pixabay.com/photo/2012/02/19/18/05/oranges-15046_1280.jpg"
            "pimenton" -> "https://cdn.pixabay.com/photo/2016/08/03/07/38/paprika-1566052_1280.jpg"
            "damasco" -> "https://cdn.pixabay.com/photo/2016/07/30/10/51/apricots-1556851_1280.jpg"
            "platano" -> "https://cdn.pixabay.com/photo/2016/01/03/17/59/bananas-1119790_1280.jpg"
            "melon" -> "https://cdn.pixabay.com/photo/2016/03/05/22/31/food-1239305_1280.jpg"
            "ajo" -> "https://cdn.pixabay.com/photo/2017/02/25/13/24/garlic-2097759_1280.jpg"
            "repollo" -> "https://cdn.pixabay.com/photo/2019/06/16/01/30/white-4276765_1280.jpg"
            "zapallo" -> "https://cdn.pixabay.com/photo/2017/07/19/15/23/pumpkin-2519423_1280.jpg"
            "durazno" -> "https://cdn.pixabay.com/photo/2017/09/25/12/14/food-2784944_1280.jpg"
            "zanahoria" -> "https://cdn.pixabay.com/photo/2016/07/11/00/18/carrots-1508847_1280.jpg"
            "cebolla" -> "https://cdn.pixabay.com/photo/2016/05/16/22/47/onions-1397037_1280.jpg"
            "tienda1" -> "https://cdn.pixabay.com/photo/2021/05/27/18/55/woman-6289052_1280.png"
            "tienda2" -> "https://cdn.pixabay.com/photo/2014/12/21/23/41/market-575842_640.png"
            "tienda3" -> "https://cdn.pixabay.com/photo/2022/08/22/21/58/grocery-7404621_640.png"
            "tienda4" -> "https://cdn.pixabay.com/photo/2017/03/05/00/41/grocery-bag-2117313_640.png"
            // Agrega más casos según tus necesidades
            else -> "https://cdn.pixabay.com/photo/2018/05/09/21/42/vegetables-3386212_1280.jpg"
        }
    }







}
