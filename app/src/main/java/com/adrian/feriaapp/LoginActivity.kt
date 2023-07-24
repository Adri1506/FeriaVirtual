package com.adrian.feriaapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

class LoginActivity : AppCompatActivity() {

    private lateinit var nombreEditText: EditText
    private lateinit var passwordEditText: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.login)

        nombreEditText = findViewById(R.id.nombreEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        var identificacion: String? = null
        var tipoUsuario: String? = null


        val loginButton: Button = findViewById(R.id.loginButton)
        loginButton.setOnClickListener {
            // Obtener los valores ingresados por el usuario
            val username = nombreEditText.text.toString()
            val password = passwordEditText.text.toString()
            // Crear el objeto Usuario
            val inicioSesion = InicioSesion(username, password)
            Log.d("TAG", "log")
            // Convertir el objeto Login a JSON
            val gson = Gson()
            val json = gson.toJson(inicioSesion)
            Log.d("TAG", "Json: $json")
            // Crear el cuerpo de la solicitud HTTP
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = json.toRequestBody(mediaType)

            // Configurar el cliente OkHttpClient
            val client = OkHttpClient.Builder()
                .sslSocketFactory(createUnsafeSSLSocketFactory(), TrustAllCerts())
                .hostnameVerifier { _, _ -> true }
                .build()

            // Crear la solicitud HTTP POST
            val request = Request.Builder()
                .url("https://44.202.231.249:8080/feria-virtual/auth/v10/login")
                .post(requestBody)
                .build()

            // Realizar la llamada al servidor
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // Manejar el error de la solicitud
                    Log.e("TAG", "Error: ${e.message}", e)
                    println("Error al realizar la llamada al servidor: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    // Manejar la respuesta del servidor
                    val statusCode = response.code.toString()
                    Log.d("TAG", "Status: $statusCode")
                    val responseData = response.body?.string()
                    println("Respuesta del servidor: $responseData")

                }
            })


            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Log.e("TAG", "Error: ${e.message}", e)

                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val statusCode = response.code.toString()
                    Log.d("TAG", "Status: $statusCode")
                    val responseBody = response.body?.string()


                    Log.d("TAG", "Response Body: $responseBody")
                    if (response.isSuccessful) {
                        val jsonResponse = JSONObject(responseBody)
                        identificacion = jsonResponse.optString("identificacion")
                        tipoUsuario = jsonResponse.optString("tipoUsuario")
                        runOnUiThread{
                        Toast.makeText(applicationContext,"inicio exitoso",Toast.LENGTH_SHORT).show()
                        }

                        if(tipoUsuario == "CLIENTE") {
                            val intent = Intent(this@LoginActivity, UsuarioMainActivity::class.java)
                            intent.putExtra("identificacion", identificacion)
                            intent.putExtra("tipoUsuario", tipoUsuario)
                            startActivity(intent)
                        }else if (tipoUsuario == "LOCATARIO"){
                            val intent = Intent(this@LoginActivity, LocatarioActivity::class.java)
                            intent.putExtra("identificacion", identificacion)
                            intent.putExtra("tipoUsuario", tipoUsuario)
                            startActivity(intent)
                        }

                         } else {

                        runOnUiThread {
                            Toast.makeText(
                                applicationContext,
                                "El inicio de sesión falló",
                                Toast.LENGTH_SHORT
                            ).show()
                            // Realizar las acciones necesarias en caso de inicio de sesión fallido

                        }
                    }
                }
            })
        }
        val registerButton: Button = findViewById(R.id.registerButton)
        registerButton.setOnClickListener {
            // Iniciar la actividad de inicio de sesión
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }



// Realiza las solicitudes con el cliente OkHttpClient



    private fun getAuthToken(): String? {
        val sharedPreferences = getSharedPreferences("FeriaPreferencias", Context.MODE_PRIVATE)
        return sharedPreferences.getString("authToken",null)

    }

    // Guardar el token de sesión en SharedPreferences
    private fun saveAuthToken(authToken: String) {
        val sharedPreferences = getSharedPreferences("FeriaPreferencias", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("authToken", authToken)
        editor.apply()
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





