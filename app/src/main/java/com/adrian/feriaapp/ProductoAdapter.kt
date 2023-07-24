package com.adrian.feriaapp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso


class ProductoAdapter(private val context: Context, private val productos: List<Producto>, private val identificacionLocatario: String) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_producto, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]
        holder.bind(producto.nombre, producto.precio)
    }

    override fun getItemCount(): Int {
        return productos.size
    }

    inner class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageViewProducto: ImageView = itemView.findViewById(R.id.fruit_image)
        private val textViewNombre: TextView = itemView.findViewById(R.id.fruit_name)
        private val textViewPrecio: TextView = itemView.findViewById(R.id.fruit_price)
        private val buttonGoToProduct: Button = itemView.findViewById(R.id.tiendasButton)


        fun bind(productoNombre: String, productoPrecio:Double) {

            textViewNombre.text = productoNombre
            textViewPrecio.text = "Precio:$productoPrecio"

            val imageUrl = asignarURLImagen(productoNombre) // Lógica para obtener la URL de la imagen según el nombre del producto
            Picasso.get().load(imageUrl).into(imageViewProducto)

            itemView.setOnClickListener {
                val intent = Intent(context, DetallesProductoActivity::class.java)
                intent.putExtra("producto_name", productoNombre)
                intent.putExtra("producto_price", productoPrecio)
                intent.putExtra("idlocal",identificacionLocatario)




                context.startActivity(intent)
            }



        }
    }

    private fun asignarURLImagen(productName: String): String {
        // Lógica para asignar la URL de la imagen según el nombre del producto
        return when (productName) {
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

