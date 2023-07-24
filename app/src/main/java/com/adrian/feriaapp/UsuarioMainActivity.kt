package com.adrian.feriaapp

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
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

class UsuarioMainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewProducto: RecyclerView
    private lateinit var usuarioAdapter: UsuarioAdapter
    private lateinit var productoAdapter: ProductoAdapter
    private lateinit var listaUsuarios: LinearLayoutManager
    private lateinit var backButton: Button
    private val usuarios: MutableList<Usuario> = mutableListOf()

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.inicio_usuario)
        backButton = findViewById(R.id.backButton)

        recyclerView = findViewById(R.id.recyclerView)


        val identificacion = intent.getStringExtra("identificacion")//codigo
        Log.d(TAG, "Usuareiomainactity : identificacion2")
        val tipoUsuario = intent.getStringExtra("tipoUsuario")//nombre

        val ComprasButton: Button = findViewById(R.id.tiendasButton)
        ComprasButton.setOnClickListener {
            // Iniciar la actividad de perfil
            val intent = Intent(this@UsuarioMainActivity, ComprasActivity::class.java)
            intent.putExtra("identificacion", identificacion)
            startActivity(intent)

        }
        val salirButton: Button = findViewById(R.id.salirButton)
        salirButton.setOnClickListener {
            val intent = Intent(this@UsuarioMainActivity, LoginActivity::class.java)
            intent.putExtra("identificacion", identificacion)
            intent.putExtra("tipoUsuario", tipoUsuario)
            startActivity(intent)
        }

        val CarritoButton: Button = findViewById(R.id.productosButton)
        CarritoButton.setOnClickListener {
            // Iniciar la actividad de perfil
            val intent = Intent(this@UsuarioMainActivity, CarritoComprasActivity::class.java)
            intent.putExtra("identificacion", identificacion)
            startActivity(intent)
        }

        val perfilButton: Button = findViewById(R.id.perfilButton)
        perfilButton.setOnClickListener {
            // Iniciar la actividad de perfil
            val intent = Intent(this@UsuarioMainActivity, PerfilActivity::class.java)
            intent.putExtra("identificacion", identificacion)
            startActivity(intent)

        }

        val client = OkHttpClient.Builder()
            .sslSocketFactory(createUnsafeSSLSocketFactory(), PerfilActivity.TrustAllCerts())
            .hostnameVerifier { _, _ -> true }
            .build()

        val urlBuilder = "https://44.202.231.249:8080/feria-virtual/admin/usuario/v10".toHttpUrlOrNull()?.newBuilder()

        urlBuilder?.addQueryParameter("tipoIdentificacion", "PATENTE_MUNICIPAL")
        urlBuilder?.addQueryParameter("limit", "100")
        urlBuilder?.addQueryParameter("offset", "0")

        val url = urlBuilder?.build().toString()

        val request = Request.Builder()
            .url(url)
            .build()


        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val statusCode = response.code.toString()
                Log.d("TAG", "Status: $statusCode")
                val responseData = response.body?.string()
                Log.d("DatosRecibidos", responseData.toString())
                val localesList:List<UsuarioAdapter.Locatario> = listOf()


                var identificacionLocatario = "${identificacion}"
                Log.d(TAG,"DatosRecibidos : $identificacion",)

                try {
                    // Parsea la respuesta JSON
                    val jsonObject = JSONObject(responseData)
                    val jsonArray = jsonObject.getJSONArray("registro")
                    val locatarios = mutableListOf<UsuarioAdapter.Locatario>()

                    // Itera sobre los objetos en el arreglo
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val nombre = jsonObject.getString("nombre")
                        identificacionLocatario = jsonObject.getString("identificacion")

                        locatarios.add(UsuarioAdapter.Locatario(nombre.toString(), identificacionLocatario.toString()))
                    }

                    runOnUiThread {
                        val adapter = UsuarioAdapter(object : UsuarioAdapter.OnLocatarioClickListener {
                            override fun onLocatarioClick(locatario: UsuarioAdapter.Locatario) {
                                val intent = Intent(this@UsuarioMainActivity, TiendaActivity::class.java)
                                intent.putExtra("nombre", locatario.nombre)
                                intent.putExtra("identificacionLocatario", locatario.identificacionLocatario)
                                intent.putExtra("identificacion2", identificacion)
                                Log.d(TAG,"intent identificacion: $identificacion")
                                Log.d(TAG, "IdentificacionLocatario: $identificacionLocatario")
                                startActivity(intent)
                            }
                        }, identificacion = identificacion.toString())
                        adapter.setLocales(locatarios, identificacionLocatario)
                        recyclerView.adapter = adapter
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

        recyclerViewProducto = findViewById(R.id.recyclerViewProductos)

        val productos = listOf(
            Producto(0,"01","manzana", "VERDURA","KILOGRAMOS",1200.0,"HABILITADO","2023",""),
            Producto(0,"02","pera", "VERDURA","KILOGRAMOS",1600.0,"HABILITADO","2023",""),
            Producto(0,"03","papa", "VERDURA","KILOGRAMOS",800.0,"HABILITADO","2023",""),
            Producto(0,"04","zapallo", "VERDURA","KILOGRAMOS",1800.0,"HABILITADO","2023",""),
            Producto(0,"05","naranja", "FRUTA","KILOGRAMOS",2200.0,"HABILITADO","2023",""),
            Producto(0,"06","durazno", "FRUTA","KILOGRAMOS",1900.0,"HABILITADO","2023",""),
            Producto(0,"07","platano", "FRUTA","KILOGRAMOS",1200.0,"HABILITADO","2023",""),
            Producto(0,"08","melon", "FRUTA","KILOGRAMOS",500.0,"HABILITADO","2023",""),
        )

        productoAdapter = ProductoAdapter(this, productos, identificacionLocatario ="")

        recyclerViewProducto.adapter = productoAdapter
        recyclerViewProducto.layoutManager = LinearLayoutManager(this)
    }

    private fun createUnsafeSSLSocketFactory(): SSLSocketFactory {
        val trustAllCerts = arrayOf<TrustManager>(CargaProductoActivity.TrustAllCerts())
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