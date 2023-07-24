package com.adrian.feriaapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

class LocatarioAdapter(
    private val productos: List<Producto>,
    private val activity: LocatarioActivity,
    private val productSwitchCallback: ProductSwitchCallback
) :
    RecyclerView.Adapter<LocatarioAdapter.LocatarioViewHolder>() {

    interface ProductSwitchCallback {
        fun onProductSwitchChanged(producto: Producto, isChecked: Boolean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocatarioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return LocatarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocatarioViewHolder, position: Int) {
        val producto = productos[position]
        holder.bind(producto)
    }

    override fun getItemCount(): Int {
        return productos.size
    }

    inner class LocatarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productImage: ImageView = itemView.findViewById(R.id.fruit_image)
        private val productName: TextView = itemView.findViewById(R.id.productName)
        private val productPrice: TextView = itemView.findViewById(R.id.productPrice)
        private val productSwitch: Switch = itemView.findViewById(R.id.productSwitch)

        fun bind(producto: Producto) {
            // Setear los valores de los elementos de la lista
            productName.text = producto.nombre
            productPrice.text = producto.precio.toString()
            productSwitch.isChecked = producto.estado == "HABILITADO"

            val imageUrl = asignarURLImagen(productName = producto.nombre) // Lógica para obtener la URL de la imagen según el nombre del producto
            Picasso.get().load(imageUrl).into(productImage)

            // Configurar listener para el interruptor
            productSwitch.isChecked = producto.estado == "HABILITADO"
            productSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    producto.estado = "HABILITADO"
                } else {
                    producto.estado = "INHABILITADO"
                }

                productSwitchCallback.onProductSwitchChanged(producto, isChecked)
            }
        }
    }

    fun asignarURLImagen(productName: String): String {
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

            // Agrega más casos según tus necesidades
            else -> "https://cdn.pixabay.com/photo/2018/05/09/21/42/vegetables-3386212_1280.jpg"
        }
    }

    private class TrustAllCerts : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }
}
