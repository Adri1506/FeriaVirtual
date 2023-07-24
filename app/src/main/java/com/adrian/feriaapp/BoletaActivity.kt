package com.adrian.feriaapp

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.TextView
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
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class BoletaActivity : AppCompatActivity(), BoletaAdapter.OnDireccionSelectedListener  {
    private val direccionList = mutableListOf<Direccion>()
    private val productoList = mutableListOf<TiendaItem>()
    private val tiendaItems: MutableList<TiendaItem> = mutableListOf()

    private lateinit var nombreText: TextView
    private lateinit var telefonoText: TextView
    private lateinit var precioTotalText: TextView
    private var nombresProductos: List<String> = emptyList()
    private var cantidades: List<Int> = emptyList()
    private lateinit var boletaAdapter: BoletaAdapter
    private lateinit var boletaInfoAdapter: BoletaInfoAdapter
    private lateinit var productoAdapter: ProductoAdapter
    private lateinit var productos: Producto
    private var selectedDireccion: Direccion? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_boleta)


        // Obtener datos de la intención
        val identificacion = intent.getStringExtra("identificacion")?.trim().toString()
        val preciototal = intent.getDoubleExtra("preciototal",0.0)
        Log.d(TAG, "Identificación: $identificacion, Precio Total: $preciototal")


        obtenerIdCarrito(identificacion.toString())
        obtenerProductosCarrito(identificacion.toString(), idCarrito = 0)


        // Inicializar vistas
        nombreText = findViewById(R.id.nombreText)
        telefonoText = findViewById(R.id.telefonoText)
        precioTotalText = findViewById(R.id.precioTotal)

        // Configurar RecyclerView para direcciones
        val recyclerViewBoleta: RecyclerView = findViewById(R.id.recyclerViewBoleta)
        recyclerViewBoleta.layoutManager = LinearLayoutManager(this)
        boletaAdapter = BoletaAdapter(direccionList)
        recyclerViewBoleta.adapter = boletaAdapter
        // Configurar RecyclerView para productos
        val recyclerViewBoletaInfo: RecyclerView = findViewById(R.id.recyclerViewBoletaInfo)
        recyclerViewBoletaInfo.layoutManager = LinearLayoutManager(this)
        boletaInfoAdapter = BoletaInfoAdapter()
        recyclerViewBoletaInfo.adapter = boletaInfoAdapter


        boletaInfoAdapter.setItems(tiendaItems)
        // Obtener y mostrar datos del usuario y direcciones
        getUserData(identificacion)
        getUserAddresses(identificacion)
        obtenerProductosCarrito(identificacion, idCarrito = 0)


        // Mostrar precio total
        precioTotalText.text = ":$ $preciototal"

        val saleBtn: Button = findViewById(R.id.saleBtn)
        saleBtn.setOnClickListener {
            val idCarrito = obtenerIdCarrito(identificacion)?.toInt() // Obtener el ID del carrito
            val idDireccion = boletaAdapter.selectedDireccionId // Obtener el ID de la dirección seleccionada
            val monto = preciototal




            if (idCarrito != null && idDireccion != null) {
                concretarVenta(preciototal.toInt(), idDireccion, identificacion, monto) // Llamar a la función concretarVenta


            } else {
                Toast.makeText(
                    this,
                    "Error al obtener el ID del carrito o la dirección seleccionada",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }



        boletaAdapter.setOnDireccionSelectedListener(object : BoletaAdapter.OnDireccionSelectedListener {
            override fun onDireccionSelected(direccion: Direccion) {
                selectedDireccion = direccion
            }
        })



     }
    override fun onDireccionSelected(direccion: Direccion) {
        selectedDireccion = direccion
    }
    private var idCarritoObtenido = -1
    private fun obtenerIdCarrito(identificacion: String) {
        val url = "https://44.202.231.249:8080/feria-virtual/$identificacion/carrito/v10"
        Log.d(TAG, "URL id : $url")
        val client = OkHttpClient.Builder()
            .sslSocketFactory(
                createUnsafeSSLSocketFactory(),
                BoletaActivity.TrustAllCerts()
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
                            var idCarrito = registroObject.getInt("id")
                            Log.d(TAG,"id carro : $idCarrito")



                            runOnUiThread {
                                idCarritoObtenido = idCarrito
                                obtenerProductosCarrito(identificacion, idCarritoObtenido)




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
        Log.d(TAG, "URL: $url")
        val client = OkHttpClient.Builder()
            .sslSocketFactory(
                createUnsafeSSLSocketFactory(),
                BoletaActivity.TrustAllCerts()
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
                            Log.d(TAG, "tiendaitem: $producto")
                            productosSeleccionados.add(producto)
                        }

                        runOnUiThread {
                            tiendaItems.clear()
                            tiendaItems.addAll(productosSeleccionados)
                            updateTotalPrice()
                            mostrarProductos()
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
    private fun updateTotalPrice() {
        val totalPrice = tiendaItems.sumByDouble { it.precio * it.cantidad }
        precioTotalText.text = "$ $totalPrice"


    }
    private fun mostrarProductos() {
        runOnUiThread {
            boletaInfoAdapter.setItems(tiendaItems)
            boletaInfoAdapter.notifyDataSetChanged()
        }
    }
    private fun concretarVenta( preciototal: Int, idDireccion: Int, identificacion: String, monto: Double) {

            val requestBody = JSONObject()
            requestBody.put("carritoID" , idCarritoObtenido)
            requestBody.put("direccionID", idDireccion)
            requestBody.put("monto", preciototal.toDouble())
            Log.d(TAG, "json : $requestBody ")

            // Realizar la solicitud POST
            val url = "https://44.202.231.249:8080/feria-virtual/$identificacion/venta/cliente/v10"
            Log.d(TAG, "url concretarventa : $url")
            val client = OkHttpClient.Builder()
                .sslSocketFactory(createUnsafeSSLSocketFactory(), TrustAllCerts())
                .hostnameVerifier { _, _ -> true }
                .build()

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBodyString = requestBody.toString()
            val request = Request.Builder()
                .url(url)
                .post(requestBodyString.toRequestBody(mediaType))
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // Manejar el error de la solicitud
                    Log.e(TAG, "Error: ${e.message}", e)
                    runOnUiThread {
                        Toast.makeText(
                            applicationContext,
                            "Error al concretar la venta",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    // Manejar la respuesta del servidor
                    val statusCode = response.code.toString()
                    Log.d(TAG, "Status: $statusCode")
                    runOnUiThread {
                        if (response.isSuccessful) {
                            Toast.makeText(
                                applicationContext,
                                "Venta concretada exitosamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this@BoletaActivity, UsuarioMainActivity::class.java)

                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Error al concretar la venta",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            })


    }
            // Obtener datos del usuario
    private fun getUserData(identificacion: String) {
        val client = OkHttpClient.Builder()
            .sslSocketFactory(createUnsafeSSLSocketFactory(), TrustAllCerts())
            .hostnameVerifier { _, _ -> true }
            .build()

        val url = "https://44.202.231.249:8080/feria-virtual/admin/usuario/v10?identificacion=$identificacion"
        Log.d(TAG, "URL: $url")
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Manejar el error de la solicitud
                Log.e(TAG, "Error: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(applicationContext, "Error al obtener los datos del usuario", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // Manejar la respuesta del servidor
                val statusCode = response.code.toString()
                Log.d(TAG, "Status: $statusCode")
                val responseData = response.body?.string()
                runOnUiThread {
                    Log.d(TAG, "Response Data: $responseData")
                    if (response.isSuccessful) {
                        // Analizar la respuesta JSON y mostrar los datos del usuario
                        val gson = Gson()
                        val jsonArray = JSONObject(responseData).getJSONArray("registro")
                        val jsonObject = jsonArray.getJSONObject(0)
                        val usuario = gson.fromJson(jsonObject.toString(), Usuario::class.java)
                        Log.d(TAG, "Usuario: $usuario")

                        nombreText.text = "Nombre: ${usuario.nombre}"
                        telefonoText.text = "Teléfono: ${usuario.telefono}"
                    } else {
                        Toast.makeText(applicationContext, "Error al obtener los datos del usuario", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    // Obtener direcciones del usuario
    private fun getUserAddresses(identificacion: String) {
        val client = OkHttpClient.Builder()
            .sslSocketFactory(createUnsafeSSLSocketFactory(), TrustAllCerts())
            .hostnameVerifier { _, _ -> true }
            .build()

        val url = "https://44.202.231.249:8080/feria-virtual/$identificacion/direccion/v10"
        Log.d(TAG, "URL: $url")
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Manejar el error de la solicitud
                Log.e(TAG, "Error: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(applicationContext, "Error al obtener las direcciones del usuario", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // Manejar la respuesta del servidor
                val statusCode = response.code.toString()
                Log.d(TAG, "Status: $statusCode")
                val responseData = response.body?.string()
                runOnUiThread {
                    Log.d(TAG, "Response Data: $responseData")

                    if (response.isSuccessful) {
                        try {
                            val jsonObject = JSONObject(responseData)
                            if (jsonObject.has("registro")) {
                                val jsonArray = jsonObject.getJSONArray("registro")
                                val addresses = mutableListOf<Direccion>()

                                for (i in 0 until jsonArray.length()) {
                                    val gson = Gson()
                                    val direccion = gson.fromJson(jsonArray.getJSONObject(i).toString(), Direccion::class.java)
                                    addresses.add(direccion)
                                }

                                // Mostrar las direcciones en el RecyclerView correspondiente
                                showUserAddresses(addresses)
                            } else {
                                Toast.makeText(applicationContext, "La respuesta del servidor no contiene el campo 'registro'", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: JSONException) {
                            Toast.makeText(applicationContext, "Error al analizar la respuesta JSON", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(applicationContext, "Error al obtener las direcciones del usuario", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    // Mostrar direcciones en el RecyclerView
    private fun showUserAddresses(addresses: List<Direccion>) {
        direccionList.clear() // Limpiar la lista existente
        direccionList.addAll(addresses) // Agregar las nuevas direcciones
        if (selectedDireccion != null && !direccionList.contains(selectedDireccion)) {
            selectedDireccion = null
        }

        boletaAdapter.notifyDataSetChanged() // Notificar al adaptador de los cambios
        Log.d(TAG, "Direcciones mostradas: ${addresses.size}")
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

    private fun Unit.toInt(): Int {
        return 0
    }

}


