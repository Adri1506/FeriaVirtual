package com.adrian.feriaapp

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class CargaProductoActivity : AppCompatActivity() {

    private lateinit var productNameEditText: EditText
    private lateinit var productPriceEditText: EditText
    private lateinit var productIdEditText: EditText
    private lateinit var uploadButton: Button
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.subir_producto_local)

        val identificacion = intent.getStringExtra("identificacion")?.trim()//codigo
        val tipoUsuario = intent.getStringExtra("tipoUsuario")//nombre



        // Obtener referencias a los elementos del diseño
        productNameEditText = findViewById(R.id.edit_product_name)
        productIdEditText = findViewById(R.id.edit_product_id)
        productPriceEditText = findViewById(R.id.edit_product_price)




        // Configurar el click listener para el botón de carga de producto
        val uploadButton: Button = findViewById(R.id.btn_upload_product)
        uploadButton.setOnClickListener {
            val productName = productNameEditText.text.toString().trim().lowercase()
            val productPrice = productPriceEditText.text.toString().toDouble()
            val productId = productIdEditText.text.toString()
            // Crear el objeto Producto con los datos del producto
            val producto = Producto(

                codigo = productId,
                estado = "HABILITADO",
                id = 0,
                imagen = "",
                nombre = productName,
                precio = productPrice,
                registroInstante = "2023-06-22T00:10:03.978Z",
                tipo = "FRUTA",
                unidadMedida = "KILOGRAMOS"
            )


            // Llamar a la función para enviar el producto a la API
            val gson = Gson()
            val json = gson.toJson(producto)
            Log.d("TAG", "Json: $json")
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = json.toRequestBody(mediaType)

            val client = OkHttpClient.Builder()
                .sslSocketFactory(createUnsafeSSLSocketFactory(), TrustAllCerts())
                .hostnameVerifier { _, _ -> true }
                .build()

            val url = "https://44.202.231.249:8080/feria-virtual/$identificacion/producto/v10"
            Log.d(TAG,"URL: $url")

            val request = Request.Builder()

                .url(url)
                .post(requestBody)
                .build()



            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    val statusCode = call.execute().code.toString()
                    Log.d("TAG", "Status: $statusCode")
                    val responseBody = call.execute().body?.string()
                    Log.d("TAG", "Response Body: $responseBody")
                    // Manejar el fallo de la solicitud
                    runOnUiThread {
                        // Mostrar mensaje de error al usuario
                        Log.e("TAG", "Error: ${e.message}", e)
                        println("Error al realizar la llamada al servidor: ${e.message}")
                        Toast.makeText(
                            applicationContext,
                            "Error al enviar el producto",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val statusCode = response.code.toString()
                    Log.d("TAG", "Status: $statusCode")
                    val responseBody = response.body?.string()
                    Log.d("TAG", "Response Body: $responseBody")


                    if (response.isSuccessful) {
                        // La carga del producto fue exitosa
                        runOnUiThread {
                            // Mostrar mensaje de éxito al usuario

                            Toast.makeText(
                                applicationContext,
                                "Producto cargado exitosamente",
                                Toast.LENGTH_SHORT
                            ).show()


                            productNameEditText.text.clear()
                            productIdEditText.text.clear()
                            productPriceEditText.text.clear()

                        }
                    } else {
                       Log.d("TAG", "Response Body: $responseBody")

                        // La carga del producto falló
                        runOnUiThread {
                            // Mostrar mensaje de error al usuario
                            Toast.makeText(
                                applicationContext,
                                "la carga del producto falló",
                                Toast.LENGTH_SHORT
                            ).show()
                            productNameEditText.text.clear()
                            productIdEditText.text.clear()
                            productPriceEditText.text.clear()

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

    @SuppressLint("CustomX509TrustManager")
    class TrustAllCerts : X509TrustManager {
        @SuppressLint("TrustAllX509TrustManager")
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        @SuppressLint("TrustAllX509TrustManager")
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }

}

