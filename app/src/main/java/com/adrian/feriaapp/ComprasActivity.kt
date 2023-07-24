package com.adrian.feriaapp

import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Toast
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

class ComprasActivity : AppCompatActivity() {
    private lateinit var recyclerViewCompras: RecyclerView
    private lateinit var comprasAdapter: ComprasAdapter
    private val comprasList = mutableListOf<Compra>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_compras)

        // Inicializar RecyclerView
        recyclerViewCompras = findViewById(R.id.recyclerViewCompras)
        recyclerViewCompras.layoutManager = LinearLayoutManager(this)
        comprasAdapter = ComprasAdapter(comprasList)
        recyclerViewCompras.adapter = comprasAdapter

        // Obtener identificación del usuario
        val identificacion = intent.getStringExtra("identificacion") ?: ""

        // Realizar la solicitud HTTP para obtener la lista de compras
        obtenerCompras(identificacion)
    }

    private fun obtenerCompras(identificacion: String) {
        val url = "https://44.202.231.249:8080/feria-virtual/$identificacion/venta/cliente/v10?limit=100&offset=0"
        val client = OkHttpClient.Builder()
            .sslSocketFactory(
                createUnsafeSSLSocketFactory(),
                ComprasActivity.TrustAllCerts()
            )
            .hostnameVerifier { _, _ -> true }
            .build()
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Error al obtener las compras", Toast.LENGTH_SHORT).show()
                }
                Log.e(TAG, "Error en la solicitud: ${e.message}", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val json = response.body?.string()
                    try {
                        val jsonObject = JSONObject(json)
                        val registroArray = jsonObject.getJSONArray("registro")
                        val compras = mutableListOf<Compra>()
                        for (i in 0 until registroArray.length()) {
                            val compraObject = registroArray.getJSONObject(i)
                            val id = compraObject.getInt("id")
                            val locatarioObject = compraObject.getJSONObject("locatario")
                            val nombreLocatario = locatarioObject.getString("nombre")
                            val registroInstante = compraObject.getString("registroInstante")
                            val monto = compraObject.getDouble("monto")

                            val compra = Compra(id, nombreLocatario, registroInstante, monto)
                            Log.d(TAG," $id, $nombreLocatario, $registroInstante, $monto")

                            compras.add(compra)

                        }
                        runOnUiThread {
                            comprasList.clear()
                            comprasList.addAll(compras)
                            comprasAdapter.notifyDataSetChanged()
                        }
                    } catch (e: JSONException) {
                        runOnUiThread {
                            Toast.makeText(applicationContext, "Error al analizar la respuesta JSON", Toast.LENGTH_SHORT).show()
                        }
                        Log.e(TAG, "Error al analizar JSON: ${e.message}", e)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Error al obtener las compras", Toast.LENGTH_SHORT).show()
                    }
                    Log.e(TAG, "Error en la respuesta: ${response.code}")
                }
            }
        })
    }




    companion object {
        private const val TAG = "ComprasActivity"
    }
    class TrustAllCerts : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }

    // Crear un SSLSocketFactory para confiar en todos los certificados SSL
    private fun createUnsafeSSLSocketFactory(): SSLSocketFactory {
        val trustAllCerts = arrayOf<TrustManager>(BoletaActivity.TrustAllCerts())
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, trustAllCerts, SecureRandom())
        }
        return sslContext.socketFactory
    }

}

private fun <E> Collection<E>.addAll(elements: MutableList<String>) {

}
