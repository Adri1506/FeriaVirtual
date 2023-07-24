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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
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

class CarritoComprasActivity : AppCompatActivity(), CarritoAdapter.OnTrashClickListener {
    private lateinit var recyclerViewCarro: RecyclerView
    private lateinit var carritoAdapter: CarritoAdapter
    private lateinit var totalPriceTextView: TextView
    private lateinit var payButton: Button
    private var idCarrito: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.carrito_compras)

        recyclerViewCarro = findViewById(R.id.recyclerViewCarro)
        totalPriceTextView = findViewById(R.id.finish_text)
        payButton = findViewById(R.id.finish_button)

        val identificacion = intent.getStringExtra("identificacion")





        obtenerIdCarrito(identificacion.toString())
        obtenerProductosCarrito(identificacion.toString(), idCarrito)

        carritoAdapter = CarritoAdapter(ArrayList())
        carritoAdapter.setOnTrashClickListener(this)

        recyclerViewCarro.adapter = carritoAdapter
        recyclerViewCarro.layoutManager = LinearLayoutManager(this)



        payButton.setOnClickListener {

            val productosSeleccionados = carritoAdapter.productosSeleccionados
            val productosArray = productosSeleccionados.toTypedArray()
            val preciototal = carritoAdapter.getTotalPrice()

            val intent = Intent(this, BoletaActivity::class.java)
            intent.putExtra("identificacion", identificacion)

            intent.putExtra("preciototal", preciototal)
            Log.d(TAG,"precio $preciototal")

            startActivity(intent)
            finish()
        }
    }
    private fun updateTotalPrice() {
        val totalPrice = carritoAdapter.getTotalPrice()
        totalPriceTextView.text = "$ $totalPrice" // este es el dato que necesito caprtutrar en boleta activity
        Log.d(TAG, "totalprice : $totalPrice")

    }
    private fun obtenerIdCarrito(identificacion: String) {
        val url = "https://44.202.231.249:8080/feria-virtual/$identificacion/carrito/v10"
        Log.d(TAG, "URL id : $url")
        val client = OkHttpClient.Builder()
            .sslSocketFactory(
                createUnsafeSSLSocketFactory(),
                CarritoComprasActivity.TrustAllCerts()
            )
            .hostnameVerifier { _, _ -> true }
            .build()

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val json = response.body?.string()
                    try {
                        val jsonObject = JSONObject(json)
                        val registroArray = jsonObject.getJSONArray("registro")
                        if (registroArray.length() > 0) {
                            val registroObject = registroArray.getJSONObject(0)
                            val idCarrito = registroObject.getInt("id")
                            this@CarritoComprasActivity.idCarrito = idCarrito
                            runOnUiThread {
                                // Llamar a la función que utiliza el ID del carrito
                                // Pasar el ID del carrito como argumento a dicha función
                                // Ejemplo: funcionQueUtilizaIdCarrito(idCarrito)
                                obtenerProductosCarrito(identificacion,idCarrito)
                            }
                        } else {
                            Log.e(TAG, "El array 'registro' está vacío")
                        }
                    } catch (e: JSONException) {
                        Log.e(TAG, "Error al analizar JSON: ${e.message}", e)
                    }
                } else {
                    Log.e(TAG, "Error en la respuesta: ${response.code}")
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Error en la solicitud: ${e.message}", e)
            }
        })
    }




    private fun obtenerProductosCarrito(identificacion: String, idCarrito: Int) {
        val url = "https://44.202.231.249:8080/feria-virtual/$identificacion/carrito/v10/$idCarrito"
        Log.d(TAG, "URL carrito: $url")
        val client = OkHttpClient.Builder()
            .sslSocketFactory(createUnsafeSSLSocketFactory(), TrustAllCerts())
            .hostnameVerifier { _, _ -> true }
            .build()

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val json = response.body?.string()
                    try {
                        val jsonObject = JSONObject(json)
                        val productosArray = jsonObject.getJSONArray("producto")
                        val productosSeleccionados = ArrayList<TiendaItem>()

                        for (i in 0 until productosArray.length()) {
                            val productoObject = productosArray.getJSONObject(i)
                            val id = productoObject.getInt("id")
                            val codigo = productoObject.getString("codigo")
                            val nombre = productoObject.getString("nombre")
                            val tipo = productoObject.getString("tipo")
                            val unidadMedida = productoObject.getString("unidadMedida")
                            val precio = productoObject.getDouble("precio")
                            val estado = productoObject.getString("estado")
                            val registroInstante = productoObject.getString("registroInstante")
                            val imagen = productoObject.getString("imagen")
                            val cantidad = productoObject.getInt("cantidad")

                            val producto = TiendaItem(nombre, precio, id, cantidad)
                            productosSeleccionados.add(producto)
                        }

                        runOnUiThread {
                            carritoAdapter = CarritoAdapter(productosSeleccionados)
                            carritoAdapter.setOnTrashClickListener(this@CarritoComprasActivity)
                            recyclerViewCarro.adapter = carritoAdapter

                            updateTotalPrice()
                        }
                    } catch (e: JSONException) {
                        Log.e(TAG, "Error al analizar JSON: ${e.message}", e)
                    }
                } else {
                    Log.e(TAG, "Error en la respuesta: ${response.code}")
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Error en la solicitud: ${e.message}", e)
            }
        })
    }

    override fun onTrashClick(position: Int, productoID: Int, cantidad: Int) {
        eliminarProductoCarrito(productoID, cantidad)
        carritoAdapter.removeItem(position)
        carritoAdapter.notifyItemRemoved(position)
        updateTotalPrice()
    }


    private fun eliminarProductoCarrito(productoID: Int, cantidad: Int) {
        val identificacion = intent.getStringExtra("identificacion")

        val url = "https://44.202.231.249:8080/feria-virtual/$identificacion/carrito/v10"
        Log.d(TAG,"url: $url")
        val requestBody = createRequestBody(productoID, cantidad)

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
                    // La solicitud POST fue exitosa, puedes realizar cualquier acción adicional si es necesario
                } else {
                    // Error en la solicitud POST, manejarlo según corresponda
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                // Fallo en la solicitud POST, manejar el error según corresponda
            }
        })
    }

    private fun createRequestBody(productoID: Int, cantidad: Int): RequestBody {
        val operacion = "ELIMINAR"

        val jsonObject = JSONObject()
        jsonObject.put("cantidad", cantidad)
        jsonObject.put("operacion", operacion)
        jsonObject.put("productoID", productoID)

        Log.d(TAG,"json : ${jsonObject}")

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        return jsonObject.toString().toRequestBody(mediaType)
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



    companion object {
        private const val TAG = "CarritoComprasActivity"
    }
}

