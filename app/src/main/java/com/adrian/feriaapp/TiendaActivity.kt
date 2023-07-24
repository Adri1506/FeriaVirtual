package com.adrian.feriaapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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



class TiendaActivity : AppCompatActivity() {
    private lateinit var recyclerViewTienda: RecyclerView
    private lateinit var tiendaAdapter: TiendaAdapter
    private lateinit var totalTextView: TextView
    private lateinit var identificacion: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.tienda_local)
        val tipoUsuario = intent.getStringExtra("tipoUsuario")
        val identificacionLocatario = intent.getStringExtra("identificacionLocatario")?.trim()
        Log.d("TiendaActivity", "IdentificacionLocatario: $identificacionLocatario")
        val nombreLocatario = intent.getStringExtra("nombre")
        val identificacion = intent.getStringExtra("identificacion")?.trim()
        Log.d(TAG, "Valor de identificacion Tiendaactivity : $identificacion")


        val client = OkHttpClient.Builder()
            .sslSocketFactory(createUnsafeSSLSocketFactory(), TrustAllCerts())
            .hostnameVerifier { _, _ -> true }
            .build()

        val url = "https://44.202.231.249:8080/feria-virtual/${identificacionLocatario}/producto/v10?limit=100&offset=0"
        Log.d(TAG, "URL de la solicitud: $url")
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val statusCode = response.code.toString()
                Log.d("TAG", "Status: $statusCode")
                val responseData = response.body?.string()
                Log.d("DatosRecibidos", responseData.toString())

                try {
                    // Parsea la respuesta JSON
                    val jsonObject = JSONObject(responseData)
                    val jsonArray = jsonObject.getJSONArray("registro")

                    val productosList: MutableList<Producto> = mutableListOf()

                    // Itera sobre los objetos en el arreglo
                    for (i in 0 until jsonArray.length()) {
                        val productoJson = jsonArray.getJSONObject(i)
                        val id = productoJson.optInt("id")
                        val codigo = productoJson.optString("codigo")
                        val nombre = productoJson.optString("nombre")
                        val precio = productoJson.optDouble("precio")
                        val categoria = productoJson.optString("categoria")
                        val estado = productoJson.optString("estado")
                        // ... Obtén los demás campos del producto

                        // Crea un objeto Producto y agrégalo a la lista
                        if (estado != "INHABILITADO") {
                            // Crea un objeto Producto y agrégalo a la lista solo si el estado no es "INHABILITADO"
                            val producto = Producto(id, codigo, nombre, categoria, "KILOGRAMOS", precio, estado, "", "")
                            productosList.add(producto)
                        }
                    }

                    runOnUiThread {
                        // Actualiza el adaptador con la nueva lista de productos
                        tiendaAdapter.setProductos(productosList)
                    }
                } catch (e: JSONException) {
                    Log.e("TAG", "Error al analizar JSON: ${e.message}", e)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("TAG", "Error: ${e.message}", e)
                // Manejar el error de la solicitud
            }
        })

        recyclerViewTienda = findViewById(R.id.recyclerViewTienda)
        totalTextView = findViewById(R.id.text_total)

        val items: MutableList<TiendaItem> = mutableListOf()
        tiendaAdapter = TiendaAdapter(this, emptyList(), items,identificacion.toString(),totalTextView)
        recyclerViewTienda.adapter = tiendaAdapter
        recyclerViewTienda.layoutManager = LinearLayoutManager(this)
        tiendaAdapter.totalTextView = totalTextView

        val payButton = findViewById<Button>(R.id.pay_button)
        payButton.setOnClickListener {
            Log.d(TAG, "Pay button clicked")

            val productosSeleccionados = items.filter { it.cantidad > 0 }.toString()
            Log.d(TAG, "Productos seleccionados: $productosSeleccionados")
            val intent = Intent(this, CarritoComprasActivity::class.java)
            Log.d(TAG, "identificacion: $identificacion")
            intent.putExtra("identificacion", identificacion)
            intent.putExtra("productosSeleccionados", productosSeleccionados)
            startActivity(intent)




        }
    }

    companion object {
        private const val TAG = "TiendaActivity" // Agregar TAG para el log
    }

    private fun createUnsafeSSLSocketFactory(): SSLSocketFactory {
        val trustAllCerts = arrayOf<TrustManager>(TrustAllCerts())
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, trustAllCerts, SecureRandom())
        }
        return sslContext.socketFactory
    }

    private class TrustAllCerts : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }
}

