package com.adrian.feriaapp

import android.content.ContentValues.TAG
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


class TiendaAdapter(
    private var context: TiendaActivity,
    private var productos: List<Producto>,
    private var items: MutableList<TiendaItem>,
    private val identificacion: String,
    var totalTextView: TextView? = null




) : RecyclerView.Adapter<TiendaAdapter.TiendaViewHolder>() {

    init {
        // Agrega elementos a la lista `items` según la cantidad de productos
        for (producto in productos) {
            items.add(TiendaItem(producto.nombre,producto.precio,producto.id,0)) // Puedes ajustar los valores iniciales según tus necesidades
        }
    }
    fun setProductos(productosList: List<Producto>) {
        productos = productosList
        items.clear()
        for (producto in productos) {
            items.add(TiendaItem(producto.nombre, producto.precio,producto.id, 0))
        }
        notifyDataSetChanged()
    }


    interface OnItemClickListener {
        fun onIncrementClick(position: Int)
        fun onDecrementClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TiendaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_tienda, parent, false)
        return TiendaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TiendaViewHolder, position: Int) {
        val item = items[position]
        val producto = productos[position]
        val cantidadAnterior = item.cantidad // Almacenar la cantidad anterior
        var operacion: String // Variable para almacenar la operación

        holder.bind(producto, item)

        holder.itemView.findViewById<Button>(R.id.sumButton).setOnClickListener {
            item.cantidad += 1
            holder.updateCounterTextView(item.cantidad)
            updateTotal()

            operacion = "AGREGAR"

            actualizarCarrito(producto.id, operacion, item.cantidad)
        }

        holder.itemView.findViewById<Button>(R.id.subButton).setOnClickListener {
            if (item.cantidad > 0) {
                item.cantidad -= 1
                holder.updateCounterTextView(item.cantidad)
                updateTotal()

                operacion = "ELIMINAR"

                actualizarCarrito(producto.id, operacion, item.cantidad)
            }
        }
    }


    private fun actualizarCarrito(productoID: Int, operacion: String, cantidad: Int) {
        val url = "https://44.202.231.249:8080/feria-virtual/$identificacion/carrito/v10"
        Log.d(TAG, "url: $url")
        val requestBody = createRequestBody(productoID, operacion, cantidad)

        val client = OkHttpClient.Builder()
            .sslSocketFactory(createUnsafeSSLSocketFactory(), TrustAllCerts())
            .hostnameVerifier { _, _ -> true }
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Solicitud POST exitosa $response")
                } else {
                    Log.d(TAG, "Error en la solicitud POST")
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                // Manejar el error de la solicitud
                Log.e(TAG, "Fallo en la solicitud POST: ${e.message}", e)
            }
        })
    }


    private fun createRequestBody(productoID: Int, operacion: String, cantidad: Int): RequestBody {
        val jsonObject = JSONObject()

        jsonObject.put("cantidad", 1)
        jsonObject.put("operacion", operacion)
        jsonObject.put("productoID", productoID)

        Log.d(TAG, "json $jsonObject")

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        return jsonObject.toString().toRequestBody(mediaType)
    }









    private fun updateTotal() {
        var total = 0.0
        for (item in items) {
            val subtotal = item.precio * item.cantidad
            total += subtotal
        }
        totalTextView?.text = total.toString()
    }


    override fun getItemCount(): Int {
        return productos.size
    }


    inner class TiendaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageViewProducto: ImageView = itemView.findViewById(R.id.fruit_image)
        private val textViewNombre: TextView = itemView.findViewById(R.id.fruit_name)
        private val textViewPrecio: TextView= itemView.findViewById(R.id.fruit_price)
        private val counterTextView: TextView = itemView.findViewById(R.id.counterTextView)



        fun bind(producto: Producto, item: TiendaItem) {

            val producto = productos[adapterPosition]

            itemView.findViewById<TextView>(R.id.fruit_name).text = producto.nombre
            itemView.findViewById<TextView>(R.id.fruit_price).text = producto.precio.toString()

            itemView.setOnClickListener {
                val intent = Intent(context, CarritoComprasActivity::class.java)
                intent.putExtra("nombre", producto.nombre)
                intent.putExtra("id",producto.id)
                intent.putExtra("cantidad", item.cantidad)
                context.startActivity(intent)
            }

            // Otras asignaciones de valores
            val imageUrl = asignarURLImagen(productName = producto.nombre) // Lógica para obtener la URL de la imagen según el nombre del producto
            Picasso.get().load(imageUrl).into(imageViewProducto)


            updateCounterTextView(item.cantidad)
        }

        fun updateCounterTextView(cantidad: Int) {
            counterTextView.text = cantidad.toString()
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
    // Clase interna para confiar en todos los certificados SSL
    class TrustAllCerts : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }

    // Crear un SSLSocketFactory para confiar en todos los certificados SSL
    private fun createUnsafeSSLSocketFactory(): SSLSocketFactory {
        val trustAllCerts = arrayOf<TrustManager>(TrustAllCerts())
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, trustAllCerts, SecureRandom())
        }
        return sslContext.socketFactory
    }

}

