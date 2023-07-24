package com.adrian.feriaapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class DetallesProductoActivity : AppCompatActivity() {
    private lateinit var frutaTextView: TextView
    private lateinit var buscarButton: Button
    private lateinit var productosRecyclerView: RecyclerView
    private lateinit var productosAdapter: ProductosAdapter
    private val productosList = mutableListOf<Producto>()
    private lateinit var identificacion : String
    private lateinit var imageUrl : ImageView
    private lateinit var identificacionLocatario : String

    private lateinit var fruta: String
    private var precio : Double = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_detalles_producto)

        // Obtener el nombre de la fruta de la actividad anterior
        fruta = intent.getStringExtra("producto_name") ?: ""
        identificacion = intent.getStringExtra("identificacion")?:""
        precio = intent.getDoubleExtra("producto_price", 0.0)
        identificacionLocatario = intent.getStringExtra("identificacionLocatario")?:""
        frutaTextView = findViewById(R.id.frutaTextView)
        imageUrl = findViewById(R.id.imageView2)
        productosRecyclerView = findViewById(R.id.productosRecyclerView)
        productosRecyclerView.layoutManager = LinearLayoutManager(this)
        productosAdapter = ProductosAdapter(productosList, onItemClick = 0, identificacion )
        productosAdapter.setOnItemClickListener(object : ProductosAdapter.OnItemClickListener {
            override fun onItemClick(producto: Producto) {
                val locatarioId = producto.id

                // Navegar a la actividad de la tienda
                val intent = Intent(this@DetallesProductoActivity, TiendaActivity::class.java)
                intent.putExtra("locatarioId", locatarioId)
                intent.putExtra("identificacion",identificacion)
                startActivity(intent)
            }
        })


        productosRecyclerView.adapter = productosAdapter

        frutaTextView.text = fruta




        buscarLocalesPorFruta(fruta)
        val imagen = asignarURLImagen(fruta)
        Picasso.get().load(imagen).into(imageUrl)

    }
    private fun buscarLocalesPorFruta(fruta: String) {
        val url = "https://44.202.231.249:8080/feria-virtual/locales/productos/v10?limit=100&offset=0&partNombre=$fruta"
        Log.d(TAG,"url $url")
        val client = OkHttpClient.Builder()
            .sslSocketFactory(
                createUnsafeSSLSocketFactory(),
                DetallesProductoActivity.TrustAllCerts()
            )
            .hostnameVerifier { _, _ -> true }
            .build()
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Error al realizar la búsqueda", Toast.LENGTH_SHORT).show()
                }
                Log.e(TAG, "Error en la solicitud: ${e.message}", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val json = response.body?.string()
                    try {
                        val jsonObject = JSONObject(json)
                        val registroArray = jsonObject.getJSONArray("registro")
                        val productoObject = registroArray.getJSONObject(0).getJSONObject("producto")
                        val locatariosArray = registroArray.getJSONObject(0).getJSONArray("locatarios")

                        val productoNombre = productoObject.getString("nombre")
                        val productos = mutableListOf<Producto>()
                        for (i in 0 until locatariosArray.length()) {
                            val locatarioObject = locatariosArray.getJSONObject(i)
                            val locatarioId = locatarioObject.getInt("id")
                            val locatarioNombre = locatarioObject.getString("nombre")
                            val locatarioPrecio = locatarioObject.getDouble("precio")

                            val producto = Producto(locatarioId,"",locatarioNombre,"FRUTA","KILOGRAMOS",locatarioPrecio,"","","")
                            Log.d(TAG,"producto , $producto")
                            productos.add(producto)
                        }

                        runOnUiThread {
                            productosList.clear()
                            productosList.addAll(productos)
                            productosAdapter.notifyDataSetChanged()
                        }
                    } catch (e: JSONException) {
                        runOnUiThread {
                            Toast.makeText(applicationContext, "Error al analizar la respuesta JSON", Toast.LENGTH_SHORT).show()
                        }
                        Log.e(TAG, "Error al analizar JSON: ${e.message}", e)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Error al realizar la búsqueda", Toast.LENGTH_SHORT).show()
                    }
                    Log.e(TAG, "Error en la respuesta: ${response.code}")
                }
            }
        })
    }

    companion object {
        private const val TAG = "DetallesProductoActivity"
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


    class TrustAllCerts : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }

    private fun createUnsafeSSLSocketFactory(): SSLSocketFactory {
        val trustAllCerts = arrayOf<TrustManager>(DetallesProductoActivity.TrustAllCerts())
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, trustAllCerts, SecureRandom())
        }
        return sslContext.socketFactory
    }
}
