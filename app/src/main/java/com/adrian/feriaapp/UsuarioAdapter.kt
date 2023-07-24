package com.adrian.feriaapp

// UsuarioAdapter.kt

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class UsuarioAdapter(private val onLocatarioClickListener: OnLocatarioClickListener,private val identificacion: String) : RecyclerView.Adapter<UsuarioAdapter.LocalViewHolder>(){
    private val nombres : MutableList<Locatario> = mutableListOf()




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_fruit, parent, false)
            return LocalViewHolder(view)
        }
    override fun onBindViewHolder(holder: LocalViewHolder, position: Int) {
        val locatario = nombres[position]
        holder.bind(locatario)

    }
        override fun getItemCount(): Int {
            return nombres.size
        }

        @SuppressLint("NotifyDataSetChanged")
        fun setLocales(localesList: List<Locatario>, identificacionLocatario: String) {
            nombres.clear()
            nombres.addAll(localesList)
            notifyDataSetChanged()

        }



        inner class LocalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val textViewLocalName: TextView = itemView.findViewById(R.id.local_name)
            private val imageViewLocal: ImageView = itemView.findViewById(R.id.fruit_image)
            private val buttonGoToStore: Button = itemView.findViewById(R.id.tiendaButton)

            init {
                buttonGoToStore.setOnClickListener {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION && position < nombres.size ) {
                        val locatario = nombres[position]
                        val identificacionLocatario = locatario.identificacionLocatario

                        val intent = Intent(itemView.context, TiendaActivity::class.java).apply {
                            putExtra("Nombre", locatario.nombre)
                            putExtra("identificacionLocatario",identificacionLocatario)
                            putExtra("identificacion",identificacion)
                        }


                        itemView.context.startActivity(intent)
                    }
                }

            }


            fun bind(locatario: Locatario) {

                    textViewLocalName.text = locatario.nombre
                    itemView.setOnClickListener{
                        onLocatarioClickListener.onLocatarioClick(locatario)
                    }

                val randomImageUrl =
                    getRandomImageUrl() // Obtén aleatoriamente una URL de imagen
                Picasso.get().load(randomImageUrl).into(imageViewLocal)


            }
    private fun getRandomImageUrl(): String {
        val randomIndex = (0 until imagenUrls.size).random()
        return imagenUrls[randomIndex]
    }

    private val imagenUrls = listOf(
        "https://cdn.pixabay.com/photo/2021/05/27/18/55/woman-6289052_1280.png",
        "https://cdn.pixabay.com/photo/2014/12/21/23/41/market-575842_640.png",
        "https://cdn.pixabay.com/photo/2022/08/22/21/58/grocery-7404621_640.png",
        "https://cdn.pixabay.com/photo/2017/03/05/00/41/grocery-bag-2117313_640.png"
    )



}

    data class Locatario(val nombre: String, val identificacionLocatario: String)

    interface OnLocatarioClickListener {
        fun onLocatarioClick(locatario: Locatario)
    }

}






