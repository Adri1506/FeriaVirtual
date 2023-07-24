package com.adrian.feriaapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
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
import okio.IOException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class MainActivity : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var roleSpinner: Spinner

    private lateinit var identificacionEditText: EditText
    private lateinit var nombreEditText: EditText
    private lateinit var telefonoEditText: EditText
    private lateinit var tipoidentificacionEditText: String

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)

        setContentView(R.layout.registro)


        emailEditText = findViewById(R.id.EmaileditText)
        passwordEditText = findViewById(R.id.editTextPassword)
        roleSpinner = findViewById(R.id.roleSpinner)


        identificacionEditText = findViewById(R.id.editTextidentification)
        nombreEditText = findViewById(R.id.editTextName)

        telefonoEditText = findViewById(R.id.editTextTelefono)



        val roleSpinnerItems = listOf("CLIENTE", "PROVEEDOR", "LOCATARIO", "TRANSPORTISTA")
        val roleAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roleSpinnerItems)
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roleSpinner.adapter = roleAdapter

        roleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedRole = parent.getItemAtPosition(position).toString()

                val hint: String = when (selectedRole) {
                    "CLIENTE" -> "RUN"
                    "PROVEEDOR" -> "CODIGO DE REGISTRO SAG"
                    "LOCATARIO" -> "PATENTE MUNICIPAL"
                    "TRANSPORTISTA" -> "RUN"
                    else -> "Documento"
                }
                identificacionEditText.hint = hint


                tipoidentificacionEditText = when (selectedRole) {
                    "CLIENTE" -> "RUN"
                    "PROVEEDOR" -> "REGISTRO_SAG"
                    "LOCATARIO" -> "PATENTE_MUNICIPAL"
                    "TRANSPORTISTA" -> "RUN"
                    else -> ""
                }.toString()


            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No se realiza ninguna acción cuando no se selecciona nada en el Spinner
            }
        }

        val loginButton: Button = findViewById(R.id.loginButton)
        loginButton.setOnClickListener {
            // Iniciar la actividad de inicio de sesión
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        val registerButton: Button = findViewById(R.id.registerButton)
        registerButton.setOnClickListener {
            // Obtener los valores ingresados por el usuario
            val email = emailEditText.text.toString()
            val contrasena = passwordEditText.text.toString()
            val tipoUsuario = roleSpinner.selectedItem.toString()

            val identificacion = identificacionEditText.text.toString()
            val nombre = nombreEditText.text.toString()
            val telefono = telefonoEditText.text.toString()
            val usuario = Usuario( 0, identificacion, "ACTIVO","2023-06-01T03:11:12", nombre , telefono, tipoidentificacionEditText)
            val login = Login(email, contrasena ,tipoUsuario,usuario)
            Log.d("TAG","log")
            val gson = Gson()
            val json = gson.toJson(login)
            Log.d("TAG","Json: $json")
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = json.toRequestBody(mediaType)
            val client = OkHttpClient.Builder()
                .sslSocketFactory(createUnsafeSSLSocketFactory(), TrustAllCerts())
                .hostnameVerifier { _, _ -> true }
                .build()
            val request = Request.Builder()
                .url("https://44.202.231.249:8080/feria-virtual/auth/v10").post(requestBody)
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("TAG", "Error: ${e.message}", e)       // Manejo de errores en caso de fallo de la solicitud
                }

                override fun onResponse(call: Call, response: Response) {
                    val statusCode = response.code.toString()
                    Log.d("TAG","Status: $statusCode")
                    val responseBody = response.body?.string()

                    Log.d("TAG", "Response Body: $responseBody")   // Manejo de la respuesta del servidor
                    if(response.isSuccessful){
                        runOnUiThread{
                            Toast.makeText(applicationContext,"Registro exitoso",Toast.LENGTH_SHORT).show()





                            val intent = Intent(this@MainActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            })


        }
    }


    private fun saveAuthToken(authToken: String?) {
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
