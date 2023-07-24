package com.adrian.feriaapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
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

class LocatarioActivity : AppCompatActivity(), LocatarioAdapter.ProductSwitchCallback {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LocatarioAdapter
    private var productos: MutableList<Producto> = mutableListOf()
    private lateinit var identificacion: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.inicio_locatario)

        identificacion = intent.getStringExtra("identificacion")?.trim().toString()
        Log.d(TAG, "Valor de identificacion: $identificacion")

        val tipoUsuario = intent.getStringExtra("tipoUsuario")

        recyclerView = findViewById(R.id.recyclerViewLocatario)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = LocatarioAdapter(productos, this, this)
        recyclerView.adapter = adapter


        val ventasButton: Button = findViewById(R.id.tiendasButton)
        ventasButton.setOnClickListener {
            val intent = Intent(this@LocatarioActivity, VentasActivity::class.java)
            intent.putExtra("identificacion", identificacion)
            intent.putExtra("tipoUsuario", tipoUsuario)
            startActivity(intent)
        }
        val salirButton: Button = findViewById(R.id.salirButton)
        salirButton.setOnClickListener {
            val intent = Intent(this@LocatarioActivity, LoginActivity::class.java)
            intent.putExtra("identificacion", identificacion)
            intent.putExtra("tipoUsuario", tipoUsuario)
            startActivity(intent)
        }

        val productosButton: Button = findViewById(R.id.localesButton)
       productosButton.setOnClickListener {
            val intent = Intent(this@LocatarioActivity, CargaProductoActivity::class.java)
            intent.putExtra("identificacion", identificacion)
            intent.putExtra("tipoUsuario", tipoUsuario)
            startActivity(intent)
        }


        val perfilButton: Button = findViewById(R.id.perfilButton)
        perfilButton.setOnClickListener {
            // Iniciar la actividad de perfil
            val intent = Intent(this@LocatarioActivity, PerfilActivity::class.java)
            intent.putExtra("identificacion", identificacion)
            intent.putExtra("tipoUsuario", tipoUsuario)
            startActivity(intent)
        }

        obtenerProductos()
    }

    private fun obtenerProductos() {
        val client = OkHttpClient.Builder()
            .sslSocketFactory(createUnsafeSSLSocketFactory(), TrustAllCerts())
            .hostnameVerifier { _, _ -> true }
            .build()

        val url =
            "https://44.202.231.249:8080/feria-virtual/$identificacion/producto/v10?limit=100&offset=0"
        Log.d(TAG, "URL de la solicitud: $url")
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Error al obtener los productos: ${e.message}", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                Log.d(TAG, "Respuesta JSON: $responseData")

                if (responseData != null) {
                    try {
                        val gson = Gson()
                        val jsonObject = JSONObject(responseData)
                        val jsonArray = jsonObject.getJSONArray("registro")
                        val productosList = mutableListOf<Producto>()
                        for (i in 0 until jsonArray.length()) {
                            val productoJson = jsonArray.getJSONObject(i)
                            val producto =
                                gson.fromJson(productoJson.toString(), Producto::class.java)
                            productosList.add(producto)
                        }
                        runOnUiThread {
                            productos.clear()
                            productos.addAll(productosList)
                            adapter.notifyDataSetChanged()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al analizar la respuesta JSON: ${e.message}", e)
                    }
                }
            }
        })
    }

    override fun onProductSwitchChanged(producto: Producto, isChecked: Boolean) {
        val nuevoEstado = if (isChecked) "INHABILITADO" else "HABILITADO"
        val productoId: Int = producto.id
        Log.d(TAG, "producto: $producto")
        actualizarEstadoProducto(productoId, nuevoEstado)
    }

    private fun actualizarEstadoProducto(productoId: Int, nuevoEstado: String) {
        val client = OkHttpClient.Builder()
            .sslSocketFactory(createUnsafeSSLSocketFactory(), TrustAllCerts())
            .hostnameVerifier { _, _ -> true }
            .build()

        val url = "https://44.202.231.249:8080/feria-virtual/$identificacion/producto/v10/$productoId"
        Log.d(TAG, "URL de la solicitud: $url")

        val jsonObject = JSONObject()
        jsonObject.put("estado", nuevoEstado)

        val requestBody =
            jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Error al actualizar el estado del producto: ${e.message}", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                Log.d(TAG, "Respuesta JSON: $responseData")

                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(
                            this@LocatarioActivity,
                            "Estado del producto actualizado",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this@LocatarioActivity,
                            "producto actualizado",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    private fun createUnsafeSSLSocketFactory(): SSLSocketFactory {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        return sslContext.socketFactory
    }

    private class TrustAllCerts : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }

    companion object {
        private const val TAG = "LocatarioActivity"
    }
}