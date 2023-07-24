package com.adrian.feriaapp

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.EditText
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

class PerfilActivity : AppCompatActivity() {
    private lateinit var editCity: EditText
    private lateinit var editDistrict: EditText
    private lateinit var editStreet: EditText
    private lateinit var textUsername: TextView
    private lateinit var textEstado: TextView
    private lateinit var textRun: TextView
    private lateinit var textPhone: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var direccionAdaptador: DireccionAdaptador
    private val direccionList = mutableListOf<Direccion>()
    private lateinit var editUsername : TextView
    private lateinit var editPhone : TextView
    private lateinit var editRun : TextView
    private var obtenerId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.perfil_usuario)
        var identificacions = ""
        val identificacion = intent.getStringExtra("identificacion")
        val tipoUsuario = intent.getStringExtra("tipoUsuario")




        editUsername = findViewById(R.id.editUsername)
        editCity = findViewById(R.id.editCity)
        editDistrict = findViewById(R.id.editDistrict)
        editStreet = findViewById(R.id.editStreet)



        textPhone = findViewById(R.id.editPhone)
        textUsername = findViewById(R.id.editUsername)

        // Obtener los datos del usuario desde la URL
        recyclerView = findViewById(R.id.recyclerViewDireccion)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Configura el adaptador para el RecyclerView
        direccionAdaptador = DireccionAdaptador(direccionList)
        recyclerView.adapter = direccionAdaptador
        identificacions = identificacion.toString()
        getUserData(identificacions)
        setupRecyclerView()
        getUserAddresses(identificacions)


        //Guardar nueva direccion
        val btnSaveButton: Button = findViewById(R.id.btnSaveAddress)
        btnSaveButton.setOnClickListener {
            // Iniciar la actividad de registrar dirección
            val ciudad = editCity.text.toString()
            val comuna = editDistrict.text.toString()
            val direccion = editStreet.text.toString()

            val direction = Direccion(ciudad, comuna, direccion, 0, ubigeo = Ubigeo(0.0, 0.0))
            val gson = Gson()
            val json = gson.toJson(direction)
            Log.d("TAG", "Json: $json")
            // Crear el cuerpo de la solicitud HTTP
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = json.toRequestBody(mediaType)

            val client = OkHttpClient.Builder()
                .sslSocketFactory(createUnsafeSSLSocketFactory(), TrustAllCerts())
                .hostnameVerifier { _, _ -> true }
                .build()

            val request = Request.Builder()
                .url("https://44.202.231.249:8080/feria-virtual/$identificacion/direccion/v10")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // Manejar el error de la solicitud
                    Log.e("TAG", "Error: ${e.message}", e)
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Error al realizar la llamada al servidor", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onResponse(call: Call, response: Response) {
                    // Manejar la respuesta del servidor
                    val statusCode = response.code.toString()
                    Log.d("TAG", "Status: $statusCode")
                    val responseData = response.body?.string()
                    runOnUiThread {
                        Log.d("TAG", "Response Data: $responseData")
                        if (response.isSuccessful) {
                            Toast.makeText(applicationContext, "Registro exitoso", Toast.LENGTH_SHORT).show()
                            // Limpiar los campos de dirección después de guardar
                            editCity.text.clear()
                            editDistrict.text.clear()
                            editStreet.text.clear()
                        } else {
                            Toast.makeText(applicationContext, "Error al registrar la dirección", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }
        //Actualizar datos
        val btnUpdateButton: Button = findViewById(R.id.btnUpdate)
        btnUpdateButton.setOnClickListener {
            // Lógica para actualizar los datos
            val nuevoNombre = editUsername.text.toString()

            val nuevoTelefono = textPhone.text.toString()

            val client = OkHttpClient.Builder()
                .sslSocketFactory(createUnsafeSSLSocketFactory(), TrustAllCerts())
                .hostnameVerifier { _, _ -> true }
                .build()


            val jsonObject = JSONObject()
            jsonObject.put("nombre", nuevoNombre)
            jsonObject.put("telefono", nuevoTelefono)


            val requestBody = jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val url = "https://44.202.231.249:8080/feria-virtual/$identificacion/usuario/v10"
            Log.d(TAG,"URL: $url")
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()



            client.newCall(request).enqueue(object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                    // Manejar el error de la solicitud
                    Log.e("TAG", "Error: ${e.message}", e)
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Error al realizar la llamada al servidor", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {

                    // Manejar la respuesta del servidor
                    val statusCode = response.code.toString()
                    Log.d("TAG", "Status: $statusCode")
                    val responseData = response.body?.string()
                    runOnUiThread {
                        Log.d("TAG", "Response Data: $responseData")
                        if (response.isSuccessful) {
                            Toast.makeText(applicationContext, "Actualización exitosa", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(applicationContext, "Error al actualizar los datos del usuario", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })





        }

    }



    private fun createUnsafeSSLSocketFactory(): SSLSocketFactory {
        val trustAllCerts = arrayOf<TrustManager>(TrustAllCerts())
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, trustAllCerts, SecureRandom())
        }
        return sslContext.socketFactory
    }

    class TrustAllCerts : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }
    //Obtener direcciones
    private fun getUserAddresses(identificacion: String) {
        val client = OkHttpClient.Builder()
            .sslSocketFactory(createUnsafeSSLSocketFactory(), TrustAllCerts())
            .hostnameVerifier { _, _ -> true }
            .build()

        val url = "https://44.202.231.249:8080/feria-virtual/$identificacion/direccion/v10"
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Manejar el error de la solicitud
                Log.e("TAG", "Error: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(applicationContext, "Error al obtener las direcciones del usuario", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // Manejar la respuesta del servidor
                val statusCode = response.code.toString()
                Log.d("TAG", "Status: $statusCode")
                val responseData = response.body?.string()
                runOnUiThread {
                    Log.d("TAG", "Response Data: $responseData")

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

                                // Mostrar las direcciones en la vista correspondiente
                                showUserAddresses(addresses)
                            } else {
                                Toast.makeText(applicationContext, "La respuesta del servidor no contiene el campo 'registro'", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: JSONException) {
                            Toast.makeText(applicationContext, "Error al analizar la respuesta JSON", Toast.LENGTH_SHORT).show()
                        }
                    } else {

                    }
                }
            }


        })
    }



    private fun setupRecyclerView() {
        val recyclerView: RecyclerView  = findViewById(R.id.recyclerViewDireccion)
        recyclerView.layoutManager = LinearLayoutManager(this)
        direccionAdaptador = DireccionAdaptador(direccionList)
        recyclerView.adapter = direccionAdaptador


    }

    private fun showUserAddresses(addresses: List<Direccion>) {
        direccionList.clear() // Limpia la lista existente
        direccionList.addAll(addresses) // Agrega las nuevas direcciones

        direccionAdaptador.notifyDataSetChanged() // Notifica al adaptador de los cambios
        Log.d("TAG", "Direcciones mostradas: ${addresses.size}")
    }



    //obtener datos usuario
    private fun getUserData(identificacion: String) {
        val client = OkHttpClient.Builder()
            .sslSocketFactory(createUnsafeSSLSocketFactory(), TrustAllCerts())
            .hostnameVerifier { _, _ -> true }
            .build()

        val url = "https://44.202.231.249:8080/feria-virtual/admin/usuario/v10?identificacion=$identificacion"
        Log.d(TAG,"URL: $url")
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Manejar el error de la solicitud
                Log.e("TAG", "Error: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(applicationContext, "Error al obtener los datos del usuario", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // Manejar la respuesta del servidor
                val statusCode = response.code.toString()
                Log.d("TAG", "Status: $statusCode")
                val responseData = response.body?.string()
                runOnUiThread {
                    Log.d("TAG", "Response Data: $responseData")
                    if (response.isSuccessful) {
                        // Analizar la respuesta JSON y mostrar los datos del usuario
                        val gson = Gson()
                        val jsonArray = JSONObject(responseData).getJSONArray("registro")
                        val jsonObject = jsonArray.getJSONObject(0)
                        val usuario = gson.fromJson(jsonObject.toString(), Usuario::class.java)
                        Log.d("TAG", "Usuario: $usuario")
                        textUsername.text = "${usuario.nombre}"

                        textPhone.text = "${usuario.telefono}"
                        obtenerId = "${usuario.id}"
                    } else {
                        Toast.makeText(applicationContext, "Error al obtener los datos del usuario", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        })
    }


}

